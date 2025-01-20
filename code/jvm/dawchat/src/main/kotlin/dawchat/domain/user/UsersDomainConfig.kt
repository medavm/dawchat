package dawchat.domain.user

import kotlin.time.Duration


data class UsersDomainConfig(
    val usernameMaxLen: Int,
    val usernameMinLen: Int,
    val passwordMinLen: Int,
    val tokenLen: Int,
    val tokenMaxAge: Duration,
    val tokenMaxAgeIdle: Duration,
    val maxTokensPerUser: Int,
) {
    init {
        require(usernameMaxLen < 64)
        require(usernameMinLen > 0)
        require(passwordMinLen > 0)
        require(tokenLen > 0)
        require(tokenMaxAge.isPositive())
        require(tokenMaxAgeIdle.isPositive())
        require(maxTokensPerUser > 0)

    }
}