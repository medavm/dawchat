package dawchat.http

import dawchat.http.model.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InviteTests {

    @LocalServerPort
    var port: Int = 0

    val perms_w_i = arrayOf(
        HttpTestUtils.Perms.Read,
        HttpTestUtils.Perms.Write,
        HttpTestUtils.Perms.Invite,
    )

    @Test
    fun`cannot invite to invalid channel`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)

        HttpTestUtils.createInvite2(client, user1.token, -111, user2.username, perms_w_i)
            .expectResult(HttpStatus.NOT_FOUND)
    }

    @Test
    fun`cannot invite with invalid username`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)


        HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, HttpTestUtils.randomUsername(),
            perms_w_i)
            .expectResult(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `cannot invite myself`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)


        HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user1.username, perms_w_i)
            .expectResult(HttpStatus.CONFLICT)

    }

    @Test
    fun`can create an invite`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)
    }

    @Test
    fun`can use invite to join channel`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)
        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username,
            arrayOf(HttpTestUtils.Perms.Invite))
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite1.inviteId)
            .expectResult(HttpStatus.OK)
    }

    @Test
    fun`cannot use invalid invite to join channel`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, Int.MAX_VALUE)
            .expectResult(HttpStatus.NOT_FOUND)
    }

    @Test
    fun`cannot create invite with different permissions than the sender`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val user3 = HttpTestUtils.createRandomUserAndLogin(client)

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite1 = HttpTestUtils.createInvite2(client, user1.token,
            channel1.channelId,
            user2.username,
            arrayOf(HttpTestUtils.Perms.Invite)
        ).expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite1.inviteId)
            .expectResult(HttpStatus.OK)

        val notAcceptablePermissions = arrayOf(HttpTestUtils.Perms.Write)

        val invite2 = HttpTestUtils.createInvite2(client, user2.token, channel1.channelId, user3.username,
            notAcceptablePermissions)
            .expectResult(HttpStatus.FORBIDDEN, ErrorResponseModel::class.java)
        assertEquals(HttpErrors.NoPermission.type, invite2.type)

        val acceptablePermissions = arrayOf(HttpTestUtils.Perms.Invite)
        val invite3 = HttpTestUtils.createInvite2(client, user2.token, channel1.channelId,
            user3.username, acceptablePermissions)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)
        val invite3Result = client.post().uri(Uris.Channel.INVITE)

    }

    @Test
    fun `can obtain user pending channel invites`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)
        val channel2 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)
        val channel3 = HttpTestUtils.createChannel(client, user3.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId,  user2.username,
            arrayOf(HttpTestUtils.Perms.Invite))
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)
        val invite2 = HttpTestUtils.createInvite2(client, user1.token, channel2.channelId,  user2.username,
            arrayOf(HttpTestUtils.Perms.Invite))
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)
        val invite3 = HttpTestUtils.createInvite2(client, user1.token, channel2.channelId,  user3.username,
            arrayOf(HttpTestUtils.Perms.Invite))
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user3.token, channel2.channelId, invite3.inviteId)
            .expectResult(HttpStatus.OK)

        val invite4 = HttpTestUtils.createInvite2(client, user3.token, channel3.channelId,  user2.username,
            arrayOf(HttpTestUtils.Perms.Invite))
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        val invitesModel = HttpTestUtils.userChannelInvites(client, user2.token)
            .expectResult(HttpStatus.OK, ChannelInvitesOutputModel::class.java)

        assertEquals(3, invitesModel.invites.size)
        val map = HashMap<Int, ChannelInviteModel>()
        for (invite in invitesModel.invites)
            map[invite.inviteId] = invite

        assertTrue(map.containsKey(invite1.inviteId))
        assertTrue(map.containsKey(invite2.inviteId))
        assertTrue(!map.containsKey(invite3.inviteId))
        assertTrue(map.containsKey(invite4.inviteId), "invite")
        assertEquals(invite1.channelId, map[invite1.inviteId]!!.channelId)
        assertEquals(invite2.senderId, map[invite2.inviteId]!!.senderId)
        assertEquals(invite4.timestamp, map[invite4.inviteId]!!.timestamp)

        HttpTestUtils.joinChannel(client, user2.token, invite2.channelId, invite2.inviteId)
            .expectResult(HttpStatus.OK)
        HttpTestUtils.joinChannel(client, user2.token, invite4.channelId, invite4.inviteId)
            .expectResult(HttpStatus.OK)

        val invitesModel2 = HttpTestUtils.userChannelInvites(client, user2.token)
            .expectResult(HttpStatus.OK, ChannelInvitesOutputModel::class.java)

        assertEquals(1, invitesModel2.invites.size)

    }

    @Test
    fun`can invite multiple users to the same channel`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user4 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId,  user2.username,
            arrayOf(HttpTestUtils.Perms.Invite))
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)
        val invite2 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId,  user3.username,
            arrayOf(HttpTestUtils.Perms.Invite))
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)
        val invite3 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId,  user4.username,
            arrayOf(HttpTestUtils.Perms.Write, HttpTestUtils.Perms.Invite))
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

    }

    @Test
    fun`cannot invite the same user to the same channel multiple times`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val perms = arrayOf(HttpTestUtils.Perms.Invite)
        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId,  user2.username, perms)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, perms)
            .expectResult(HttpStatus.CONFLICT, ErrorResponseModel::class.java)

    }

    @Test
    fun `can invite the same user again after he leaves the channel`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user4 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(), HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val perms = arrayOf(HttpTestUtils.Perms.Read, HttpTestUtils.Perms.Write, HttpTestUtils.Perms.Invite)
        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId,  user2.username, perms)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite1.inviteId)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, perms)
            .expectResult(HttpStatus.CONFLICT)

        HttpTestUtils.leaveChannel(client, user2.token, channel1.channelId)
            .expectResult(HttpStatus.OK)

        val invite2 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId,  user2.username, perms)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite1.inviteId)
            .expectResult(HttpStatus.FORBIDDEN)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite2.inviteId)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite2.inviteId)
            .expectResult(HttpStatus.CONFLICT)

        HttpTestUtils.leaveChannel(client, user2.token, channel1.channelId)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite2.inviteId)
            .expectResult(HttpStatus.FORBIDDEN)

    }

    @Test
    fun`cannot invite for non joined channel`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)


        HttpTestUtils.createInvite2(client, user2.token, channel1.channelId, user3.username, perms_w_i)
            .expectResult(HttpStatus.FORBIDDEN)
    }

    @Test
    fun`can invite for private channel after accepting invite with invite permissions`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite1.inviteId)
            .expectResult(HttpStatus.OK)

        val invite2 = HttpTestUtils.createInvite2(client, user2.token, channel1.channelId, user3.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user3.token, channel1.channelId, invite2.inviteId)
            .expectResult(HttpStatus.OK)
    }

    @Test
    fun`cannot invite for private channel after accepting invite without invite permissions`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val perms = arrayOf(HttpTestUtils.Perms.Write)
        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, perms)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite1.inviteId)
            .expectResult(HttpStatus.OK)

        val invite2 = HttpTestUtils.createInvite2(client, user2.token, channel1.channelId, user3.username, perms_w_i)
            .expectResult(HttpStatus.FORBIDDEN)

    }

    @Test
    fun`can invite for public channel after joining without invite`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, null)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.createInvite2(client, user2.token, channel1.channelId, user3.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)
    }

    @Test
    fun`can invite for public channel after accepting invite with invite permission`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite1.inviteId)
            .expectResult(HttpStatus.OK)


        val invite2 = HttpTestUtils.createInvite2(client, user2.token, channel1.channelId, user3.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user3.token, channel1.channelId, invite2.inviteId)
            .expectResult(HttpStatus.OK)
    }

    @Test
    fun`cannot invite user already in channel`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user4 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId,  user2.username,
            arrayOf(HttpTestUtils.Perms.Write, HttpTestUtils.Perms.Invite))
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)
        val invite2 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId,  user3.username,
            arrayOf(HttpTestUtils.Perms.Write, HttpTestUtils.Perms.Invite))
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite1.inviteId)
            .expectResult(HttpStatus.OK)
        HttpTestUtils.joinChannel(client, user3.token, channel1.channelId, invite2.inviteId)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.createInvite2(client, user3.token, channel1.channelId, user2.username, perms_w_i)
            .expectResult(HttpStatus.CONFLICT)

    }

    @Test
    fun`cannot re use already accepted invite to re join channel`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite1.inviteId)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.leaveChannel(client, user2.token, channel1.channelId)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite1.inviteId)
            .expectResult(HttpStatus.FORBIDDEN)

    }

    @Test
    fun `can clear pending invites`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val channel2 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val channel3 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        val invite2 = HttpTestUtils.createInvite2(client, user1.token, channel2.channelId, user2.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        val invite3 = HttpTestUtils.createInvite2(client, user1.token, channel3.channelId, user2.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        val userInvites = HttpTestUtils.userChannelInvites(client, user2.token)
            .expectResult(HttpStatus.OK, ChannelInvitesOutputModel::class.java)

        assertEquals(3, userInvites.invites.size)

        val inviteIds = arrayOf(invite1.inviteId, invite2.inviteId)

        HttpTestUtils.clearChannelInvites(client, user2.token, inviteIds)
            .expectResult(HttpStatus.OK)

        val userInvites2 = HttpTestUtils.userChannelInvites(client, user2.token)
            .expectResult(HttpStatus.OK, ChannelInvitesOutputModel::class.java)

        assertEquals(1, userInvites2.invites.size)


    }

    @Test
    fun `cannot use invite after clearing it`(){

        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.clearChannelInvites(client, user2.token, arrayOf(invite1.inviteId))
            .expectResult(HttpStatus.OK)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite1.inviteId)
            .expectResult(HttpStatus.FORBIDDEN)


    }

    @Test
    fun `can be invited again after clearing invite`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.clearChannelInvites(client, user2.token, arrayOf(invite1.inviteId))
            .expectResult(HttpStatus.OK)

        val invite2 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)
        assertNotEquals(invite1.inviteId, invite2.inviteId)


        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, null)
            .expectResult(HttpStatus.FORBIDDEN)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite2.inviteId)
            .expectResult(HttpStatus.OK)
    }

    @Test
    fun `can obtain pending invites for a channel`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user4 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        val invite2 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user3.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        val invite3 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user4.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        val invitesMap = mapOf(
            invite1.inviteId to invite1,
            invite2.inviteId to invite2,
            invite3.inviteId to invite3
        )

        val invites = HttpTestUtils.userChannelInvites(client, user1.token, channel1.channelId)
            .expectResult(HttpStatus.OK, ChannelInvitesOutputModel::class.java)

        assertEquals(3, invites.invites.size)
        for (invite in invites.invites){
            assertTrue(invitesMap.containsKey(invite.inviteId))
            assertEquals(channel1.channelName, invite.channelName)
            assertEquals(invitesMap[invite.inviteId]?.senderId, invite.senderId)
        }

    }

    @Test
    fun `cannot obtain pending invites for a channel if not in the channel`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user4 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        val invite2 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user3.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        val invite3 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user4.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        val invitesMap = mapOf(
            invite1.inviteId to invite1,
            invite2.inviteId to invite2,
            invite3.inviteId to invite3
        )

        val invites = HttpTestUtils.userChannelInvites(client, user2.token, channel1.channelId)
            .expectResult(HttpStatus.FORBIDDEN)

    }

    @Test
    fun `can remove user invites with permissions`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user4 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        val invite2 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user3.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        val invite3 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user4.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        val invites = HttpTestUtils.userChannelInvites(client, user1.token, channel1.channelId)
            .expectResult(HttpStatus.OK, ChannelInvitesOutputModel::class.java)

        assertEquals(3, invites.invites.size)

        val toClear = arrayOf(invite1.inviteId, invite3.inviteId)
        HttpTestUtils.removeChannelInvites(client, user1.token, toClear)
            .expectResult(HttpStatus.OK)

        val invites2 = HttpTestUtils.userChannelInvites(client, user1.token, channel1.channelId)
            .expectResult(HttpStatus.OK, ChannelInvitesOutputModel::class.java)


        assertEquals(1, invites2.invites.size)

    }

    @Test
    fun `cannot remove user invites without permissions`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user3 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user4 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        val invite2 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user3.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        val invite3 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user4.username, perms_w_i)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite1.inviteId)
            .expectResult(HttpStatus.OK)

        val invites = HttpTestUtils.userChannelInvites(client, user2.token, channel1.channelId)
            .expectResult(HttpStatus.OK, ChannelInvitesOutputModel::class.java)

        assertEquals(2, invites.invites.size)

        val toClear = arrayOf(invite2.inviteId, invite3.inviteId)
        HttpTestUtils.removeChannelInvites(client, user2.token, toClear)
            .expectResult(HttpStatus.FORBIDDEN)

        HttpTestUtils.removeChannelInvites(client, user1.token, toClear)
            .expectResult(HttpStatus.OK)

        val invites2 = HttpTestUtils.userChannelInvites(client, user2.token, channel1.channelId)
            .expectResult(HttpStatus.OK, ChannelInvitesOutputModel::class.java)


        assertEquals(0, invites2.invites.size)

    }


}