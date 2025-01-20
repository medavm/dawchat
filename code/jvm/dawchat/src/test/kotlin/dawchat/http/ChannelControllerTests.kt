package dawchat.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import dawchat.domain.channel.ChannelType
import dawchat.domain.channel.UserPermissions
import dawchat.http.model.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.http.codec.CodecConfigurer.DefaultCodecs
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.ExchangeStrategies
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChannelControllerTests {

    @LocalServerPort
    var port: Int = 8080


    @Test
    fun `can create channel and rename it`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val channelName = HttpTestUtils.randomChannelName()
        val createdChannel = HttpTestUtils.createChannel(client, user1.token, channelName,
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val newChannelName = HttpTestUtils.randomChannelName()
        HttpTestUtils.renameChannel(client, user1.token, createdChannel.channelId, newChannelName)
            .expectResult(HttpStatus.OK)
    }

    @Test
    fun `can't create existing channel name or invalid name'`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)

        val channelName = HttpTestUtils.randomChannelName()
        val channel1 = HttpTestUtils.createChannel(client, user1.token, channelName, 
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        HttpTestUtils.createChannel(client, user1.token, channelName, HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CONFLICT)

        HttpTestUtils.createChannel(client, user1.token, "a", HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.BAD_REQUEST)

    }

    @Test
    fun `can't rename if not allowed or name already exists`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val channelName = HttpTestUtils.randomChannelName()
        val channelName2 = HttpTestUtils.randomChannelName()
        val type = HttpTestUtils.ChannelType.Private

        val channel1 = HttpTestUtils.createChannel(client, user1.token, channelName, type)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)
        val channel2 = HttpTestUtils.createChannel(client, user1.token, channelName2, type)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        HttpTestUtils.renameChannel(client, user2.token, channel1.channelId, HttpTestUtils.randomChannelName())
            .expectResult(HttpStatus.FORBIDDEN)

        HttpTestUtils.renameChannel(client, user1.token, channel1.channelId, channel2.channelName)
            .expectResult(HttpStatus.CONFLICT)

    }

    @Test
    fun `can search channels`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val channelName = HttpTestUtils.randomChannelName()
        val channelName2 = HttpTestUtils.randomChannelName()
        val type = HttpTestUtils.ChannelType.Public

        val channel1 = HttpTestUtils.createChannel(client, user1.token, channelName, type)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)
        val channel2 = HttpTestUtils.createChannel(client, user1.token, channelName2, type)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val searchKeyword = channelName.substring(0, channelName.length - 4)

        val results = HttpTestUtils.searchChannel(client, user1.token, searchKeyword)
            .expectResult(HttpStatus.OK, SearchChannelOutputModel::class.java)

        var found: SearchChannelResult? = null
        for (channel in results.results) {
            if (channel.channelName == channelName)
                found = channel
        }
        assertNotNull(found, "no channels found for keyword: $searchKeyword")
        assertEquals(channelName, found.channelName)

    }

    @Test
    fun `can join public channel`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val channelName = HttpTestUtils.randomChannelName()
        val channelName2 = HttpTestUtils.randomChannelName()
        val type = HttpTestUtils.ChannelType.Public

        val channel1 = HttpTestUtils.createChannel(client, user1.token, channelName, type)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val res = HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, null)
            .expectResult(HttpStatus.OK, UserChannelOutputModel::class.java)

        assertEquals(channel1.channelName, res.channelName)

    }

    @Test
    fun `can leave a channel`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)
        val channel2 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite = HttpTestUtils.createInvite2(client, user1.token, channel2.channelId, user2.username,
            HttpTestUtils.Perms.all())
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, null)

        HttpTestUtils.leaveChannel(client, user2.token, channel1.channelId)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, null)

        HttpTestUtils.leaveChannel(client, user2.token, channel1.channelId)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.leaveChannel(client, user1.token, channel1.channelId)
            .expectResult(HttpStatus.OK)

    }

    @Test
    fun `can leave the channel that created passing a new owner`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        /*
        val objectMapper = ObjectMapper().registerModule(
            SimpleModule()
                .addSerializer(ChannelType::class.java, MyJsonComponent.Serializer())
                .addDeserializer(ChannelType::class.java, MyJsonComponent.Deserializer())

        )


        val strategies = ExchangeStrategies
            .builder()
            .codecs { clientDefaultCodecsConfigurer: ClientCodecConfigurer ->
                clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(
                    Jackson2JsonEncoder(
                       objectMapper,
                        MediaType.APPLICATION_JSON
                    )
                )
                clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(
                    Jackson2JsonDecoder(
                        objectMapper,
                        MediaType.APPLICATION_JSON
                    )
                )
            }.build()

        val webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:$port").exchangeStrategies(
            strategies
        ).build()

         */



        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, null)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.leaveChannel(client, user1.token, channel1.channelId, user2.userId)
            .expectResult(HttpStatus.OK)

    }

    @Test
    fun `cannot leave the channel that created without passing a new owner`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val invite1 = HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username, HttpTestUtils.Perms.all())
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, invite1.inviteId)

        HttpTestUtils.leaveChannel(client, user1.token, channel1.channelId)
            .expectResult(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `cannot leave a not joined channel`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, null)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.leaveChannel(client, user2.token, channel1.channelId)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.leaveChannel(client, user2.token, channel1.channelId)
            .expectResult(HttpStatus.CONFLICT)

    }

    @Test
    fun `can invite and join to channels`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)

        val channelName = HttpTestUtils.randomChannelName()
        val type = HttpTestUtils.ChannelType.Public
        val createdChannel = HttpTestUtils.createChannel(client, user1.token, channelName, type)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)
        val permissions = HttpTestUtils.Perms.all()

        //creates invite
        val createdInvite = HttpTestUtils.createInvite2(client, user1.token, createdChannel.channelId,
            user2.username, permissions)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

        //joins channel using previous invite
        HttpTestUtils.joinChannel(client, user2.token, createdChannel.channelId, createdInvite.inviteId)
            .expectResult(HttpStatus.OK)

    }

    @Test
    fun `join channel without invite`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)

        val channelName = HttpTestUtils.randomChannelName()
        val type = HttpTestUtils.ChannelType.Public
        val publicChannel = HttpTestUtils.createChannel(client, user1.token, channelName, type)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val res = HttpTestUtils.joinChannel(client, user1.token, publicChannel.channelId, null)
            .expectResult(HttpStatus.CONFLICT, ErrorResponseModel::class.java)
        assertEquals(HttpErrors.UserAlreadyInChannel.type, res.type)

        HttpTestUtils.joinChannel(client, user2.token, publicChannel.channelId, null)
            .expectResult(HttpStatus.OK)

        val res2 = HttpTestUtils.joinChannel(client, user2.token, publicChannel.channelId, null)
            .expectResult(HttpStatus.CONFLICT, ErrorResponseModel::class.java)
        assertEquals(HttpErrors.UserAlreadyInChannel.type, res2.type)


        val privateChannel = HttpTestUtils.createChannel(client, user2.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Private)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val res3 = HttpTestUtils.joinChannel(client, user2.token, privateChannel.channelId, null)
            .expectResult(HttpStatus.CONFLICT, ErrorResponseModel::class.java)
        assertEquals(HttpErrors.UserAlreadyInChannel.type, res3.type)

        val res4 = HttpTestUtils.joinChannel(client, user1.token, privateChannel.channelId, null)
            .expectResult(HttpStatus.FORBIDDEN, ErrorResponseModel::class.java)
        assertEquals(HttpErrors.NoPermission.type, res4.type)

    }

    @Test
    fun `can't invite without permission`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val user3 = HttpTestUtils.createRandomUserAndLogin(client)

        val channelName = HttpTestUtils.randomChannelName()
        val type = HttpTestUtils.ChannelType.Public
        val createdChannel = HttpTestUtils.createChannel(client, user1.token, channelName, type)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)
        val permissions = arrayOf(HttpTestUtils.Perms.Write)

        HttpTestUtils.createInvite2(client, user2.token, createdChannel.channelId, user3.username, permissions)
            .expectResult(HttpStatus.FORBIDDEN)

        val createdInvite = HttpTestUtils.createInvite2(client, user1.token, createdChannel.channelId, user2.username,
            permissions)
            .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)
        assertEquals(createdInvite.senderId, user1.userId)

        HttpTestUtils.createInvite2(client, user2.token, createdChannel.channelId, user3.username,
            permissions)
            .expectResult(HttpStatus.FORBIDDEN)

    }

    //@Test
    fun `check all restricted functionality without authorization`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val user3 = HttpTestUtils.createRandomUserAndLogin(client)
        val channelName = HttpTestUtils.randomChannelName()
        val type = HttpTestUtils.ChannelType.Public

        HttpTestUtils.createChannel(client, "invalidtoken", channelName, type)
            .expectResult(HttpStatus.UNAUTHORIZED)

        val channel1 = HttpTestUtils.createChannel(client, user1.token, channelName, type)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        HttpTestUtils.logout(client, user1.token)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.createInvite2(client, user1.token, channel1.channelId, user2.username,
            HttpTestUtils.Perms.all())
            .expectResult(HttpStatus.UNAUTHORIZED)


        HttpTestUtils.logout(client, user2.token)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, null)
            .expectResult(HttpStatus.UNAUTHORIZED)

    }

    @Test
    fun `can get channel users if in the channel`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val user3 = HttpTestUtils.createRandomUserAndLogin(client)
        val user4 = HttpTestUtils.createRandomUserAndLogin(client)

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val channelUsers1 = HttpTestUtils.channelUsers(client, user1.token, channel1.channelId)
            .expectResult(HttpStatus.OK, ChannelUsersOutputModel::class.java)

        assertEquals(1, channelUsers1.users.size)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, null)
            .expectResult(HttpStatus.OK)
        HttpTestUtils.joinChannel(client, user3.token, channel1.channelId, null)
            .expectResult(HttpStatus.OK)
        HttpTestUtils.joinChannel(client, user4.token, channel1.channelId, null)
            .expectResult(HttpStatus.OK)

        val channelUsers2 = HttpTestUtils.channelUsers(client, user1.token, channel1.channelId)
            .expectResult(HttpStatus.OK, ChannelUsersOutputModel::class.java)

        assertEquals(4, channelUsers2.users.size)
    }

    @Test
    fun `cannot get channel users if not in the channel`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val user3 = HttpTestUtils.createRandomUserAndLogin(client)
        val user4 = HttpTestUtils.createRandomUserAndLogin(client)

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val channelUsers1 = HttpTestUtils.channelUsers(client, user2.token, channel1.channelId)
            .expectResult(HttpStatus.FORBIDDEN)

    }

    @Test
    fun `can remove users from channel with permission`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val user3 = HttpTestUtils.createRandomUserAndLogin(client)
        val user4 = HttpTestUtils.createRandomUserAndLogin(client)

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val channelUsers1 = HttpTestUtils.channelUsers(client, user1.token, channel1.channelId)
            .expectResult(HttpStatus.OK, ChannelUsersOutputModel::class.java)

        assertEquals(1, channelUsers1.users.size)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, null)
            .expectResult(HttpStatus.OK)
        HttpTestUtils.joinChannel(client, user3.token, channel1.channelId, null)
            .expectResult(HttpStatus.OK)
        HttpTestUtils.joinChannel(client, user4.token, channel1.channelId, null)
            .expectResult(HttpStatus.OK)

        val channelUsers2 = HttpTestUtils.channelUsers(client, user1.token, channel1.channelId)
            .expectResult(HttpStatus.OK, ChannelUsersOutputModel::class.java)

        assertEquals(4, channelUsers2.users.size)

        HttpTestUtils.removeChannelUsers(client, user1.token, channel1.channelId,
            arrayOf(user2.userId, user3.userId)
        ).expectResult(HttpStatus.OK)

        val channelUsers3 = HttpTestUtils.channelUsers(client, user1.token, channel1.channelId)
            .expectResult(HttpStatus.OK, ChannelUsersOutputModel::class.java)

        assertEquals(2, channelUsers3.users.size)

    }

    @Test
    fun `cannot remove users from channel without permission`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val user3 = HttpTestUtils.createRandomUserAndLogin(client)

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, null)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.joinChannel(client, user3.token, channel1.channelId, null)
            .expectResult(HttpStatus.OK)

        HttpTestUtils.removeChannelUsers(client, user2.token, channel1.channelId,
            arrayOf(user3.userId)
        ).expectResult(HttpStatus.FORBIDDEN)


    }


}