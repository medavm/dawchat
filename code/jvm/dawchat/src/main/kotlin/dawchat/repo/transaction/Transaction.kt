package dawchat.repo.transaction

import dawchat.repo.ChannelRepo
import dawchat.repo.InvitesRepo
import dawchat.repo.MessagesRepo
import dawchat.repo.UsersRepo


interface Transaction {
    val usersRepo: UsersRepo
    val channelsRepo: ChannelRepo
    val invitesRepo: InvitesRepo
    val messagesRepo: MessagesRepo
    // other repository types
    fun rollback()
}