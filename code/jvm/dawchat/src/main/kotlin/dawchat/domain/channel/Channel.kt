package dawchat.domain.channel

import kotlinx.datetime.Instant

enum class ChannelType{
    Private,
    Public
}

class Channel(
    val id: Int,
    val name: String,
    val type: ChannelType,
    val ownerId: Int,
    val lastMessage: Int?,
    val createdAt: Instant,
)