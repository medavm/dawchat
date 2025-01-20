package dawchat.repojdbi.mappers

import dawchat.domain.channel.ChannelUser
import dawchat.domain.channel.UserPermissions
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class ChannelUserMapper : RowMapper<ChannelUser> {

    companion object{

        private fun permValue(p: UserPermissions): Int{
            return when(p){
                UserPermissions.Read ->         (1 shl 0)
                UserPermissions.Write ->        (1 shl 1)
                UserPermissions.Invite ->       (1 shl 2)
                UserPermissions.Rename ->       (1 shl 3)
                UserPermissions.RemoveUsers ->  (1 shl 4)
            }
        }

        private val permsMap = UserPermissions.entries.map { it to permValue(it) }.toMap()

        fun fromUserPermissions(perms: Array<UserPermissions>): Int{
            var res = 0
            for (perm in perms){
                val bit = permsMap[perm]
                    ?: TODO()
                res = res or bit
            }
            return res
        }

        fun toUserPermissions(value: Int): Array<UserPermissions>{
            val perms = mutableListOf<UserPermissions>()
            for (entry in permsMap.entries){
                if(entry.value and value > 0)
                    perms.add(entry.key)
            }
            return perms.toTypedArray()
        }

    }


    override fun map(rs: ResultSet, ctx: StatementContext?): ChannelUser {
        return ChannelUser(
            userId = rs.getInt("userId"),
            channelId = rs.getInt("channelId"),
            permissions = toUserPermissions(rs.getInt("permissions")),
            inviteId = rs.getInt("inviteId"),
            lastRead = rs.getInt("lastRead"),
            joinedAt = Instant.fromEpochSeconds(rs.getLong("joinedAt"))
        )
    }
}