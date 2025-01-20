package dawchat.http

import dawchat.http.model.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertEquals
import kotlin.test.fail

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTests {
    // One of the very few places where we use property injection
    @LocalServerPort
    var port: Int = 0

    val perms_w_i = arrayOf(
        HttpTestUtils.Perms.Read,
        HttpTestUtils.Perms.Write,
        HttpTestUtils.Perms.Invite,
    )



    @Test
    fun `can create user with invite`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)

        val invite = HttpTestUtils.createUserInvite(client, user1.token)
            .expectResult(HttpStatus.CREATED, UserInviteOutputModel::class.java)

        val username = HttpTestUtils.randomUsername()
        val password = "password123"
        val user3 = HttpTestUtils.createUser(client, username, password, invite.token)
            .expectResult(HttpStatus.CREATED, UserInfoOutputModel::class.java)

    }

    @Test
    fun `cannot create user without invite`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val username = HttpTestUtils.randomUsername()
        val password = "password123"
        val user = HttpTestUtils.createUser(client, username, password, "invalidinvite")
            .expectResult(HttpStatus.FORBIDDEN)

    }

    @Test
    fun `cannot use the same user invite twice`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)

        val invite = HttpTestUtils.createUserInvite(client, user1.token)
            .expectResult(HttpStatus.CREATED, UserInviteOutputModel::class.java)

        val username = HttpTestUtils.randomUsername()
        val password = "password123"
        val user3 = HttpTestUtils.createUser(client, username, password, invite.token)
            .expectResult(HttpStatus.CREATED, UserInfoOutputModel::class.java)

        val user4 = HttpTestUtils.createUser(client, username, password, invite.token)
            .expectResult(HttpStatus.FORBIDDEN, ErrorResponseModel::class.java)
        assertEquals(HttpErrors.InvalidUserInvite.type, user4.type)

    }

    @Test
    fun `can't create invalid user`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val invite = HttpTestUtils.createUserInvite(client, user1.token)
            .expectResult(HttpStatus.CREATED, UserInviteOutputModel::class.java)

        val username = "a"
        val password = "b"

        val user3 = HttpTestUtils.createUser(client, username, password, invite.token)
            .expectResult(HttpStatus.BAD_REQUEST, ErrorResponseModel::class.java)

    }

    @Test
    fun `can't create existent user`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val invite = HttpTestUtils.createUserInvite(client, user1.token)
            .expectResult(HttpStatus.CREATED, UserInviteOutputModel::class.java)

        val username = HttpTestUtils.randomUsername()
        val password = "password123"
        val user3 = HttpTestUtils.createUser(client, username, password, invite.token)
            .expectResult(HttpStatus.CREATED, UserInfoOutputModel::class.java)

        val invite2 = HttpTestUtils.createUserInvite(client, user1.token)
            .expectResult(HttpStatus.CREATED, UserInviteOutputModel::class.java)

        val user4 = HttpTestUtils.createUser(client, username, password, invite2.token)
            .expectResult(HttpStatus.CONFLICT)

    }

    @Test
    fun `can retrieve user info`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val info = HttpTestUtils.userInfo(client, user1.token)
            .expectResult(HttpStatus.OK, UserInfoOutputModel::class.java)

        assertEquals(user1.userId, info.userId)
        assertEquals(user1.username, info.username)
    }

    @Test
    fun `can create user, login and logout`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val info = HttpTestUtils.userInfo(client, user1.token)
            .expectResult(HttpStatus.OK, UserInfoOutputModel::class.java)

        HttpTestUtils.logout(client, user1.token)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.userInfo(client, user1.token)
            .expectResult(HttpStatus.UNAUTHORIZED)

    }

    @Test
    fun `can't login with invalid user`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val invite = HttpTestUtils.createUserInvite(client, user1.token)
            .expectResult(HttpStatus.CREATED, UserInviteOutputModel::class.java)

        val res = HttpTestUtils.login(client, HttpTestUtils.randomChannelName(), "invalidpassord")
            .expectResult(HttpStatus.UNAUTHORIZED, ErrorResponseModel::class.java)
        assertEquals(HttpErrors.InvalidCredentials.type, res.type)

    }

    @Test
    fun `can't logout without authorization`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        HttpTestUtils.logout(client, "invalid")
            .expectResult(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `can get user channels`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val user3 = HttpTestUtils.createRandomUserAndLogin(client)

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val channel2 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val channel3 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, null)
            .expectResult(HttpStatus.OK)
        HttpTestUtils.joinChannel(client, user2.token, channel2.channelId, null)
            .expectResult(HttpStatus.OK)
        HttpTestUtils.joinChannel(client, user2.token, channel3.channelId, null)
            .expectResult(HttpStatus.OK)

        val userChannels = HttpTestUtils.userChannels(client, user2.token)
            .expectResult(HttpStatus.OK, UserChannelsOutputModel::class.java)

        assertEquals(3, userChannels.channels.size)

    }


}