package dawchat.services

import dawchat.domain.channel.*
import dawchat.domain.message.MessageTypes
import dawchat.domain.user.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.datetime.Instant
import org.springframework.stereotype.Service
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


data class ChannelUserInfo(
    private val user: User,
    private val channel: Channel,
    private val channelUser: ChannelUser
){
    val userId = user.id
    val username = user.username
    val channelId = channel.id
    val channelName = channel.name
    val channelType = channel.type
    val permissions = channelUser.permissions
    val lastMessage = channel.lastMessage
    val lastRead = channelUser.lastRead
    val ownerId = channel.ownerId
    val joinedAt = channelUser.joinedAt
}


sealed class CreateChannelError{
    data object InvalidChannelName:     CreateChannelError()
    data object ChannelNameTaken:       CreateChannelError()
}

sealed class ChannelInfoError{
    data object ChannelNotFound:        ChannelInfoError()
    data object NoPermission:           ChannelInfoError()
}

sealed class RenameChannelError{
    data object ChannelNotFound:        RenameChannelError()
    data object InvalidChannelName:     RenameChannelError()
    data object NoPermission:           RenameChannelError()
    data object ChannelNameTaken:       RenameChannelError()
}

sealed class JoinChannelError{
    data object ChannelNotFound:        JoinChannelError()
    data object UserAlreadyInChannel:   JoinChannelError()
    data object ChannelInviteNotFound:  JoinChannelError()
    data object InvalidChannelInvite:   JoinChannelError()
    data object NoPermission:           JoinChannelError()
}

sealed class LeaveChannelError{
    data object ChannelNotFound:        LeaveChannelError()
    data object UserNotInChannel:       LeaveChannelError()
    data object SelectNewOwner:         LeaveChannelError()
}

sealed class RemoveUserError{
    data object ChannelNotFound:        RemoveUserError()
    data object UserNotInChannel:       RemoveUserError()
    data object CantRemoveOwner:        RemoveUserError()
    data object CantSelfRemove:         RemoveUserError()
    data object NoPermission:           RemoveUserError()
}

sealed class ChannelUsersError{
    data object ChannelNotFound:        ChannelUsersError()
    data object NoPermission:           ChannelUsersError()
}


@Service
class ChannelsService(
    val serviceUtils: ServiceUtils
) {

    //prevents user from leaving channel mid transferring ownership
    private val leaveChannelLock = ReentrantLock()

    fun createChannel(authUser: AuthenticatedUser, name: String, type: ChannelType):
            ServiceResult<ChannelUserInfo, CreateChannelError>{

        if(!serviceUtils.channelDomain.validateChannelName(name))
            return ServiceResult.Error(CreateChannelError.InvalidChannelName)

        var sse: SSEEvent? = null;
        val res = serviceUtils.transactionManager.run {

            if(it.channelsRepo.getChannel(name) != null)
                return@run ServiceResult.Error(CreateChannelError.ChannelNameTaken)

            val channel = it.channelsRepo.createChannel(
                user = authUser.user,
                name = name,
                type = type,
                createdAt = serviceUtils.clock.now()
            )

            val channelUser = it.channelsRepo.addUserToChannel(
                userId = authUser.user.id,
                channelId = channel.id,
                permissions = UserPermissions.entries.toTypedArray(),
                inviteId = null,
                joinedAt = serviceUtils.clock.now()
            )

            val m = it.messagesRepo.createMessage(authUser.user.id, channel.id,
                MessageTypes.ChannelCreated, null, serviceUtils.clock.now())

            it.channelsRepo.updateLastMessage(channel.id, m.messageId)
            val updatedChannel = it.channelsRepo.getChannel(channel.id)
                ?:TODO()

            val channelUserInfo = ChannelUserInfo(authUser.user, updatedChannel, channelUser)

            val recipients = arrayOf(authUser.user.id)
            sse = SSEEvent(
                ev = Event.JoinChannel(JoinChannelEventData(updatedChannel, channelUser)),
                recipients = recipients
            )

            return@run ServiceResult.Success(channelUserInfo)
        }

        serviceUtils.eventEmitter.queueEvent(sse);
        return res
    }

    fun channelInfo(authUser: AuthenticatedUser, channelId: Int):
            ServiceResult<ChannelUserInfo, ChannelInfoError>{
        return serviceUtils.transactionManager.run {

            val channel = it.channelsRepo.getChannel(channelId)
                ?: return@run ServiceResult.Error(ChannelInfoError.ChannelNotFound)

            val channelUser = it.channelsRepo.getChannelUser(authUser.user.id, channelId)
                ?: return@run ServiceResult.Error(ChannelInfoError.NoPermission)

            val channelUserInfo  = ChannelUserInfo(authUser.user, channel, channelUser)

            return@run ServiceResult.Success(channelUserInfo)
        }
    }

    fun renameChannel(authUser: AuthenticatedUser, channelId: Int, name: String):
            ServiceResult<Channel, RenameChannelError>{

        if(!serviceUtils.channelDomain.validateChannelName(name))
            return ServiceResult.Error(RenameChannelError.InvalidChannelName)

        var sse: SSEEvent? = null
        var sse2: SSEEvent? = null
        val res =  serviceUtils.transactionManager.run {

            val channel = it.channelsRepo.getChannel(channelId)
                ?: return@run ServiceResult.Error(RenameChannelError.ChannelNotFound)

            val channelUser = it.channelsRepo.getChannelUser(authUser.user.id, channelId)
                ?: return@run ServiceResult.Error(RenameChannelError.NoPermission)

            if(UserPermissions.Rename !in channelUser.permissions)
                return@run ServiceResult.Error(RenameChannelError.NoPermission)

            if(it.channelsRepo.getChannel(name)!=null)
                return@run ServiceResult.Error(RenameChannelError.ChannelNameTaken)

            it.channelsRepo.renameChannel(channelId, name)

            val renamedChannel = it.channelsRepo.getChannel(channelId)
                ?: TODO()

            val m = it.messagesRepo.createMessage(authUser.user.id, channel.id,
                MessageTypes.ChannelRenamed, name, serviceUtils.clock.now())

            //it.channelsRepo.updateLastEvent(channelId, event.id) update on rename?

            val recipients = it.channelsRepo.getChannelUsers(channelId).map { user -> user.userId }.toTypedArray()
            sse = SSEEvent(
                ev = Event.ChannelRename(RenameEventData(renamedChannel, channelUser)),
                recipients = recipients
            )
            sse2 = SSEEvent(
                ev = Event.ChannelMessage(MessageEventData(channel, m, authUser.user)),
                recipients = recipients
            )
            return@run ServiceResult.Success(renamedChannel)
        }

        serviceUtils.eventEmitter.queueEvent(sse)
        serviceUtils.eventEmitter.queueEvent(sse2)
        return res
    }

    fun userChannels(authUser: AuthenticatedUser):
            Array<ChannelUserInfo>{
        return serviceUtils.transactionManager.run {
            val res = ArrayList<ChannelUserInfo>()
            val channelsUser = it.channelsRepo.getUserChannels(authUser.user)
            for (channelUser in channelsUser){
                val channel = it.channelsRepo.getChannel(channelUser.channelId)
                    ?: TODO()
                res.add(ChannelUserInfo(authUser.user, channel, channelUser))
            }
            res.toTypedArray()
        }
    }

    fun channelUsers(authUser: AuthenticatedUser, channelId: Int):
            ServiceResult<Array<ChannelUserInfo>, ChannelUsersError>{
        return serviceUtils.transactionManager.run {

            val channel = it.channelsRepo.getChannel(channelId)
                ?: return@run ServiceResult.Error(ChannelUsersError.ChannelNotFound)

            val chUser = it.channelsRepo.getChannelUser(authUser.user.id, channel.id)
                ?: return@run ServiceResult.Error(ChannelUsersError.NoPermission)

            val res = ArrayList<ChannelUserInfo>()
            val channelUsers = it.channelsRepo.getChannelUsers(channel.id)
            for (channelUser in channelUsers){
                val user = it.usersRepo.getUser(channelUser.userId)
                    ?: TODO()
                res.add(ChannelUserInfo(user, channel, channelUser))
            }
            return@run ServiceResult.Success(res.toTypedArray())
        }
    }

    fun searchChannel(name: String): Array<Channel>{
        return serviceUtils.transactionManager.run {
            val channels = it.channelsRepo.searchChannels(name, ChannelType.Public)
            channels
        }
    }

    fun joinChannel(authUser: AuthenticatedUser, channelId: Int, inviteId: Int?):
            ServiceResult<ChannelUserInfo, JoinChannelError>  {


        var sse: SSEEvent? = null;
        val res = serviceUtils.transactionManager.run {

            val channel = it.channelsRepo.getChannel(channelId)
                ?:return@run ServiceResult.Error(JoinChannelError.ChannelNotFound)

            if(it.channelsRepo.getChannelUser(authUser.user.id, channelId) != null)
                return@run ServiceResult.Error(JoinChannelError.UserAlreadyInChannel)

            val invite = inviteId?.let{ id ->
                val iv = it.invitesRepo.getChannelInvite(id)
                    ?:return@run ServiceResult.Error(JoinChannelError.ChannelInviteNotFound)
                if (iv.channelId != channelId
                    || iv.status != InviteStatus.Pending
                    || iv.recipientId != authUser.user.id)
                    return@run ServiceResult.Error(JoinChannelError.InvalidChannelInvite)
                iv
            }


            if(!serviceUtils.channelDomain.canJoinChannel(authUser, channel, invite))
                return@run ServiceResult.Error(JoinChannelError.NoPermission)

            val perms = invite?.permissions ?: serviceUtils.channelDomain.defaultJoinPermissions(channel.type)

            val channelUser = it.channelsRepo.addUserToChannel(
                userId = authUser.user.id,
                channelId = channel.id,
                permissions = perms,
                inviteId = invite?.inviteId,
                joinedAt = serviceUtils.clock.now()
            )

            val chanelUserInfo = ChannelUserInfo(authUser.user, channel, channelUser)

            if(invite!=null)
                it.invitesRepo.updateInviteStatus(inviteId, InviteStatus.Accepted, serviceUtils.clock.now())

            val recipients = arrayOf(authUser.user.id)
            sse = SSEEvent(
                ev = Event.JoinChannel(JoinChannelEventData(channel, channelUser)),
                recipients = recipients
            )

            return@run ServiceResult.Success(chanelUserInfo)
        }

        serviceUtils.eventEmitter.queueEvent(sse);
        return res
    }

    fun leaveChannel(authUser: AuthenticatedUser, channelId: Int, newOwnerId: Int?) :
            ServiceResult<Boolean, LeaveChannelError> = leaveChannelLock.withLock{


        var sse: SSEEvent? = null;
        val res = serviceUtils.transactionManager.run {

            val channel = it.channelsRepo.getChannel(channelId)
                ?:return@run ServiceResult.Error(LeaveChannelError.ChannelNotFound)

            val channelUser = it.channelsRepo.getChannelUser(authUser.user.id, channelId)
                ?: return@run ServiceResult.Error(LeaveChannelError.UserNotInChannel)

            if(channel.ownerId == authUser.user.id){ //owner wants to leave...

                val channelUsers = it.channelsRepo.getChannelUsers(channel.id)

                if(channelUsers.size > 1){ //set new channel owner
                    if(newOwnerId==null)
                        return@run ServiceResult.Error(LeaveChannelError.SelectNewOwner)

                    val newOwnerChUser = it.channelsRepo.getChannelUser(newOwnerId, channelId)
                        ?: return@run ServiceResult.Error(LeaveChannelError.UserNotInChannel)

                    it.channelsRepo.updateOwner(channelId, newOwnerId)
                    it.channelsRepo.updatePermissions(channelId, newOwnerId, UserPermissions.entries.toTypedArray())
                }
                else{ //delete the channel
                    //TODO()
                }
            }

            it.channelsRepo.removeUserFromChannel(authUser.user.id, channelId)
            val recipients = arrayOf(authUser.user.id)
            sse = SSEEvent(
                ev = Event.LeftChannel(LeftChannelEventData(channel)),
                recipients = recipients
            )

            return@run ServiceResult.Success(true)
        }

        serviceUtils.eventEmitter.queueEvent(sse);
        return res

    }

    fun removeUser(authUser: AuthenticatedUser, channelId: Int, userId: Int):
            ServiceResult<Boolean, RemoveUserError>{

        var sse: SSEEvent? = null;
        val res = serviceUtils.transactionManager.run {

            val channel = it.channelsRepo.getChannel(channelId)
                ?:return@run ServiceResult.Error(RemoveUserError.ChannelNotFound)

            if(authUser.user.id == userId)
                return@run ServiceResult.Error(RemoveUserError.CantSelfRemove)

            val authChannelUser = it.channelsRepo.getChannelUser(authUser.user.id, channelId)
                ?: return@run ServiceResult.Error(RemoveUserError.NoPermission)

            if(UserPermissions.RemoveUsers !in authChannelUser.permissions)
                return@run ServiceResult.Error(RemoveUserError.NoPermission)

            val channelUser = it.channelsRepo.getChannelUser(userId, channelId)
                ?: return@run ServiceResult.Error(RemoveUserError.UserNotInChannel)

            if(channel.ownerId == userId)
                return@run ServiceResult.Error(RemoveUserError.CantRemoveOwner)

            it.channelsRepo.removeUserFromChannel(userId, channelId)

            val removedUser = it.usersRepo.getUser(userId)
                ?: TODO()

            val recipients = arrayOf(removedUser.id)
            sse = SSEEvent(
                ev = Event.RemovedFromChannel(RemovedFromChannelEventData(channel)),
                recipients = recipients
            )

            return@run ServiceResult.Success(true)
        }

        serviceUtils.eventEmitter.queueEvent(sse);
        return res
    }

    fun removeUsers(authUser: AuthenticatedUser, channelId: Int, userIds: Array<Int>):
    ServiceResult<Boolean, RemoveUserError>{
        for (userId in userIds){
            val res = removeUser(authUser, channelId, userId)
            if(res is ServiceResult.Error)
                return res
        }
        return ServiceResult.Success(true)
    }



}