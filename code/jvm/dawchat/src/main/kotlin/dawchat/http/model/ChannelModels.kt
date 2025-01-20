package dawchat.http.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import dawchat.domain.channel.ChannelType
import dawchat.domain.channel.UserPermissions


data class ChannelUserOutputModel(
    val userId: Int,
    val username: String,

    @JsonSerialize(using = UserPermsSerializer::class)
    @JsonDeserialize(using = UserPermsDeserializer::class)
    val userPerms: Array<UserPermissions>,
    val joinedAt: Long
)

class UserChannelOutputModel(
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

class ChannelCreateInputModel(
    val channelName: String,

    @JsonSerialize(using = ChannelTypeSerializer::class)
    @JsonDeserialize(using = ChannelTypeDeserializer::class)
    val channelType: ChannelType
)

class LeaveChannelInputModel(
    val channelId: Int,
    val newOwner: Int?
)

data class RenameChannelInputModel(
    val channelId: Int,
    val name: String
)


data class JoinChannelInputModel(
    val channelId: Int,
    val inviteId: Int?
)

class SearchChannelResult(
    val channelId: Int,
    val channelName: String,

    @JsonSerialize(using = ChannelTypeSerializer::class)
    @JsonDeserialize(using = ChannelTypeDeserializer::class)
    val channelType: ChannelType
)

class SearchChannelOutputModel(
    val results: Array<SearchChannelResult>
)

class UserChannelsOutputModel(
    val channels: Array<UserChannelOutputModel>
)

class ChannelUsersOutputModel(
    val users: Array<ChannelUserOutputModel>
)

class RemoveUsersInputModel(
    val channelId: Int,
    val toRemove: Array<Int>
)