package dawchat.http.model


import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import dawchat.domain.channel.ChannelType
import dawchat.domain.channel.UserPermissions

class CreateInviteInputModel (
    val channelId: Int,
    val username: String,

    @JsonSerialize(using = UserPermsSerializer::class)
    @JsonDeserialize(using = UserPermsDeserializer::class)
    val invitePerms: Array<UserPermissions>
)

class CreateInviteOutputModel(
    val inviteId: Int,
    val senderId: Int,
    val recipientId: Int,
    val channelId: Int,

    @JsonSerialize(using = UserPermsSerializer::class)
    @JsonDeserialize(using = UserPermsDeserializer::class)
    val invitePerms: Array<UserPermissions>,
    val timestamp: Long
)

class ChannelInviteModel(
    val inviteId: Int,
    val senderId: Int,
    val senderName: String,
    val recipientId: Int,
    val recipientName: String,
    val channelId: Int,
    val channelName: String,

    @JsonSerialize(using = ChannelTypeSerializer::class)
    @JsonDeserialize(using = ChannelTypeDeserializer::class)
    val channelType: ChannelType,

    @JsonSerialize(using = UserPermsSerializer::class)
    @JsonDeserialize(using = UserPermsDeserializer::class)
    val invitePerms: Array<UserPermissions>,
    val timestamp: Long
)


class ChannelInvitesOutputModel(
    val invites: Array<ChannelInviteModel>
)

class RemoveChannelInvitesInputModel(
    val toRemove: Array<Int>
)