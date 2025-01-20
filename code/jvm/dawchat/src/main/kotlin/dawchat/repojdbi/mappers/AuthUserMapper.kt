package dawchat.repojdbi.mappers

import dawchat.domain.channel.Channel
import dawchat.domain.channel.ChannelType
import dawchat.domain.user.*
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class AuthUserMapper : RowMapper<AuthenticatedUser> {
    override fun map(rs: ResultSet, ctx: StatementContext?): AuthenticatedUser {
        return AuthenticatedUser(
            user = User(
                id = rs.getInt("id"),
                username = rs.getString("username"),
                passwInfo = PasswordValidationInfo(rs.getString("secret")),
                createdAt = Instant.fromEpochSeconds(rs.getLong("userCreatedAt"))
            ),
            token = Token(
                userId = rs.getInt("id"),
                createdAt = Instant.fromEpochSeconds(rs.getLong("createdAt")),
                lastUsed = Instant.fromEpochSeconds(rs.getLong("lastUsed")),
                tokenValidationInfo = TokenValidationInfo(rs.getString("token"))
            )
        )
    }
}