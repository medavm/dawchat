package dawchat.services

import dawchat.UtilsTests
import dawchat.domain.user.*
import dawchat.repojdbi.transaction.JdbiTransactionManager
import dawchat.repojdbi.configureWithAppRequirements
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*
import kotlin.random.Random
import kotlin.test.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class UsersServicesTests {

    /*
    @Test
    fun `can create user and token using service`() {
        // given: a UsersRepository
        val userService = createUsersService(UtilsTests.TestClock())

        // when: storing a user
        val userName = newTestUserName()
        val password = "password"
        val resultUser = userService.createUser(userName, password, "")

        when (resultUser) {
            is ServiceResult.Success -> assertTrue(resultUser.result.id > 0)
            is ServiceResult.Error -> fail("Unexpected ${resultUser.error}")
        }

        val resultToken = userService.createToken(userName, password)

        val token = when (resultToken) {
            is ServiceResult.Success -> resultToken.result
            is ServiceResult.Error -> fail("Unexpected ${resultToken.error}")
        }

        val tokenBytes = Base64.getUrlDecoder().decode(token)
        assertEquals(256 / 8, tokenBytes.size)

    }

    @Test
    fun `can logout`() {
        // given: a user service
        val userService = createUsersService(UtilsTests.TestClock(), maxTokensPerUser = 5)

        // when: creating a user
        val username = newTestUserName()
        val password = "changeit"
        val resultUser = userService.createUser(username, password, "")

        // then: the creation is successful
        when (resultUser) {
            is ServiceResult.Success -> assertTrue(resultUser.result.id > 0)
            is ServiceResult.Error -> fail("Unexpected ${resultUser.error}")
        }

        // when: creating a token
        val resultToken = userService.createToken(username, password)

        // then: token creation is successful
        val token = when (resultToken) {
            is ServiceResult.Success -> resultToken.result
            is ServiceResult.Error -> fail("Token creation should be successful: '${resultToken.error}'")
        }

        // when: using the token
        //var maybeUser = userService.getUserByToken(token.tokenValue)

        // then: token usage is successful
        //assertNotNull(maybeUser)

        // when: revoking and using the token
        userService.removeToken(token)

        //maybeUser = userService.getUserByToken(token.tokenValue)

        // then: token usage is successful
        //assertNull(maybeUser)
    }


    companion object {
        private fun createUsersService(
            testClock: UtilsTests.TestClock,
            usernameMaxLen: Int = 63,
            usernameMinLen: Int = 1,
            passwordMinLen: Int = 4,
            tokenLen: Int = 256 / 8,
            tokenTtl: Duration = 30.days,
            tokenRollingTtl: Duration = 30.minutes,
            maxTokensPerUser: Int = 3,
        ) = UsersService(
            JdbiTransactionManager(jdbi),
            UsersDomain(
                BCryptPasswordEncoder(),
                Sha256TokenEncoder(),
                UsersDomainConfig(
                    usernameMaxLen = usernameMaxLen,
                    usernameMinLen = usernameMinLen,
                    passwordMinLen = passwordMinLen,
                    tokenLen = tokenLen,
                    tokenMaxAge = tokenTtl,
                    tokenMaxAgeIdle = tokenRollingTtl,
                    maxTokensPerUser = maxTokensPerUser,
                ),
            ),
            SseManager(JdbiTransactionManager(jdbi)),

            testClock,
        )

        private fun newTestUserName() = "user-${Math.abs(Random.nextLong())}"

        private val jdbi = Jdbi.create(
            PGSimpleDataSource().apply {
                setURL("jdbc:postgresql://localhost:5432/db?user=dbuser&password=changeit")
            },
        ).configureWithAppRequirements()


    }

     */


}