package dawchat.domain.user

import dawchat.domain.channel.Channel
import dawchat.domain.channel.ChannelUser
import dawchat.domain.message.Message


class MessageEventData(
    private val channel: Channel,
    private val m: Message,
    private val user: User
){
    val channelId = channel.id
    val channelName = channel.name
    val messageId = m.messageId
    val userId  = m.userId
    val userName = user.username
    val type = m.type
    val content = m.content
    val timestamp = m.timestamp
}

class RenameEventData(
    val channel: Channel,
    val channelUser: ChannelUser,
)

data class JoinChannelEventData(
    private val channel: Channel,
    private val channelUser: ChannelUser
){
    val channelId = channel.id
    val channelName = channel.name
    val channelType = channel.type
    val permissions = channelUser.permissions
    val lastMessage = channel.lastMessage
    val lastRead = channelUser.lastRead
    val ownerId = channel.ownerId
    val joinedAt = channelUser.joinedAt
}

class LeftChannelEventData(
    val channel: Channel
)

class RemovedFromChannelEventData(
    val channel: Channel
)


sealed class Event {

    data class ChannelMessage(val d: MessageEventData) : Event()

    data class ChannelRename(val d: RenameEventData):   Event()

    data class JoinChannel(val d: JoinChannelEventData): Event()

    data class LeftChannel(val d: LeftChannelEventData): Event()

    data class RemovedFromChannel(val d: RemovedFromChannelEventData): Event()
}
