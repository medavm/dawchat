package dawchat.services

import dawchat.domain.channel.UserPermissions
import dawchat.domain.message.Message
import dawchat.domain.message.MessageTypes
import dawchat.domain.user.AuthenticatedUser
import dawchat.domain.user.Event
import dawchat.domain.user.MessageEventData
import dawchat.domain.user.User
import org.springframework.stereotype.Service
import java.util.LinkedList



class ChannelMessageInfo(
    private val m: Message,
    private val user: User
){
    val messageId = m.messageId
    val userId  = m.userId
    val userName = user.username
    val type = m.type
    val content = m.content
    val timestamp = m.timestamp
}


class LoadMessagesResult(
    val channelId: Int,
    val channelName: String,
    val messages: Array<ChannelMessageInfo>
)

class LoadMessagesOptions(
    val channelId: Int,
    val types: Array<MessageTypes>?,
    val lastMessage: Int?,
    val limit: Int?
)

sealed class ChannelMessagesError{
    data object ChannelNotFound:        ChannelMessagesError()
    data object NoPermission:           ChannelMessagesError()
}

sealed class CreateMessageError{
    data object ChannelNotFound:        CreateMessageError()
    data object NoPermission:           CreateMessageError()
}

sealed class UpdateLastReadError{
    data object ChannelNotFound:        UpdateLastReadError()
    data object UserNotInChannel:       UpdateLastReadError()
    data object InvalidMessageId:       UpdateLastReadError()
}

@Service
class MessageService(
    private val serviceUtils: ServiceUtils
) {

    fun createMessage(authUser: AuthenticatedUser, channelId: Int, type: MessageTypes, content: String):
            ServiceResult<ChannelMessageInfo, CreateMessageError>{

        var sse: SSEEvent? = null;
        val res = serviceUtils.transactionManager.run {
            val channel = it.channelsRepo.getChannel(channelId)
                ?: return@run ServiceResult.Error(CreateMessageError.ChannelNotFound)
            val channelUser = it.channelsRepo.getChannelUser(authUser.user.id, channel.id)
                ?: return@run ServiceResult.Error(CreateMessageError.NoPermission)
            if(UserPermissions.Write !in channelUser.permissions)
                return@run ServiceResult.Error(CreateMessageError.NoPermission)
            val msg = it.messagesRepo.createMessage(
                authUser.user.id,
                channelId,
                type,
                content,
                serviceUtils.clock.now()
            )

            it.channelsRepo.updateLastMessage(channelId, msg.messageId)
            it.messagesRepo.updateLastRead(authUser.user.id, channelId, msg.messageId)
            val mInfo = ChannelMessageInfo(msg, authUser.user)

            val recipients = it.channelsRepo.getChannelUsers(channelId).map { user -> user.userId }.toTypedArray()
            sse = SSEEvent(
                ev = Event.ChannelMessage(MessageEventData(channel, msg, authUser.user)),
                recipients = recipients
            )

            return@run ServiceResult.Success(mInfo)
        }


        serviceUtils.eventEmitter.queueEvent(sse);
        return res
    }



    private fun getChannelMessages(authUser: AuthenticatedUser, channelId: Int,
                                   filter: Array<MessageTypes>?, lastMessage: Int?, limit: Int?):
            ServiceResult<Array<ChannelMessageInfo>, ChannelMessagesError>{
        return serviceUtils.transactionManager.run {

            val types = filter?:MessageTypes.entries.toTypedArray()
            val last = lastMessage?:Int.MAX_VALUE
            val lim = if(limit!=null && limit <= 100) limit else 100
            val msgs = it.messagesRepo.getChannelMessages(channelId, types, last, lim)

            val msgsInfo = msgs.map { m ->
                val user = it.usersRepo.getUser(m.userId)
                    ?: TODO()
                ChannelMessageInfo(m, user)
            }
            return@run ServiceResult.Success(msgsInfo.toTypedArray())
        }
    }

    fun channelMessages(authUser: AuthenticatedUser, channelsOptions: Array<LoadMessagesOptions>)
    : ServiceResult<Array<LoadMessagesResult>, ChannelMessagesError>{
        return serviceUtils.transactionManager.run {
            val toRet = LinkedList<LoadMessagesResult>()
            for (options in channelsOptions){
                val channel = it.channelsRepo.getChannel(options.channelId)
                    ?: return@run ServiceResult.Error(ChannelMessagesError.ChannelNotFound)
                val channelUser = it.channelsRepo.getChannelUser(authUser.user.id, channel.id)
                    ?: return@run ServiceResult.Error(ChannelMessagesError.NoPermission)
                val res = getChannelMessages(authUser, options.channelId, options.types, options.lastMessage, options.limit)
                when(res){
                    is ServiceResult.Success -> toRet.add(LoadMessagesResult(
                        channelId = options.channelId,
                        channelName = channel.name,
                        messages = res.result
                    ))
                    is ServiceResult.Error ->
                        return@run res
                }
            }
            return@run ServiceResult.Success(toRet.toTypedArray())
        }
    }



    fun updateLastRead(authUser: AuthenticatedUser, channelId: Int, messageId: Int):
            ServiceResult<Boolean, UpdateLastReadError>{
        return serviceUtils.transactionManager.run {
            val channel = it.channelsRepo.getChannel(channelId)
                ?: return@run ServiceResult.Error(UpdateLastReadError.ChannelNotFound)
            val channelUser = it.channelsRepo.getChannelUser(authUser.user.id, channel.id)
                ?: return@run ServiceResult.Error(UpdateLastReadError.UserNotInChannel)

            val message = it.messagesRepo.getMessage(messageId)
                ?: return@run ServiceResult.Error(UpdateLastReadError.InvalidMessageId)

            it.messagesRepo.updateLastRead(authUser.user.id, channelId, messageId)

            return@run ServiceResult.Success(true)
        }

    }




}