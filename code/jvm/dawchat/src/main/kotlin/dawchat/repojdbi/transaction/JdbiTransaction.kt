package dawchat.repojdbi.transaction

import dawchat.repo.ChannelRepo
import dawchat.repo.InvitesRepo
import dawchat.repo.MessagesRepo
import dawchat.repo.transaction.Transaction
import dawchat.repo.UsersRepo
import dawchat.repojdbi.JdbiChannelRepo
import dawchat.repojdbi.JdbiInvitesRepo
import dawchat.repojdbi.JdbiMessagesRepo
import dawchat.repojdbi.JdbiUsersRepo
import org.jdbi.v3.core.Handle


class JdbiTransaction(
    private val handle: Handle,
) : Transaction {
    override val usersRepo: UsersRepo = JdbiUsersRepo(handle)
    override val channelsRepo: ChannelRepo = JdbiChannelRepo(handle)
    override val invitesRepo: InvitesRepo = JdbiInvitesRepo(handle)
    override val messagesRepo: MessagesRepo = JdbiMessagesRepo(handle)
    override fun rollback() {
        handle.rollback()
    }
}