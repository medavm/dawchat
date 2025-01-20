package dawchat.services

import dawchat.domain.channel.ChannelInvite
import dawchat.domain.channel.ChannelType
import dawchat.domain.channel.InviteStatus
import dawchat.domain.channel.UserPermissions
import dawchat.domain.user.AuthenticatedUser
import dawchat.repo.transaction.Transaction
import kotlinx.datetime.Instant
import org.springframework.stereotype.Service
import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class ChannelInviteInfo(
    val inviteId: Int,
    val senderId: Int,
    val senderName: String,
    val recipientId: Int,
    val recipientName: String,
    val channelId: Int,
    val channelName: String,
    val channelType: ChannelType,
    val channelPermissions: Array<UserPermissions>,
    val message: String?,
    val status: InviteStatus,
    val createdAt: Instant,
    val resolvedAt: Instant?
)

sealed class CreateChannelInviteError{
    data object ChannelNotFound:        CreateChannelInviteError()
    data object NoPermission:           CreateChannelInviteError()
    data object UserNotFound:           CreateChannelInviteError()
    data object UserAlreadyInChannel:   CreateChannelInviteError()
    data object UserAlreadyInvited:     CreateChannelInviteError()
}

sealed class RemoveChannelInviteError{
    data object ChannelInviteNotFound:RemoveChannelInviteError()
    data object NoPermission:         RemoveChannelInviteError()
}

sealed class ChannelInvitesError{
    data object ChannelNotFound:        ChannelInvitesError()
    data object NoPermission:           ChannelInvitesError()
}


@Service
class InvitesService (
    val serviceUtils: ServiceUtils
){

    //No more than 1 invite per user...
    val createInviteLock = ReentrantLock()

    fun createChannelInvite(authUser: AuthenticatedUser, channelId: Int, username: String, invitePerms: Array<UserPermissions>):
            ServiceResult<ChannelInvite, CreateChannelInviteError> = createInviteLock.withLock {

        return serviceUtils.transactionManager.run {

            val channel = it.channelsRepo.getChannel(channelId)
                ?: return@run ServiceResult.Error(CreateChannelInviteError.ChannelNotFound)

            val authChUser = it.channelsRepo.getChannelUser(authUser.user.id, channelId)
                ?: return@run ServiceResult.Error(CreateChannelInviteError.NoPermission)

            if(UserPermissions.Invite !in authChUser.permissions
                || !serviceUtils.channelDomain.canGivePermissions(authChUser.permissions, invitePerms))
                return@run ServiceResult.Error(CreateChannelInviteError.NoPermission)

            val invitedUser = it.usersRepo.getUser(username)
                ?: return@run ServiceResult.Error(CreateChannelInviteError.UserNotFound)

            val invitedChUser = it.channelsRepo.getChannelUser(invitedUser.id, channelId)
            if(invitedChUser!=null)
                return@run ServiceResult.Error(CreateChannelInviteError.UserAlreadyInChannel)

            if(it.invitesRepo.getChannelInvite(invitedUser.id, channelId, InviteStatus.Pending) != null)
                return@run ServiceResult.Error(CreateChannelInviteError.UserAlreadyInvited)

            val invite = it.invitesRepo.createChannelInvite(
                senderId = authUser.user.id,
                recipientId = invitedUser.id,
                channelId = channelId,
                permissions = invitePerms,
                message = null, //TODO
                status = InviteStatus.Pending,
                createdAt = serviceUtils.clock.now()
            )

            ServiceResult.Success(invite)
        }

    }

    fun removeChannelInvite(authUser: AuthenticatedUser, inviteId: Int):
            ServiceResult<Boolean, RemoveChannelInviteError>{
        return serviceUtils.transactionManager.run {

            val invite = it.invitesRepo.getChannelInvite(inviteId)
                ?: return@run ServiceResult.Error(RemoveChannelInviteError.ChannelInviteNotFound)

            if(invite.status!=InviteStatus.Pending)
                return@run ServiceResult.Success(true)

            if(invite.recipientId==authUser.user.id){
                it.invitesRepo.updateInviteStatus(inviteId, InviteStatus.Denied, serviceUtils.clock.now())
            }
            else if(invite.senderId==authUser.user.id){
                it.invitesRepo.updateInviteStatus(inviteId, InviteStatus.Revoked, serviceUtils.clock.now())
            }
            else{
                val channelUser = it.channelsRepo.getChannelUser(authUser.user.id, invite.channelId)
                    ?: return@run ServiceResult.Error(RemoveChannelInviteError.NoPermission)

                if(UserPermissions.RemoveUsers !in channelUser.permissions)
                    return@run ServiceResult.Error(RemoveChannelInviteError.NoPermission)

                it.invitesRepo.updateInviteStatus(inviteId, InviteStatus.Revoked, serviceUtils.clock.now())
            }

            return@run ServiceResult.Success(true)
        }

    }

    fun removeChannelInvites(authUser: AuthenticatedUser, inviteIds: Array<Int>):
            ServiceResult<Boolean, RemoveChannelInviteError> {
        for (inviteId in inviteIds){
            val res = removeChannelInvite(authUser, inviteId)
            if(res is ServiceResult.Error)
                return res
        }
        return ServiceResult.Success(true)
    }

    private fun getInviteInfo(trans: Transaction, invite: ChannelInvite): ChannelInviteInfo{
        val sender = trans.usersRepo.getUser(invite.senderId)
            ?: TODO()
        val recipient = trans.usersRepo.getUser(invite.recipientId)
            ?: TODO()
        val channel = trans.channelsRepo.getChannel(invite.channelId)
            ?: TODO()
        return ChannelInviteInfo(
            inviteId = invite.inviteId,
            senderId = sender.id,
            senderName = sender.username,
            recipientId = recipient.id,
            recipientName = recipient.username,
            channelId = channel.id,
            channelName = channel.name,
            channelType = channel.type,
            channelPermissions = invite.permissions,
            message = invite.message,
            status = invite.status,
            createdAt = invite.createdAt,
            resolvedAt = invite.resolvedAt
        )
    }

    fun channelInvites(authUser: AuthenticatedUser, channelId: Int):
            ServiceResult<Array<ChannelInviteInfo>, ChannelInvitesError> {
        return serviceUtils.transactionManager.run {

            val channel = it.channelsRepo.getChannel(channelId)
                ?: return@run ServiceResult.Error(ChannelInvitesError.ChannelNotFound)

            val channelUser = it.channelsRepo.getChannelUser(authUser.user.id, channel.id)
                ?: return@run ServiceResult.Error(ChannelInvitesError.NoPermission)

            val invites = it.invitesRepo.getChannelInvites(channel.id, InviteStatus.Pending)
            val res = LinkedList<ChannelInviteInfo>()
            for(invite in invites)
                res.add(getInviteInfo(it, invite))

            return@run ServiceResult.Success(res.toTypedArray())
        }
    }

    fun userInvites(authUser: AuthenticatedUser):
            Array<ChannelInviteInfo>{
        return serviceUtils.transactionManager.run {
            val res = LinkedList<ChannelInviteInfo>()
            val invites = it.invitesRepo.getUserInvites(authUser.user.id, InviteStatus.Pending)
            for(invite in invites)
                res.add(getInviteInfo(it, invite))

            return@run res.toTypedArray()
        }
    }





}