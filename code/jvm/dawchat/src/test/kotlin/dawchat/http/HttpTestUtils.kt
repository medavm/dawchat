package dawchat.http

import dawchat.http.model.CreateInviteInputModel
import dawchat.http.model.LoadMessagesInputModel
import dawchat.http.model.UserInfoOutputModel
import dawchat.http.model.UserInviteOutputModel
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.fail
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import kotlin.math.abs
import kotlin.random.Random


class HttpTestUtils {

    class TestUser(
        val userId: Int,
        val username: String,
        val password: String,
        val token: String,
    )

    class Perms{
        companion object{
            const val Read =        "read"
            const val Write =       "write"
            const val Invite =      "invite"
            const val Rename =      "rename"
            const val RemoveUsers = "remove-users"

            fun all() = arrayOf(
                Read, Write, Invite, Read, RemoveUsers
            )
        }
    }

    class ChannelType{
        companion object{
            const val Public =        "public"
            const val Private =       "private"
        }
    }

    class MessageType{
        companion object{
            const val ChannelCreated =          "channel-create"
            const val ChannelRenamed =          "channel-rename"
            const val Text =                    "text"
        }
    }


    companion object{

        const val STARTUSER_ID = 1
        const val STARTUSER_NAME = "startuser"
        const val STARTUSER_PASSW = "password123"

        var startUser: TestUser? = null


        class RequestResult(
            private val exchange: WebTestClient.ResponseSpec,
        ){

            fun expectResult(status: HttpStatus){
                exchange.expectStatus().isEqualTo(status)
                    .expectBody<Any>()
                    .returnResult()
                    .responseBody
            }

            fun <T> expectResult(status: HttpStatus, c: Class<T>): T{
                return exchange.expectStatus().isEqualTo(status)
                    .expectBody(c)
                    .returnResult()
                    .responseBody!!
            }

            fun expectCookie(status: HttpStatus, name: String): String{
                var t = ""
                exchange.expectStatus().isEqualTo(status)
                    .expectBody<Any>()
                    .consumeWith{ resp ->

                        val cookieValue: String? = resp.responseHeaders
                            .getFirst("Set-Cookie") // Or if you are expecting a cookie by name

                        // Assert or log the cookie
                        assertNotNull(cookieValue, "Cookie is not found")

                        t = resp.responseCookies.getFirst(name)?.value
                            ?: fail("Failed to obtain session token")

                    }
                    .returnResult()
                return t
            }
        }

        fun randomUsername(): String{
            return "user-${abs(Random.nextLong())}"
        }

        fun randomChannelName(): String{
            return "channel-${abs(Random.nextLong())}"
        }

        fun createUser(client: WebTestClient, username: String, password: String, token: String): RequestResult{
            return RequestResult(client.post().uri(Uris.User.CREATE)
                .bodyValue(
                    mapOf(
                        "username" to username,
                        "password" to password,
                        "inviteToken" to token
                    ),
                )
                .exchange()
            )
        }

        fun createUserInvite(client: WebTestClient, token: String): RequestResult{
            
            return RequestResult(client.post().uri(Uris.User.CREATE_USER_INVITE)
                .cookie("t", token)
                .exchange()
            )
        }

        fun login(client: WebTestClient, username: String, password: String): RequestResult {
            val res = client.post().uri(Uris.User.LOGIN)
                .bodyValue(
                    mapOf(
                        "username" to username,
                        "password" to password,
                    ),
                )
                .exchange()
            return RequestResult(res)
        }

        fun logout(client: WebTestClient, token: String): RequestResult {
            return RequestResult(client.post().uri(Uris.User.LOGOUT)
                .cookie("t", token)
                .exchange()
            )
        }

        fun userInfo(client: WebTestClient, token: String): RequestResult{
            return RequestResult(
                client.get().uri(Uris.User.INFO)
                    .cookie("t", token)
                    .exchange()
            )
        }

        fun createRandomUserAndLogin(client: WebTestClient): TestUser {

            if(startUser==null){
                val startUserToken = login(client, STARTUSER_NAME, STARTUSER_PASSW)
                    .expectCookie(HttpStatus.OK, "t")
                startUser = TestUser(STARTUSER_ID, STARTUSER_NAME, STARTUSER_PASSW, startUserToken)
            }

            val username1 = randomUsername()
            val password1 = "password123"
            val invite = createUserInvite(client, startUser!!.token)
                .expectResult(HttpStatus.CREATED, UserInviteOutputModel::class.java)
            val user1 = createUser(client, username1, password1, invite.token)
                .expectResult(HttpStatus.CREATED, UserInfoOutputModel::class.java)
            val token = login(client, username1, password1)
                .expectCookie(HttpStatus.OK, "t")
            return TestUser(user1.userId, username1, password1, token)
        }

        fun createChannel(client: WebTestClient, token: String, channelName: String, type: String): RequestResult  {
            return RequestResult(
                client.post().uri(Uris.Channel.CREATE)
                    .cookie("t", token)
                    .bodyValue(
                        mapOf(
                            "channelName" to channelName,
                            "channelType" to type
                        ),
                    )
                    .exchange()
            )
        }

        fun renameChannel(client: WebTestClient, token: String, channelId: Int, name: String): RequestResult  {
            return RequestResult(
                client.post().uri(Uris.Channel.RENAME)
                    .cookie("t", token)
                    .bodyValue(
                        mapOf(
                            "channelId" to channelId,
                            "name" to name
                        ),
                    )
                    .exchange()
            )
        }

        fun joinChannel(client: WebTestClient, token: String, channelId: Int, inviteId: Int?): RequestResult  {
            return RequestResult(
                client.post().uri(Uris.Channel.JOIN)
                .cookie("t", token)
                .bodyValue(
                    mapOf(
                        "channelId" to channelId,
                        "inviteId" to inviteId
                    ),
                )
                .exchange()
            )
        }

        fun searchChannel(client: WebTestClient, token: String, keyword: String): RequestResult{
            return  RequestResult(
                client.get()
                    .uri { it.path(Uris.Channel.SEARCH).queryParam("keyword", keyword).build() }
                    .cookie("t", token)
                    .exchange()
            )
        }

        fun leaveChannel(client: WebTestClient, token: String, channelId: Int, newOwner: Int? = null): RequestResult {
            return RequestResult(
                client.post().uri(Uris.Channel.LEAVE)
                    .cookie("t", token)
                    .bodyValue(
                        mapOf(
                            "channelId" to channelId,
                            "newOwner" to newOwner
                        ),
                    )
                    .exchange()
            )
        }

        fun userChannels(client: WebTestClient, token: String): RequestResult{
            return RequestResult(
                client.get().uri(Uris.User.CHANNELS)
                    .cookie("t", token)
                    .exchange()
            )
        }

        fun channelUsers(client: WebTestClient, token: String, channelId: Int): RequestResult{
            return RequestResult(
                client.get()
                    .uri {
                        it.path(Uris.Channel.USERS)
                            .queryParam("channelId", channelId)
                            .build()
                    }
                    .cookie("t", token)
                    .exchange()
            )
        }

        fun createInvite2(client: WebTestClient, token: String, channelId: Int, recipient: String, permissions: Array<String>): RequestResult {
            return RequestResult(
                client.post().uri(Uris.Channel.INVITE)
                .cookie("t", token)
                .bodyValue(
                    mapOf(
                        "channelId" to channelId,
                        "username" to recipient,
                        "invitePerms" to permissions
                    ),
                )
                .exchange()
            )
        }

        fun userChannelInvites(client: WebTestClient, token: String): RequestResult{
            return RequestResult(
                client.get().uri(Uris.User.CHANNEL_INVITES)
                    .cookie("t", token)
                    .exchange()
            )
        }

        fun userChannelInvites(client: WebTestClient, token: String, channelId: Int): RequestResult{
            return RequestResult(
                client.get()
                    .uri {
                        it.path(Uris.Channel.INVITES)
                            .queryParam("channelId", channelId)
                            .build()
                    }
                    .cookie("t", token)
                    .exchange()
            )
        }

        fun clearChannelInvites(client: WebTestClient, token: String, inviteIds: Array<Int>): RequestResult{
            return RequestResult(
                client.post().uri(Uris.User.CLEAR_CHANNEL_INVITES)
                    .cookie("t", token)
                    .bodyValue(
                        mapOf(
                            "toRemove" to inviteIds,
                        ),
                    )
                    .exchange()
            )
        }

        fun removeChannelInvites(client: WebTestClient, token: String, inviteIds: Array<Int>): RequestResult{
            return RequestResult(
                client.post().uri(Uris.Channel.REMOVE_INVITES)
                    .cookie("t", token)
                    .bodyValue(
                        mapOf(
                            "toRemove" to inviteIds,
                        ),
                    )
                    .exchange()
            )
        }

        fun removeChannelUsers(client: WebTestClient, token: String, channelId: Int, userIds: Array<Int>): RequestResult{
            return RequestResult(
                client.post().uri(Uris.Channel.REMOVE_USERS)
                    .cookie("t", token)
                    .bodyValue(
                        mapOf(
                            "channelId" to channelId,
                            "toRemove" to userIds
                        ),
                    )
                    .exchange()
            )
        }

        fun createMessage(client: WebTestClient, token: String, channelId: Int, type: String, content: String): RequestResult {
            return RequestResult(client.post().uri(Uris.Channel.Message.SEND)
                .cookie("t", token)
                .bodyValue(
                    mapOf(
                        "channelId" to channelId,
                        "type" to type,
                        "content" to content
                    ),
                )
                .exchange()
            )
        }

        fun getMessages(
            client: WebTestClient, token: String, channelId: Int,
            filter: Array<String>? = null, lastMessage: Int? = null, limit: Int? = null,
        ): RequestResult {
            return RequestResult(client.post().uri(Uris.Channel.Message.GET)
                .cookie("t", token)
                .bodyValue(
                    mapOf(
                        "load" to arrayOf(
                            mapOf(
                                "channelId" to channelId,
                                "lastMessage" to lastMessage,
                                "limit" to limit,
                                "filter" to filter
                            )
                        )
                    ),
                )
                .exchange()
            )
        }

        fun getMessages(client: WebTestClient, token: String, channels: LoadMessagesInputModel): RequestResult{

            return RequestResult(client.post().uri(Uris.Channel.Message.GET)
                .cookie("t", token)
                .bodyValue(
                    channels,
                )
                .exchange()
            )
        }

    }
}