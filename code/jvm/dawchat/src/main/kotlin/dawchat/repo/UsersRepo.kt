package dawchat.repo

import dawchat.domain.user.*
import kotlinx.datetime.Instant

interface UsersRepo {

    fun createUser(username: String, passwordInfo: PasswordValidationInfo, createdAt: Instant): User

    fun createInvite(userId: Int, token: String, createdAt: Instant): UserInvite

    fun getInvite(token: String) : UserInvite?

    fun getInvites(userId: Int): Array<UserInvite>

    fun useInvite(userId: Int, token: String)

    fun removeInvite(token: String)

    fun getUser(username: String): User?

    fun getUser(userId: Int): User?

    fun getAuthUser(tokenValidationInfo: TokenValidationInfo): AuthenticatedUser?

    fun createToken(token: Token, maxUserTokens: Int)

    fun updateTokenLastUsed(token: Token)

    fun removeToken(tokenValidationInfo: TokenValidationInfo)

}