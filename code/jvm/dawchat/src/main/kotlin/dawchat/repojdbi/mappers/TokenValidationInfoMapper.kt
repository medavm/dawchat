package dawchat.repojdbi.mappers


import dawchat.domain.user.TokenValidationInfo
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class TokenValidationInfoMapper : ColumnMapper<TokenValidationInfo> {
    @Throws(SQLException::class)
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext?): TokenValidationInfo =
        TokenValidationInfo(r.getString(columnNumber))
}