package dawchat.domain.user

import kotlinx.datetime.Clock
import java.security.SecureRandom
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.*

@Component
class UsersDomain(
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder,
    private val config: UsersDomainConfig
) {

    fun generateToken(): String{
        return ByteArray(config.tokenLen).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            Base64.getUrlEncoder().encodeToString(byteArray)
        }
    }

    fun createTokenValidationInfo(token: String): TokenValidationInfo{
        return tokenEncoder.createValidationInfo(token)
    }

    fun canBeToken(token: String): Boolean{
        try{
            return Base64.getUrlDecoder().decode(token).size == config.tokenLen
        }
        catch (ex: IllegalArgumentException) {
            return false
        }
    }

    fun tokenExpired(token: Token, clock: Clock) : Boolean{
        val now = clock.now()
        return !(token.createdAt <= now &&
                (now - token.createdAt) <= config.tokenMaxAge &&
                (now - token.lastUsed) <= config.tokenMaxAgeIdle)
    }

    fun validatePassword(password: String, validationInfo: PasswordValidationInfo) = passwordEncoder.matches(
        password,
        validationInfo.value,
    )

    fun createPasswordValidationInfo(password: String) = PasswordValidationInfo(
        value = passwordEncoder.encode(password),
    )

    fun validateUsername(username: String): Boolean{
        return username.length >= config.usernameMinLen
                && username.length <= config.usernameMaxLen
    }

    fun isLegalPassword(password: String): Boolean{
        return password.length >= config.passwordMinLen
    }

    val maxUserTokens = config.maxTokensPerUser

}