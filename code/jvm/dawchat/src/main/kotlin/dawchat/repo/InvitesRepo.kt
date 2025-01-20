package dawchat.repo

import dawchat.domain.channel.ChannelInvite
import dawchat.domain.channel.InviteStatus
import dawchat.domain.channel.UserPermissions
import kotlinx.datetime.Instant

interface InvitesRepo {

    fun createChannelInvite(senderId: Int, recipientId: Int, channelId: Int, permissions: Array<UserPermissions>,
                            message: String?, status: InviteStatus, createdAt: Instant
    ): ChannelInvite

    fun updateInviteStatus(inviteId: Int, status: InviteStatus, resolvedAt: Instant)

    fun updateUserInvitesStatus(recipientId: Int, currentStatus: InviteStatus, newStatus: InviteStatus, resolvedAt: Instant)

    fun getChannelInvite(inviteId: Int): ChannelInvite?

    fun getChannelInvite(recipientId: Int, channelId: Int, status: InviteStatus): ChannelInvite?

    fun getChannelInvite(senderId: Int, recipientId: Int, channelId: Int, status: InviteStatus): ChannelInvite?

    fun getChannelInvites(channelId: Int, status: InviteStatus): Array<ChannelInvite>

    fun getUserInvites(userId: Int, status: InviteStatus): Array<ChannelInvite>



}