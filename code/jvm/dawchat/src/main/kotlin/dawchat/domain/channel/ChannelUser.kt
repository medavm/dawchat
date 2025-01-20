package dawchat.domain.channel

import kotlinx.datetime.Instant


enum class UserPermissions{
    Read,
    Write,
    Invite,
    Rename,
    RemoveUsers
}

class ChannelUser(
    val userId: Int,
    val channelId: Int,
    val permissions: Array<UserPermissions>,
    val inviteId: Int?,
    val lastRead: Int?,
    val joinedAt: Instant,
)