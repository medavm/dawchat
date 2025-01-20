package dawchat.repojdbi

import dawchat.domain.channel.*
import dawchat.domain.message.Message
import dawchat.domain.user.User
import dawchat.repo.ChannelRepo
import dawchat.repojdbi.mappers.ChannelInviteMapper
import dawchat.repojdbi.mappers.ChannelMapper
import dawchat.repojdbi.mappers.ChannelUserMapper
import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiChannelRepo (
    private val handle: Handle,
): ChannelRepo {
    override fun createChannel(user: User, name: String, type: ChannelType, createdAt: Instant): Channel{
        val channelId = handle.createUpdate(
            """
            insert into dbo.Channels (name, type, ownerId, createdAt) 
                values (:name, :type, :userId, :createdAt)
            """,
        )
            .bind("name", name)
            .bind("type", type)
            .bind("userId", user.id)
            .bind("createdAt", createdAt.epochSeconds)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
        return Channel(channelId, name,type, user.id, null, createdAt)
    }

    override fun getChannel(name: String): Channel? {
        return handle.createQuery("select * from dbo.Channels where name = :name")
            .bind("name", name)
            .mapTo<Channel>()
            .singleOrNull()
    }

    override fun getChannel(channelId: Int): Channel? {
        return handle.createQuery("select * from dbo.Channels where id = :channelId")
            .bind("channelId", channelId)
            .mapTo<Channel>()
            .singleOrNull()
    }

    override fun renameChannel(channelId: Int, name: String) {
        handle.createUpdate(
            """
                update dbo.channels set name = :name where id = :channelId
            """,
        )
            .bind("name", name)
            .bind("channelId", channelId)
            .execute()
    }



    override fun addUserToChannel(
        userId: Int,
        channelId: Int,
        permissions: Array<UserPermissions>,
        inviteId: Int?,
        joinedAt: Instant
    ): ChannelUser {
        val id = handle.createUpdate(
            """
            INSERT INTO dbo.ChannelUsers (userId, channelId, permissions, inviteId, joinedAt) 
                values (:userId, :channelId, :permissions, :inviteId, :joinedAt)
            """,
        )
            .bind("userId", userId)
            .bind("channelId", channelId)
            .bind("permissions", ChannelUserMapper.fromUserPermissions(permissions))
            .bind("inviteId", inviteId)
            .bind("joinedAt", joinedAt.epochSeconds)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
        return ChannelUser(
            userId = userId,
            channelId = channelId,
            permissions = permissions,
            inviteId = inviteId,
            lastRead = null,
            joinedAt = joinedAt)
    }

    override fun removeUserFromChannel(userId: Int, channelId: Int) {
        handle.createUpdate(
            """
                delete from dbo.ChannelUsers
                where userId = :userId and channelId = :channelId
            """,
        )
            .bind("userId", userId)
            .bind("channelId", channelId)
            .execute()
    }

    override fun getChannelUser(userId: Int, channelId: Int): ChannelUser? {
        return handle.createQuery(
            "SELECT * FROM dbo.ChannelUsers WHERE userId = :userId and channelId = :channelId")
            .bind("userId", userId)
            .bind("channelId", channelId)
            .mapTo<ChannelUser>()
            .singleOrNull()
    }

    override fun getUserChannels(user: User): Array<ChannelUser> {
        return handle.createQuery(
            """
               SELECT * FROM dbo.channelusers 
               WHERE userId = :userId
            """,
        )
            .bind("userId", user.id)
            .mapTo<ChannelUser>().list().toTypedArray()
    }

    override fun getChannelUsers(channelId: Int): Array<ChannelUser> {
        return handle.createQuery("SELECT * FROM dbo.channelusers WHERE channelId = :channelId",)
            .bind("channelId", channelId)
            .mapTo<ChannelUser>().list().toTypedArray()
    }

    override fun searchChannels(name: String, type: ChannelType): Array<Channel> {
        return handle.createQuery(
            """
                select * from dbo.Channels
                where name LIKE :channelName AND type = :type
            """,
        )
            .bind("channelName", name+"%")
            .bind("type", type)
            .mapTo<Channel>().list().toTypedArray()
    }

    override fun getChannelsByType(private: Int): Array<Channel> {
        return handle.createQuery(
            """
                select * from dbo.Channels
                where private = :private
            """,
        )
            .bind("private", private)
            .mapTo<Channel>().list().toTypedArray()
    }

    override fun updateLastMessage(channelId: Int, messageId: Int) {
        handle.createUpdate(
            """
                update dbo.channels set lastMessage = :messageId where id = :channelId
            """,
        )
            .bind("messageId", messageId)
            .bind("channelId", channelId)
            .execute()
    }


    override fun updateOwner(channelId: Int, userId: Int) {
        handle.createUpdate(
            """
                update dbo.channels set ownerId = :userId where id = :channelId
            """,
        )
            .bind("userId", userId)
            .bind("channelId", channelId)
            .execute()
    }

    override fun updatePermissions(channelId: Int, userId: Int, perms: Array<UserPermissions>) {
        handle.createUpdate(
            """
                update dbo.ChannelUsers set permissions = :perms WHERE
                 userId = :userId AND
                 channelId = :channelId
            """,
        )
            .bind("perms", ChannelUserMapper.fromUserPermissions(perms))
            .bind("userId", userId)
            .bind("channelId", channelId)
            .execute()
    }
}