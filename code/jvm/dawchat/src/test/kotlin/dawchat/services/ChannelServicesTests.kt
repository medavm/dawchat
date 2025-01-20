package dawchat.services

class ChannelServicesTests {

    /*
    @Test
    fun `can create channel and channel user from service`(){
        // given: a user service
        val testClock = UtilsTests.TestClock()
        val channelServices = createChannelServices(testClock)

        val userService = createUsersService(testClock)

        // when: storing a user
        val userName = newTestUserName()
        val password = "password"
        val resultUser = userService.createUser(userName, password)

        when (resultUser) {
            is ServiceResult.Success -> assertTrue(resultUser.result.id > 0)
            is ServiceResult.Error -> fail("Unexpected ${resultUser.error}")
        }

        val resultToken = userService.createToken(userName, password)

        //val authenticatedUser: AuthenticatedUser(resultUser)
        //channelServices.createChannel()
    }

    companion object {
        private fun createChannelServices(
            testClock: UtilsTests.TestClock,
            channelNameMax: Int = 63,
            channelNameMin: Int = 1,
        ) = ChannelsService(
            JdbiTransactionManager(jdbi),
            ChannelDomain(
                ChannelDomainConfig(
                    channelNameMaxLen = channelNameMax,
                    channelNameMinLen = channelNameMin,
                )
            ),
            SseManager(JdbiTransactionManager(jdbi)),
            testClock,
        )
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