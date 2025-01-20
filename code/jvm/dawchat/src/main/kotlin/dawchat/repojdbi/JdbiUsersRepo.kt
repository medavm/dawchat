package dawchat.repojdbi

import dawchat.domain.channel.ChannelUser
import dawchat.domain.user.*
import dawchat.repo.UsersRepo
import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiUsersRepo(
    private val handle: Handle,
): UsersRepo {

    override fun createUser(username: String, passwordInfo: PasswordValidationInfo, createdAt: Instant): User {
         val userId =  handle.createUpdate(
            """
            insert into dbo.Users (username, secret, createdAt) values (:username, :passwordValidation, :createdAt)
            """,
        )
            .bind("username", username)
            .bind("passwordValidation", passwordInfo.value)
            .bind("createdAt", createdAt.epochSeconds)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

        return User(
            id = userId,
            username = username,
            passwInfo = passwordInfo,
            createdAt = createdAt
        )
    }

    override fun createInvite(userId: Int, token: String, createdAt: Instant): UserInvite {
        handle.createUpdate(
            """
            insert into dbo.UserInvites (token, createdBy, timestamp) 
            values (:token, :createdBy, :timestamp)
            """,
        )
            .bind("token", token)
            .bind("createdBy", userId)
            .bind("timestamp", createdAt.epochSeconds)
            .execute()

        return UserInvite(
            token = token,
            createdBy = userId,
            acceptedBy = null,
            timestamp = createdAt.epochSeconds
        )
    }

    override fun getInvite(token: String): UserInvite? {
        return handle.createQuery("select * from dbo.UserInvites where token = :token")
            .bind("token", token)
            .mapTo<UserInvite>()
            .singleOrNull()
    }

    override fun getInvites(userId: Int): Array<UserInvite> {
        return handle.createQuery(
            """
               SELECT * FROM dbo.UserInvites 
               WHERE createdBy = :userId
            """,
        )
            .bind("userId", userId)
            .mapTo<UserInvite>().list().toTypedArray()
    }

    override fun useInvite(userId: Int, token: String) {
        handle.createUpdate(
            """
                update dbo.UserInvites
                set acceptedBy = :userId
                where token = :token
            """.trimIndent(),
        )
            .bind("userId", userId)
            .bind("token", token)
            .execute()
    }

    override fun removeInvite(token: String) {
        handle.createUpdate(
            """
                delete from dbo.UserInvites
                where token = :token
            """,
        )
            .bind("token", token)
            .execute()
    }

    override fun getUser(username: String): User? {
        return handle.createQuery("select * from dbo.Users where username = :username")
            .bind("username", username)
            .mapTo<User>()
            .singleOrNull()
    }

    override fun getUser(userId: Int): User? {
        return handle.createQuery("select * from dbo.Users where id = :userId")
            .bind("userId", userId)
            .mapTo<User>()
            .singleOrNull()
    }

    override fun getAuthUser(tokenValidationInfo: TokenValidationInfo): AuthenticatedUser? {
        return handle.createQuery(
            """
                select id, username, secret, token, 
                users.createdAt as userCreatedAt,
                sessions.createdAt as createdAt, 
                lastUsed
                from dbo.Users as users 
                inner join dbo.Sessions as sessions 
                on users.id = sessions.userId
                where token = :token
            """,
        )
            .bind("token", tokenValidationInfo.value)
            .mapTo<AuthenticatedUser>() //todo map?
            .singleOrNull()
    }

    override fun createToken(token: Token, maxUserTokens: Int) {
        val deletions = handle.createUpdate(
            """
            delete from dbo.Sessions 
            where userId = :userId 
                and token in (
                    select token from dbo.Sessions where userId = :userId 
                        order by lastUsed desc offset :offset
                )
            """.trimIndent(),
        )
            .bind("userId", token.userId)
            .bind("offset", maxUserTokens - 1)
            .execute()

        handle.createUpdate(
            """
                insert into dbo.Sessions(token, userId, createdAt, lastUsed) 
                values (:token, :userId, :createdAt, :lastUsed)
            """.trimIndent(),
        )
            .bind("token",      token.tokenValidationInfo.value)
            .bind("userId",     token.userId)
            .bind("createdAt",  token.createdAt.epochSeconds)
            .bind("lastUsed",   token.lastUsed.epochSeconds)
            .execute()

    }

    override fun updateTokenLastUsed(token: Token) {
        handle.createUpdate(
            """
                update dbo.Sessions
                set lastUsed = :lastUsed
                where token = :token
            """.trimIndent(),
        )
            .bind("lastUsed", token.lastUsed.epochSeconds)
            .bind("token", token.tokenValidationInfo.value)
            .execute()
    }

    override fun removeToken(tokenValidationInfo: TokenValidationInfo) {
        handle.createUpdate(
            """
                delete from dbo.Sessions
                where token = :token
            """,
        )
            .bind("token", tokenValidationInfo.value)
            .execute()
    }

}