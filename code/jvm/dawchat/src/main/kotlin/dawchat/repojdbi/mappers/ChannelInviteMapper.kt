package dawchat.repojdbi.mappers

import dawchat.domain.channel.*
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet


class ChannelInviteMapper : RowMapper<ChannelInvite> {

    override fun map(rs: ResultSet, ctx: StatementContext?): ChannelInvite {
        return ChannelInvite(
            inviteId =      rs.getInt("inviteId"),
            senderId =      rs.getInt("senderId"),
            recipientId =   rs.getInt("recipientId"),
            channelId =     rs.getInt("channelId"),
            permissions =   ChannelUserMapper.toUserPermissions(rs.getInt("permissions")),
            message =       rs.getString("message"),
            status =        InviteStatus.valueOf(rs.getString("status")),
            createdAt =     Instant.fromEpochSeconds(rs.getLong("createdAt")),
            resolvedAt =    Instant.fromEpochSeconds(rs.getLong("resolvedAt"))
        )
    }
}
