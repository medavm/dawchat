package dawchat.repojdbi

import dawchat.Environment
import dawchat.domain.channel.Channel
import dawchat.domain.channel.ChannelType
import dawchat.domain.channel.UserPermissions
import dawchat.domain.user.PasswordValidationInfo
import dawchat.domain.user.User
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.*


// Don't forget to ensure DBMS is up (e.g. by running ./gradlew dbTestsWait)
class JdbiRepoTests {

    @Test
    fun `can create and retrieve user`(){
        runWithHandle { handle ->
            // given: a UsersRepository
            val repo = JdbiUsersRepo(handle)

            // when: storing a user
            val userName = newTestUserName()
            val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
            val user= repo.createUser(userName, passwordValidationInfo, Clock.System.now())

            // and: retrieving a user
            val retrievedUser: User? = repo.getUser(userName)

            // then:
            assertNotNull(retrievedUser)
            assertEquals(userName, retrievedUser.username)
            assertEquals(passwordValidationInfo, retrievedUser.passwInfo)
            assertTrue(retrievedUser.id >= 0)

            // when: asking if the user exists
            val isUserIsStored = repo.getUser(userName)

            // then: response is true
            assertNotNull(isUserIsStored)

            // when: asking if a different user exists
            val anotherUserIsStored = repo.getUser("another-$userName")

            // then: response is false
            assertNull(anotherUserIsStored)
        }
    }

    @Test
    fun `create and validate channel`() {
        runWithHandle { handle ->
            // given: a UsersRepository
            val userRepo = JdbiUsersRepo(handle)
            val channelRepo = JdbiChannelRepo(handle)

            // when: storing a user
            val userName = newTestUserName()
            val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
            val user = userRepo.createUser(userName, passwordValidationInfo,  Clock.System.now())

            // and: retrieving a user
            val retrievedUser: User? = userRepo.getUser(userName)

            // then: response is true
            assertNotNull(retrievedUser)

            val channelName = newTestChannelName()
            val channelPrivacy = 0
            val channelInstant = Instant.fromEpochSeconds(Clock.System.now().epochSeconds)

            val channel = channelRepo.createChannel(
                retrievedUser,
                channelName,
                ChannelType.Public,
                channelInstant
            )

            val retrievedChannel: Channel? = channelRepo.getChannel(channel.id)

            // then:
            assertNotNull(retrievedChannel)
            assertEquals(channel.id, retrievedChannel.id)
            assertEquals(channelName, retrievedChannel.name)
            assertEquals(ChannelType.Public, retrievedChannel.type)
            assertEquals(retrievedUser.id, retrievedChannel.ownerId)
            assertEquals(channelInstant, retrievedChannel.createdAt)
        }
    }

    @Test
    fun `create and validate channel user`() {
        runWithHandle { handle ->
            // given: a UsersRepository
            val userRepo = JdbiUsersRepo(handle)
            val channelRepo = JdbiChannelRepo(handle)

            // when: storing a user
            val userName = newTestUserName()
            val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
            val user = userRepo.createUser(userName, passwordValidationInfo,  Clock.System.now())

            // and: retrieving a user
            val retrievedUser: User? = userRepo.getUser(userName)

            // then: response is true
            assertNotNull(retrievedUser)

            // when: storing a user
            val userName2 = newTestUserName()
            val passwordValidationInfo2 = PasswordValidationInfo(newTokenValidationData())
            val userId2 = userRepo.createUser(userName2, passwordValidationInfo2,  Clock.System.now())

            // and: retrieving a user
            val retrievedUser2: User? = userRepo.getUser(userName2)

            val channelName = newTestChannelName()
            val channelType= ChannelType.Public
            val channelInstant = Clock.System.now()

            val channel = channelRepo.createChannel(retrievedUser, channelName, channelType, channelInstant)

            val retrievedChannel: Channel? = channelRepo.getChannel(channel.id)

            // then:
            assertNotNull(retrievedChannel)

            val permissions = arrayOf(UserPermissions.Read, UserPermissions.Write)
            val channelUserInstant = Instant.fromEpochSeconds(Clock.System.now().epochSeconds)

            channelRepo.addUserToChannel(
                retrievedUser.id,
                retrievedChannel.id,
                permissions,
                null,
                channelUserInstant
            )

            assertNotNull(channelRepo.getChannelUser(retrievedUser.id, retrievedChannel.id))

            channelRepo.getChannelUser(retrievedUser.id, retrievedChannel.id)
                ?.let { assertEquals(retrievedUser.id, it.userId) }

            channelRepo.getChannelUser(retrievedUser.id, retrievedChannel.id)
                ?.let { assertEquals(retrievedChannel.id, it.channelId) }

            channelRepo.getChannelUser(retrievedUser.id, retrievedChannel.id)
                ?.let { assertContentEquals(permissions, it.permissions) }

            channelRepo.getChannelUser(retrievedUser.id, retrievedChannel.id)
                ?.let { assertEquals(channelUserInstant, it.joinedAt) }
        }
    }

    companion object {

        private val jdbi =
            Jdbi.create(
                PGSimpleDataSource().apply {
                    setURL(Environment.getDbUrl())
                },
            ).configureWithAppRequirements()

        private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

        private fun newTestUserName() = "user-${abs(Random.nextLong())}"

        private fun newTokenValidationData() = "token-${abs(Random.nextLong())}"

        private fun newTestChannelName() = "channel-${abs(Random.nextLong())}"
    }
}
