package dawchat.repojdbi

import dawchat.domain.channel.Channel
import dawchat.domain.message.Message
import dawchat.domain.message.MessageTypes
import dawchat.repo.MessagesRepo
import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiMessagesRepo(
    private val handle: Handle
): MessagesRepo {



    override fun createMessage(userId: Int, channelId: Int, type: MessageTypes, content: String?, createdAt: Instant): Message {
        val id = handle.createUpdate(
            """
            INSERT INTO dbo.Messages (userId, channelId, type, content, createdAt) 
                values (:userId, :channelId, :type, :content, :createdAt)
            """,
        )
            .bind("userId", userId)
            .bind("channelId", channelId)
            .bind("type", type)
            .bind("content", content)
            .bind("createdAt", createdAt.epochSeconds)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

        return Message(
            messageId = id,
            userId = userId,
            channelId = channelId,
            type = type,
            content = content,
            timestamp = createdAt.epochSeconds
        )
    }

    override fun getChannelMessages(channelId: Int, types: Array<MessageTypes>, fromId: Int, limit: Int): Array<Message> {
        return handle.createQuery(
            """
                SELECT * FROM dbo.Messages
                WHERE channelId = :channelId 
                AND id < :fromId 
                AND type in (<types>) 
                ORDER BY id DESC LIMIT :limit
            """,
        )
            .bind("channelId", channelId)
            .bind("fromId", fromId)
            .bindList("types", types.toList())
            .bind("limit", limit)
            .map { rs, col, ctx -> Message(
                messageId = rs.getInt("id"),
                userId = rs.getInt("userId"),
                channelId = rs.getInt("channelId"),
                type = MessageTypes.valueOf(rs.getString("type")),
                content = rs.getString("content"),
                timestamp = rs.getLong("createdAt")
            )}
            .list().toTypedArray()
            //.mapTo<Message>().list().toTypedArray()
    }


    override fun getMessage(messageId: Int): Message? {
        return handle.createQuery("SELECT * FROM dbo.Messages WHERE id = :messageId")
            .bind("messageId", messageId)
            .map { rs, col, ctx -> Message(
                messageId = rs.getInt("id"),
                userId = rs.getInt("userId"),
                channelId = rs.getInt("channelId"),
                type = MessageTypes.valueOf(rs.getString("type")),
                content = rs.getString("content"),
                timestamp = rs.getLong("createdAt")
            )}
            .singleOrNull()
    }

    override fun updateLastRead(userId: Int, channelId: Int, messageId: Int) {
        handle.createUpdate(
            """
                UPDATE dbo.ChannelUsers SET lastRead = :messageId WHERE userId = :userId AND channelId = :channelId
            """,
        )
            .bind("messageId", messageId)
            .bind("userId", userId)
            .bind("channelId", channelId)
            .execute()
    }










}