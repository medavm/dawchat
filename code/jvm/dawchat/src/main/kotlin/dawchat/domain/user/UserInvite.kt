package dawchat.domain.user

class UserInvite(
    val token: String,
    val createdBy: Int,
    val acceptedBy: Int?,
    val timestamp: Long
) {
}