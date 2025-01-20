package dawchat.http.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import dawchat.domain.message.MessageTypes

class MessageOutputModel(
    val messageId: Int,
    val userId: Int,
    val username: String,

    @JsonSerialize(using = MessTypeSerializer::class)
    @JsonDeserialize(using = MessTypeDeserializer::class)
    val type: MessageTypes,
    val content: String?,
    val timestamp: Long
)

data class CreateMessageInputModel(
    val channelId: Int,

    @JsonSerialize(using = MessTypeSerializer::class)
    @JsonDeserialize(using = MessTypeDeserializer::class)
    val type: MessageTypes,
    val content: String
)

class ChannelMessagesInputModel(
    val channelId: Int,
    val lastMessage: Int?,
    val limit: Int?,

    @JsonSerialize(using = MessTypesSerializer::class)
    @JsonDeserialize(using = MessTypesDeserializer::class)
    val filter: Array<MessageTypes>?
)

class ChannelMessagesOutputModel(
    val channelId: Int,
    val channelName: String,
    val channelMessages: Array<MessageOutputModel>
)

class LoadMessagesInputModel(
    val load: Array<ChannelMessagesInputModel>
)

 class LoadMessagesOutputModel(
    val results: Map<Int, ChannelMessagesOutputModel>
)
