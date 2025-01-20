package dawchat.services

import dawchat.domain.channel.ChannelDomain
import dawchat.domain.user.EventEmitter
import dawchat.domain.user.UsersDomain
import dawchat.repo.transaction.TransactionManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Component
class ServiceUtils(
    val transactionManager: TransactionManager,
    val usersDomain: UsersDomain,
    val channelDomain : ChannelDomain,
    val eventEmitter: SseManager,
    val clock: Clock
) {

    //val joinChannelLock = ReentrantLock()

}