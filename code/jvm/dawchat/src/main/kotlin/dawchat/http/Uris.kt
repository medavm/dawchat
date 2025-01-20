package dawchat.http

import org.springframework.web.util.UriTemplate
import java.net.URI

data object Uris {
    const val PREFIX = "/api"

    const val STATUS = "$PREFIX/status"

    object User {
        const val CREATE =                  "$PREFIX/user/create"
        const val LOGIN =                   "$PREFIX/user/login"
        const val LOGOUT =                  "$PREFIX/user/logout"
        const val SEARCH =                  "$PREFIX/user/search"
        const val INFO =                    "$PREFIX/user/info"
        const val USER_INVITES =            "$PREFIX/user/invites"
        const val CREATE_USER_INVITE =      "$PREFIX/user/invites/create"
        const val REMOVE_USER_INVITES =     "$PREFIX/user/invites/remove"
        const val CHANNEL_INVITES =         "$PREFIX/user/channel/invites"
        const val CLEAR_CHANNEL_INVITES =   "$PREFIX/user/channel/invites/clear"
        const val CHANNELS =                "$PREFIX/user/channels"
        const val LISTENER =                "$PREFIX/user/listener"

    }

    object Channel{

        const val CREATE =          "$PREFIX/channel/create"
        const val INFO =            "$PREFIX/channel/info"
        const val RENAME =          "$PREFIX/channel/rename"
        const val SEARCH =          "$PREFIX/channel/search"
        const val JOIN =            "$PREFIX/channel/join"
        const val LEAVE =           "$PREFIX/channel/leave"
        const val INVITE =          "$PREFIX/channel/invite"
        const val INVITES =         "$PREFIX/channel/invites"
        const val REMOVE_INVITES =  "$PREFIX/channel/invites/remove"
        const val USERS =           "$PREFIX/channel/users"
        const val REMOVE_USERS=     "$PREFIX/channel/users/remove"
        object Message{
            const val GET =         "$PREFIX/channel/messages"
            const val SEND =        "$PREFIX/channel/messages/create"
            const val READ =        "$PREFIX/channel/messages/read"
        }
    }
}