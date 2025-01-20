package dawchat.http

import dawchat.domain.message.MessageTypes
import dawchat.http.model.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MessagesTest {

    @LocalServerPort
    var port: Int = 0


    @Test
    fun `can create messages`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)

        val channelName = HttpTestUtils.randomChannelName()
        val channel = HttpTestUtils.createChannel(client, user1.token, channelName,
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val message1 = HttpTestUtils.createMessage(client, user1.token, channel.channelId,
            HttpTestUtils.MessageType.Text, "this is a test")
            .expectResult(HttpStatus.CREATED, MessageOutputModel::class.java)
        val message2 = HttpTestUtils.createMessage(client, user1.token, channel.channelId,
            HttpTestUtils.MessageType.Text, "this is a test 2")
            .expectResult(HttpStatus.CREATED, MessageOutputModel::class.java)
        val message3 = HttpTestUtils.createMessage(client, user1.token, channel.channelId,
            HttpTestUtils.MessageType.Text, "this is a test 3")
            .expectResult(HttpStatus.CREATED, MessageOutputModel::class.java)
        val message4 = HttpTestUtils.createMessage(client, user1.token, channel.channelId,
            HttpTestUtils.MessageType.Text, "this is a test 4")
            .expectResult(HttpStatus.CREATED, MessageOutputModel::class.java)


        assertTrue(message2.messageId > message1.messageId)
        assertTrue(message4.messageId > message3.messageId)
    }

    @Test
    fun`can write to public channel`() {

    }

    @Test
    fun`can write to private channel with write permission`() {

    }

    @Test
    fun`cannot write to private channel without write permissions`() {

    }

    @Test
    fun`cannot write to non joined channel`() {

    }

    @Test
    fun`can read messages from joined channel`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client, )
        val user2 = HttpTestUtils.createRandomUserAndLogin(client, )

        val channelName = HttpTestUtils.randomChannelName()
        val channel1 = HttpTestUtils.createChannel(client, user1.token, channelName,
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        HttpTestUtils.joinChannel(client, user2.token, channel1.channelId, null)

        val message1Content = "this is message 1"
        val message2Content = "this is message 2"
        val message3Content = "this is message 3"
        val message4Content = "this is message 4"

        val message1Id = HttpTestUtils.createMessage(client, user1.token, channel1.channelId, "text", message1Content)
            .expectResult(HttpStatus.CREATED)
        val message2Id = HttpTestUtils.createMessage(client, user1.token, channel1.channelId, "text", message2Content)
            .expectResult(HttpStatus.CREATED)
        val message3Id = HttpTestUtils.createMessage(client, user1.token, channel1.channelId, "text", message3Content)
            .expectResult(HttpStatus.CREATED)
        val message4Id = HttpTestUtils.createMessage(client, user1.token, channel1.channelId, "text", message4Content)
            .expectResult(HttpStatus.CREATED)

        val filter = arrayOf(HttpTestUtils.MessageType.Text)
        val results = HttpTestUtils.getMessages(client, user2.token, channel1.channelId, filter)
            .expectResult(HttpStatus.OK, LoadMessagesOutputModel::class.java).results

        assertEquals(1, results.values.size)
        assertNotNull(results[channel1.channelId])
        assertEquals(4, results[channel1.channelId]!!.channelMessages.size)
        assertEquals(message4Content, results[channel1.channelId]!!.channelMessages[0].content)
        assertEquals(message3Content, results[channel1.channelId]!!.channelMessages[1].content)
        assertEquals(message2Content, results[channel1.channelId]!!.channelMessages[2].content)
        assertEquals(message1Content, results[channel1.channelId]!!.channelMessages[3].content)
    }


    @Test
    fun`cannot read messages from non joined channel`() {

    }

    @Test
    fun `can read messages with filter`(){

    }

    @Test
    fun `can read messages with limits`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = HttpTestUtils.createRandomUserAndLogin(client)

        val channelName = HttpTestUtils.randomChannelName()
        val channel = HttpTestUtils.createChannel(client, user1.token, channelName,
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        val filter = arrayOf(HttpTestUtils.MessageType.Text)
        val map = HashMap<Int, String>()
        var createdCount = 0
        for (i in 0..80){
            val content = "this is message $i"
            val created = HttpTestUtils.createMessage(client, user1.token, channel.channelId,
                HttpTestUtils.MessageType.Text, content)
                .expectResult(HttpStatus.CREATED, MessageOutputModel::class.java)
            val result = HttpTestUtils.getMessages(client, user1.token, channel.channelId,
                filter = filter,
                limit = 1
            )
                .expectResult(HttpStatus.OK, LoadMessagesOutputModel::class.java).results
            assertEquals(1, result.size)
            val messages = result.values.toTypedArray()[0].channelMessages
            assertEquals(1, messages.size)
            assertEquals(content, messages[0].content)
            map[created.messageId] = content
            createdCount++
        }

        val chuckSize = 13
        var readCount = 0
        var lastId = Int.MAX_VALUE
        val start = System.currentTimeMillis()
        while (readCount < createdCount){

            val result = HttpTestUtils.getMessages(client, user1.token, channel.channelId,
                filter = filter,
                lastMessage = lastId,
                limit = chuckSize
                )
                .expectResult(HttpStatus.OK, LoadMessagesOutputModel::class.java).results
            assertEquals(1, result.size)
            val messages = result.values.toTypedArray()[0].channelMessages
            if(createdCount - readCount > chuckSize-1)
                assertEquals(chuckSize, messages.size)
            else
                assertEquals(createdCount-readCount, messages.size)

            for (m in messages){
                assertEquals(map[m.messageId], m.content)
                assertTrue(lastId > m.messageId)
                readCount++
                lastId = m.messageId
            }

            if(System.currentTimeMillis() - start > 1000*10)
                assert(false) { "timeout" }
        }

        val result = HttpTestUtils.getMessages(client, user1.token, channel.channelId,
            filter = filter,
            lastMessage = lastId,
            limit = chuckSize
        )
            .expectResult(HttpStatus.OK, LoadMessagesOutputModel::class.java).results
        assertEquals(1, result.size)
        val messages = result.values.toTypedArray()[0].channelMessages
        assertEquals(0, messages.size)

    }

    @Test
    fun `lastMessage updates when new message created`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        val user1 = HttpTestUtils.createRandomUserAndLogin(client)
        val user2 = HttpTestUtils.createRandomUserAndLogin(client)
        val user3 = HttpTestUtils.createRandomUserAndLogin(client)

        val channel1 = HttpTestUtils.createChannel(client, user1.token, HttpTestUtils.randomChannelName(),
            HttpTestUtils.ChannelType.Public)
            .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

        assertNotEquals(0, channel1.lastMessage)

        val result1 = HttpTestUtils.getMessages(client, user1.token, channel1.channelId)
            .expectResult(HttpStatus.OK, LoadMessagesOutputModel::class.java).results
        assertEquals(1, result1.size)
        val messages1 = result1.values.toTypedArray()[0].channelMessages

        assertEquals(1, messages1.size)
        assertEquals(MessageTypes.ChannelCreated, messages1[0].type)

        var channels = HttpTestUtils.userChannels(client, user1.token)
            .expectResult(HttpStatus.OK, UserChannelsOutputModel::class.java).channels
        assertEquals(1, channels.size)
        var ch1 = channels[0]
        assertEquals(channel1.channelId, ch1.channelId)
        assertEquals(ch1.lastMessage, channel1.lastMessage)

        val created1 = HttpTestUtils.createMessage(client, user1.token, channel1.channelId,
            HttpTestUtils.MessageType.Text, "this is a message 1")
            .expectResult(HttpStatus.CREATED, MessageOutputModel::class.java)

        val created2 = HttpTestUtils.createMessage(client, user1.token, channel1.channelId,
            HttpTestUtils.MessageType.Text, "this is a message 2")
            .expectResult(HttpStatus.CREATED, MessageOutputModel::class.java)

        val created3 = HttpTestUtils.createMessage(client, user1.token, channel1.channelId,
            HttpTestUtils.MessageType.Text, "this is a message 3")
            .expectResult(HttpStatus.CREATED, MessageOutputModel::class.java)

        val messagesOnly = arrayOf(HttpTestUtils.MessageType.Text)
        val result2 = HttpTestUtils.getMessages(client, user1.token, channel1.channelId, messagesOnly)
            .expectResult(HttpStatus.OK, LoadMessagesOutputModel::class.java).results
        assertEquals(1, result2.size)
        val messages2 = result2.values.toTypedArray()[0].channelMessages


        assertEquals(3, messages2.size)
        assertEquals(created3.content, messages2[0].content)

        channels = HttpTestUtils.userChannels(client, user1.token)
            .expectResult(HttpStatus.OK, UserChannelsOutputModel::class.java).channels
        assertEquals(1, channels.size)
        ch1 = channels[0]
        assertEquals(channel1.channelId, ch1.channelId)
        assertEquals(created3.messageId, ch1.lastMessage)

    }







}