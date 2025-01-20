package dawchat.repojdbi

import dawchat.domain.channel.ChannelInvite
import dawchat.domain.channel.InviteStatus
import dawchat.domain.channel.UserPermissions
import dawchat.repo.InvitesRepo
import dawchat.repojdbi.mappers.ChannelInviteMapper
import dawchat.repojdbi.mappers.ChannelUserMapper
import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiInvitesRepo(
    private val handle: Handle
): InvitesRepo {

    override fun createChannelInvite(senderId: Int, recipientId: Int, channelId: Int, permissions: Array<UserPermissions>,
                                     message: String?, status: InviteStatus, createdAt: Instant
    ): ChannelInvite {
        val res = handle.createUpdate(
            """
            INSERT INTO dbo.ChannelInvites (senderId, recipientId, channelId, permissions, message, status, createdAt) 
                values (:senderId, :recipientId, :channelId, :permissions, :message, :status, :createdAt)
            """,
        )
            .bind("senderId", senderId)
            .bind("recipientId", recipientId)
            .bind("channelId", channelId)
            .bind("permissions", ChannelUserMapper.fromUserPermissions(permissions))
            .bind("message", message)
            .bind("status", status)
            .bind("createdAt", createdAt.epochSeconds)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

        return ChannelInvite(
            inviteId = res,
            senderId = senderId,
            recipientId = recipientId,
            channelId = channelId,
            permissions = permissions,
            message = message,
            status = status,
            createdAt = createdAt,
            resolvedAt = null
        )
    }


    override fun updateInviteStatus(inviteId: Int, status: InviteStatus, resolvedAt: Instant) {
        handle.createUpdate(
            """
                update dbo.ChannelInvites set status = :status, resolvedAt = :resolvedAt where inviteId = :inviteId
            """,
        )
            .bind("status",status)
            .bind("resolvedAt", resolvedAt.epochSeconds)
            .bind("inviteId", inviteId)
            .execute()
    }

    override fun updateUserInvitesStatus(recipientId: Int, currentStatus: InviteStatus, newStatus: InviteStatus, resolvedAt: Instant){
        handle.createUpdate(
            """
                update dbo.ChannelInvites set status = :newStatus, resolvedAt = :resolvedAt
                WHERE recipientId = :recipientId 
                AND status = :currentStatus
            """,
        )
            .bind("newStatus", newStatus)
            .bind("resolvedAt", resolvedAt.epochSeconds)
            .bind("recipientId", recipientId)
            .bind("currentStatus", currentStatus)
            .execute()
    }

    override fun getChannelInvite(inviteId: Int): ChannelInvite? {
        return handle.createQuery("SELECT * FROM dbo.ChannelInvites WHERE inviteId = :inviteId")
            .bind("inviteId", inviteId)
            .mapTo<ChannelInvite>()
            .singleOrNull()
    }

    override fun getChannelInvite(recipientId: Int, channelId: Int, status: InviteStatus): ChannelInvite? {
        return handle.createQuery(
            """
              SELECT * FROM dbo.ChannelInvites WHERE 
              recipientId = :recipientId
              AND channelId = :channelId
              AND status = :status
            """
        )
            .bind("recipientId", recipientId)
            .bind("channelId", channelId)
            .bind("status", status)
            .mapTo<ChannelInvite>()
            .singleOrNull()
    }

    override fun getChannelInvite(senderId: Int, recipientId: Int, channelId: Int, status: InviteStatus): ChannelInvite? {
        return handle.createQuery(
            """
              SELECT * FROM dbo.ChannelInvites WHERE 
              senderId = :senderId AND recipientId = :recipientId 
              AND channelId = :channelId
              AND status = :status
            """
        )
            .bind("senderId", senderId)
            .bind("recipientId", recipientId)
            .bind("channelId", channelId)
            .bind("status", status)
            .mapTo<ChannelInvite>()
            .singleOrNull()
    }

    override fun getChannelInvites(channelId: Int, status: InviteStatus): Array<ChannelInvite> {
        return handle.createQuery(
            """
               SELECT * FROM dbo.ChannelInvites 
               WHERE channelId = :channelId AND status = :status
            """,
        )
            .bind("channelId", channelId)
            .bind("status", status)
            .mapTo<ChannelInvite>().list().toTypedArray()
    }

    override fun getUserInvites(userId: Int, status: InviteStatus): Array<ChannelInvite> {
        return handle.createQuery(
            """
               SELECT * FROM dbo.ChannelInvites 
               WHERE recipientId = :userId AND status = :status
            """,
        )
            .bind("userId", userId)
            .bind("status", status)
            .mapTo<ChannelInvite>().list().toTypedArray()
    }
}