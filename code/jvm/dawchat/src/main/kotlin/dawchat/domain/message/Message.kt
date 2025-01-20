package dawchat.domain.message

import kotlinx.datetime.Instant

enum class MessageTypes{
    ChannelCreated,
    ChannelRenamed,
    Text
}

class Message(
    val messageId: Int,
    val userId: Int,
    val channelId: Int,
    val type: MessageTypes,
    val content: String?,
    val timestamp: Long
) {
}