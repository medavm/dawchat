package dawchat.http


import dawchat.domain.user.Event
import dawchat.domain.user.EventEmitter
import dawchat.http.model.*
import org.springframework.http.MediaType
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

class HttpEventEmitter(
    override val sseEmitter: SseEmitter
): EventEmitter {
    override fun emit(ev: Event) {
        val event = when (ev) {

            is Event.ChannelMessage ->{
                val resp = MessageEventOutputModel(
                    channelId = ev.d.channelId,
                    channelName = ev.d.channelName,
                    messageId = ev.d.messageId,
                    userId =    ev.d.userId,
                    username =  ev.d.userName,
                    type =      ev.d.type,
                    content   = ev.d.content,
                    timestamp = ev.d.timestamp
                )

                SseEmitter.event()
                    .id(ev.d.messageId.toString())
                    .name("channel-message")
                    .data(resp, MediaType.APPLICATION_JSON)
            }

            is Event.ChannelRename -> {
                val resp = ChannelRenameEventOutputModel(
                    channelId = ev.d.channel.id,
                    channelName = ev.d.channel.name,
                )
                SseEmitter.event()
                    .id(ev.d.channel.id.toString())
                    .name("channel-rename")
                    .data(resp, MediaType.APPLICATION_JSON)
            }

            is Event.JoinChannel -> {
                val resp = JoinChannelEventOutputModel(
                    channelId = ev.d.channelId,
                    channelName = ev.d.channelName,
                    channelType = ev.d.channelType,
                    channelPerms = ev.d.permissions,
                    channelOwner = ev.d.ownerId,
                    lastMessage = ev.d.lastMessage ?: 0,
                    lastRead = ev.d.lastRead ?: 0,
                    joinedAt = ev.d.joinedAt.epochSeconds
                )

                SseEmitter.event()
                    .id(ev.d.channelId.toString())
                    .name("channel-join")
                    .data(resp, MediaType.APPLICATION_JSON)

            }

            is Event.LeftChannel -> {
                val resp = LeftChannelEventModel(
                    channelId = ev.d.channel.id
                )

                SseEmitter.event()
                    .id(ev.d.channel.id.toString())
                    .name("channel-leave")
                    .data(resp, MediaType.APPLICATION_JSON)
            }

            is Event.RemovedFromChannel -> {
                val resp = RemovedFromChannelEventModel(
                    channelId = ev.d.channel.id
                )

                SseEmitter.event()
                    .id(ev.d.channel.id.toString())
                    .name("channel-remove")
                    .data(resp, MediaType.APPLICATION_JSON)
            }
        }

        try {
            sseEmitter.send(event)
        } catch (ex: IllegalStateException) {
            sseEmitter.complete()
            println("Error sending event: ${ex.message}")
        }

    }

    override fun onCompletion(callback: () -> Unit) {
        sseEmitter.onCompletion(callback)
    }

    override fun onError(callback: (Throwable) -> Unit) {
        sseEmitter.onError(callback)
    }

}