package dawchat.repojdbi.mappers

import dawchat.domain.message.Message
import dawchat.domain.message.MessageTypes
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet


class MessageColMapper : ColumnMapper<MessageTypes>{
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext?): MessageTypes {
        return MessageTypes.entries.find { it.name == r.getString(columnNumber) }
            ?: TODO()
    }
}

/*
class MessageRowMapper : RowMapper<Message> {
    override fun map(rs: ResultSet, ctx: StatementContext?): Message {
        return Message(
            messageId = rs.getInt("id"),
            userId = rs.getInt("userId"),
            channelId = rs.getInt("channelId"),
            type = rs.getObject<MessageTypes>("type"),
            content = rs.getString("content"),
            timestamp = rs.getLong("createdAt")
        )
    }

}

 */