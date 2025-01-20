package dawchat.repojdbi

import dawchat.repojdbi.mappers.*
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin


fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

    registerColumnMapper(MessageColMapper())
    //registerRowMapper(MessageMappers())
    registerRowMapper(AuthUserMapper())
    registerRowMapper(ChannelInviteMapper())
    registerRowMapper(UserMapper())
    registerRowMapper(ChannelMapper())
    registerRowMapper(ChannelUserMapper())

    return this
}