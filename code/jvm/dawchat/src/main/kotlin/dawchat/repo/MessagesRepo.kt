package dawchat.repo

import dawchat.domain.message.Message
import dawchat.domain.message.MessageTypes
import kotlinx.datetime.Instant

interface MessagesRepo {





    fun createMessage(userId: Int, channelId: Int, type: MessageTypes, content: String?, createdAt: Instant): Message

    fun getChannelMessages(channelId: Int, types: Array<MessageTypes>, fromId: Int, limit: Int): Array<Message>

    fun getMessage(messageId: Int): Message?

    fun updateLastRead(userId: Int, channelId: Int, messageId: Int)

    //@SqlQuery("SELECT * FROM users WHERE id = :userId")
    //fun findById(@Bind("userId") userId: Int): User
}