package dawchat.domain.user

import kotlinx.datetime.Instant


class Token(
    val userId: Int,
    val createdAt: Instant,
    val lastUsed: Instant,
    val tokenValidationInfo: TokenValidationInfo
) {
}