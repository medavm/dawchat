package dawchat.domain.user

class AuthenticatedUser(
    val user: User,
    val token: Token
) {
}