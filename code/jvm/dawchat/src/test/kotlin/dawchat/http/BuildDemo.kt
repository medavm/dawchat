package dawchat.http

import dawchat.domain.channel.UserPermissions
import dawchat.http.HttpTestUtils.Companion.STARTUSER_ID
import dawchat.http.HttpTestUtils.Companion.STARTUSER_NAME
import dawchat.http.HttpTestUtils.Companion.STARTUSER_PASSW
import dawchat.http.HttpTestUtils.Companion.createUser
import dawchat.http.HttpTestUtils.Companion.createUserInvite
import dawchat.http.HttpTestUtils.Companion.login
import dawchat.http.HttpTestUtils.Companion.randomUsername
import dawchat.http.HttpTestUtils.Companion.startUser
import dawchat.http.HttpTestUtils.TestUser
import dawchat.http.model.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BuildDemo{

    @LocalServerPort
    var port: Int = 8080

    var user1: HttpTestUtils.TestUser? = null
    var demoUser1: HttpTestUtils.TestUser? = null
    var demoUser2: HttpTestUtils.TestUser? = null

    val usernames = arrayOf(
        "demouser1",
        "demouser2",

        "Waldo123",
        "jesse91",
        "TonyNotStark",
        "MarkHum",
        "rita44",
        "Imogen3",
        "MikeO1",
        "maiden5",
        "peter11",
        "Josh77",

        "Golden22",
        "JackOwn",
        "Michel_09",
        "Jonny1111",
        "spencerAA",
        "Dragon.92",
        "Coup44",
        "flashflash",
        "Meme35",
        "JohnMon",

        "33Force",
        "Nickelback22",
        "superMan7",
        "system32",
        "Kot123",
        "t1me",
        "yellow5",
        "LuLu09",
        "Bond1",
        "bimmer46",
    )

    val messages = arrayOf(
        ""


    )


    fun createUserAndLogin(client: WebTestClient, username: String, passw: String): TestUser {

        if(startUser ==null){
            val startUserToken = login(client, STARTUSER_NAME, STARTUSER_PASSW)
                .expectCookie(HttpStatus.OK, "t")

            startUser = TestUser(STARTUSER_ID, STARTUSER_NAME, STARTUSER_PASSW, startUserToken)
        }

        val invite = createUserInvite(client, startUser!!.token)
            .expectResult(HttpStatus.CREATED, UserInviteOutputModel::class.java)
        val user1 = createUser(client, username, passw, invite.token)
            .expectResult(HttpStatus.CREATED, UserInfoOutputModel::class.java)
        val token = login(client, username, passw)
            .expectCookie(HttpStatus.OK, "t")
        return TestUser(user1.userId, username, passw, token)
    }

    fun randomPerms(): Array<String>{

        val perms = mutableListOf(HttpTestUtils.Perms.Read)

        if((0..100).random() < 70)
            perms.add(HttpTestUtils.Perms.Write)

        if((0..100).random() < 50)
            perms.add(HttpTestUtils.Perms.Invite)

        if((0..100).random() < 20)
            perms.add(HttpTestUtils.Perms.Rename)

        if((0..100).random() < 10)
            perms.add(HttpTestUtils.Perms.RemoveUsers)

        return perms.toTypedArray()

    }
    //@Test
    fun buildDemoDB(){
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()

        //val users = mutableListOf<TestUser>() //HashMap<Int, TestUser>()
        val users = HashMap<Int, TestUser>()
        val channelsOwners = HashMap<Int, Int>()
        val channelWriteUsers = HashMap<Int, MutableList<Int>>()

        var counter = 0
        for (name in usernames)   {
            val user = createUserAndLogin(client, name, "password123")
            val type = if((0..100).random() < 30) HttpTestUtils.ChannelType.Private else HttpTestUtils.ChannelType.Public
            val channelUser = HttpTestUtils.createChannel(client, user.token,"channel-$counter", type)
                .expectResult(HttpStatus.CREATED, UserChannelOutputModel::class.java)

            //users.add(user)
            users[user.userId] = user
            channelsOwners[channelUser.channelId] = user.userId
            if(channelWriteUsers[channelUser.channelId]==null)
            channelWriteUsers[channelUser.channelId] = mutableListOf(user.userId)

            counter++
        }


        //invite to channel
        for (channelId in channelsOwners.keys){
            val notInvitedUsers = users.values.toMutableList()
            var channelOwner = users[channelsOwners[channelId]]

            for (i in 0..12){

                //invite random user

                val randUser = notInvitedUsers.random()
                if(randUser!=channelOwner){
                    val perms = randomPerms()
                    val invite = HttpTestUtils.createInvite2(client, channelOwner!!.token, channelId, randUser.username, perms)
                        .expectResult(HttpStatus.CREATED, CreateInviteOutputModel::class.java)

                    //accept invite?
                    if((0..100).random() < 50){

                        val channelUser = HttpTestUtils.joinChannel(client, randUser.token,channelId, invite.inviteId)
                            .expectResult(HttpStatus.OK, UserChannelOutputModel::class.java)

                        if(HttpTestUtils.Perms.Write in perms)
                            channelWriteUsers[channelId]!!.add(randUser.userId)

                        //if(HttpMappers.UserPerms.INVITE in invite.permissions){
                        //    if((0..100).random() < 10){
                        //        user = randUser
                        //    }
                        //}
                    }

                    notInvitedUsers.remove(randUser)
                }


            }



        }

        val sentMap = HashMap<Int, Int>()

        //create messages
        counter = 0
        for (i in 0..1000){

            val channelId = channelWriteUsers.keys.random()

            if(sentMap[channelId]==null)
                sentMap[channelId] = 0

            val userIds = channelWriteUsers[channelId]!!
            val user = users[userIds.random()]!!
            HttpTestUtils.createMessage(client, user.token, channelId, "text",
                "Message $counter from '${user.username}' to the channel")
                .expectResult(HttpStatus.CREATED, MessageOutputModel::class.java)

            counter++
        }




    }






}