package dawchat.services



import dawchat.domain.user.AuthenticatedUser
import dawchat.domain.user.Event
import dawchat.domain.user.EventEmitter
import dawchat.repo.transaction.TransactionManager
import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import java.util.LinkedList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class SSEEvent(
    val ev: Event,
    val recipients: Array<Int>
)




open class ThreadSafeCounter(
    startValue: Int
){
    private val lock: Lock = ReentrantLock()
    open var value: Int = startValue
        set(value) {
            lock.withLock {
                field = value
            }
        }
}

@Service
class SseManager(
    private val transactionManager: TransactionManager,
) {

    private val events =    LinkedBlockingQueue<SSEEvent>()
    private val users   =   HashMap<Int, MutableMap<String, EventEmitter>>()

    private var userCounter =       ThreadSafeCounter(0)
    private var listenersCounter =  ThreadSafeCounter(0)


    init {

        GlobalScope.launch{
            while (true) { //TODO
                try {
                    while (!events.isEmpty())
                        dispatchEvent(events.poll())
                }catch (e: Exception){
                    //TODO remove listener
                    e.printStackTrace()
                }

                delay(100)
            }
        }

    }


    private fun dispatchEvent(event: SSEEvent): Boolean {

        var sentCounter = 0
        for(userId in event.recipients){
            var userListeners: MutableMap<String, EventEmitter>?
            synchronized(users){
                userListeners = users[userId]
            }

            if(userListeners!=null){
                synchronized(userListeners!!){
                    userListeners!!.values.forEach{ ee ->
                        ee.emit(event.ev)
                        sentCounter++
                    }
                }
            }
        }

        return sentCounter > 0
    }

    fun addListener(authenticatedUser: AuthenticatedUser, eventEmitter: EventEmitter) {
        val userId = authenticatedUser.user.id
        val userToken = authenticatedUser.token.tokenValidationInfo.value

        var userListeners: MutableMap<String, EventEmitter>
        synchronized(users) {
            if (!users.containsKey(userId)) {
                users[userId] = LinkedHashMap()
                userCounter.value++
            }

            userListeners = users[userId]!!
        }

        synchronized(userListeners) {
            if(!userListeners.containsKey(userToken))
                listenersCounter.value++

            userListeners[userToken]?.sseEmitter?.complete()
            userListeners[userToken] = eventEmitter
        }

    }

    fun removeListener(authenticatedUser: AuthenticatedUser, eventEmitter: EventEmitter){
        val userId = authenticatedUser.user.id
        val userToken = authenticatedUser.token.tokenValidationInfo.value
        synchronized(users) {
            users[userId]?.let {
                synchronized(it){
                    if(it.containsKey(userToken)){
                        it[userToken]?.sseEmitter?.complete()
                        it.remove(userToken)
                    }
                }
            }
        }
    }

    fun queueEvent(event: SSEEvent?){
        if(event != null)
            events.put(event)
    }

}