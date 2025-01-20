package dawchat.domain.user

interface TokenEncoder {
    fun createValidationInfo(token: String): TokenValidationInfo
}