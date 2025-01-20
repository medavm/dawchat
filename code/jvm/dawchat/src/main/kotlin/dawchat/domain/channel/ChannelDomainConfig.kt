package dawchat.domain.channel

data class ChannelDomainConfig(
    val channelNameMaxLen: Int,
    val channelNameMinLen: Int,
) {
    init {
        require(channelNameMaxLen < 64)
        require(channelNameMinLen > 0)
    }
}