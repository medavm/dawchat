package dawchat.domain.user

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

interface EventEmitter {

    val sseEmitter: SseEmitter

    fun emit(event: Event)

    fun onCompletion(callback: () -> Unit)

    fun onError(callback: (Throwable) -> Unit)
}