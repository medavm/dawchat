package dawchat.http

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PermissionsTests {

    @LocalServerPort
    var port: Int = 0



    @Test
    fun`can write to channel with permission`() {

    }

    @Test
    fun`cannot write to channel without permission`() {

    }

    @Test
    fun`can invite to channel with permission`() {

    }

    @Test
    fun`cannot invite to channel without permission`() {

    }

    @Test
    fun`can rename channel with permission`() {

    }

    @Test
    fun`cannot rename channel without permission`() {

    }




}