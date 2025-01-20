package dawchat.repo

import dawchat.domain.channel.*
import dawchat.domain.message.Message
import dawchat.domain.user.User
import kotlinx.datetime.Instant

interface ChannelRepo {

    fun createChannel(user: User, name: String, type: ChannelType, createdAt: Instant) : Channel

    fun getChannel(name: String): Channel?

    fun getChannel(channelId: Int): Channel?

    fun renameChannel(channelId: Int, name: String)

    fun addUserToChannel(userId: Int, channelId: Int, permissions: Array<UserPermissions>, inviteId: Int?, joinedAt: Instant): ChannelUser

    fun getChannelUser(userId: Int, channelId: Int): ChannelUser?

    fun removeUserFromChannel(userId: Int, channelId: Int)

    fun getUserChannels(user: User): Array<ChannelUser>

    fun getChannelUsers(channelId: Int): Array<ChannelUser>

    fun searchChannels(name: String, type: ChannelType): Array<Channel>

    fun getChannelsByType(private: Int): Array<Channel>

    fun updateLastMessage(channelId: Int, messageId: Int)

    fun updateOwner(channelId: Int, userId: Int)

    fun updatePermissions(channelId: Int, userId: Int, perms: Array<UserPermissions>)

}