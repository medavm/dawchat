package dawchat.domain.channel

import dawchat.domain.user.AuthenticatedUser
import dawchat.domain.user.User
import kotlinx.datetime.Instant
import org.springframework.stereotype.Component
import java.util.LinkedList


@Component
class ChannelDomain(
    val channelDomainConfig: ChannelDomainConfig
) {

    fun validateChannelName(name: String): Boolean{
        return name.length >= channelDomainConfig.channelNameMinLen
                && name.length <= channelDomainConfig.channelNameMaxLen
    }

    fun canGivePermissions(sender: Array<UserPermissions>, recipient: Array<UserPermissions>): Boolean{
        for (p in recipient)
            if(p !in sender)
                return false
        return true
    }

    fun defaultJoinPermissions(type: ChannelType): Array<UserPermissions>{
        if(type == ChannelType.Public){
            return arrayOf( //default permissions for public channel
                UserPermissions.Read,
                UserPermissions.Write,
                UserPermissions.Invite
            )
        }

        if(type == ChannelType.Private){
            return arrayOf(

            )
        }
        throw NotImplementedError()
    }

    fun canJoinChannel(user: AuthenticatedUser, channel: Channel, invite: ChannelInvite?): Boolean{
        if (invite != null && invite.channelId == channel.id) {
            return true
        }
        else if(channel.type == ChannelType.Public){
            return true
        }
        return false
    }


}