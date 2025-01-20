package dawchat.http.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import dawchat.domain.channel.ChannelType
import dawchat.domain.channel.UserPermissions
import dawchat.domain.message.MessageTypes


class MessageEventOutputModel(
    val channelId: Int,
    val channelName: String,
    val messageId: Int,
    val userId: Int,
    val username: String,
    @JsonSerialize(using = MessTypeSerializer::class)
    @JsonDeserialize(using = MessTypeDeserializer::class)
    val type: MessageTypes,
    val content: String?,
    val timestamp: Long
)

class ChannelRenameEventOutputModel(
    val channelId: Int,
    val channelName: String
)

class JoinChannelEventOutputModel(
    val channelId: Int,
    val channelName: String,

    @JsonSerialize(using = ChannelTypeSerializer::class)
    @JsonDeserialize(using = ChannelTypeDeserializer::class)
    val channelType: ChannelType,

    @JsonSerialize(using = UserPermsSerializer::class)
    @JsonDeserialize(using = UserPermsDeserializer::class)
    val channelPerms: Array<UserPermissions>,
    val channelOwner: Int,
    val lastMessage: Int,
    val lastRead: Int,
    val joinedAt: Long
)

class LeftChannelEventModel(
    val channelId: Int
)

class RemovedFromChannelEventModel(
    val channelId: Int
)