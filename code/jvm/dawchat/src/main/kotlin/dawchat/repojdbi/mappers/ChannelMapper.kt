package dawchat.repojdbi.mappers

import dawchat.domain.channel.Channel
import dawchat.domain.channel.ChannelType
import dawchat.domain.channel.InviteStatus
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class ChannelMapper : RowMapper<Channel> {

    override fun map(rs: ResultSet, ctx: StatementContext?): Channel {
        return Channel(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            type = ChannelType.valueOf(rs.getString("type")),
            ownerId = rs.getInt("ownerId"),
            lastMessage = rs.getInt("lastMessage"),
            createdAt = Instant.fromEpochSeconds(rs.getLong("createdAt"))
        )
    }
}