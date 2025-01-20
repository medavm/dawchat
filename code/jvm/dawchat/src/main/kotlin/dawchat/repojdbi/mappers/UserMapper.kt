package dawchat.repojdbi.mappers

import dawchat.domain.user.PasswordValidationInfo
import dawchat.domain.user.User
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class UserMapper: RowMapper<User> {
    override fun map(rs: ResultSet, ctx: StatementContext?): User {
        return User(
            id = rs.getInt("id"),
            username = rs.getString("username"),
            passwInfo = PasswordValidationInfo(rs.getString("secret")),
            createdAt = Instant.fromEpochMilliseconds(rs.getLong("createdAt"))
        )
    }
}