package dawchat.http

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SseTests {

    @LocalServerPort
    var port: Int = 0

    /*
    //@Test
    fun `TODO`(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        val user1 = createRandomUserAndLogin()
        val channel1 = createChannel(user1.token, newChannelName(), ChannelType.public())
        val channel2 = createChannel(user1.token, newChannelName(), ChannelType.public())
        val channel3 = createChannel(user1.token, newChannelName(), ChannelType.public())

        client.get()
            .uri(Uris.User.LISTENER)
            .header("Authorization", "Bearer ${user1.token}")
            .accept(TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk()
            .returnResult(Message::class.java)
            .responseBody
            .subscribe {
                assertEquals(1, it.messageId)
            }
    }
    */





}

