package dawchat.domain.channel

import kotlinx.datetime.Instant

enum class InviteStatus {
    Pending,
    Accepted,
    Denied,
    Revoked,
    Expired
}

data class ChannelInvite (
    val inviteId: Int,
    val senderId: Int,
    val recipientId: Int,
    val channelId: Int,
    val permissions: Array<UserPermissions>,
    val message: String?,
    val status: InviteStatus,
    val createdAt: Instant,
    val resolvedAt: Instant?,
){
}


