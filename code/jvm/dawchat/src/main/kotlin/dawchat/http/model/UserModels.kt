package dawchat.http.model


data class CreateUserInputModel (
    val username: String,
    val password: String,
    val inviteToken: String
)

data class UserInfoOutputModel(
    val userId: Int,
    val username: String,
    val createdAt: Long
)

data class LoginInputModel(
    val username: String,
    val password: String,
)

class UserInviteOutputModel(
    val token: String,
    val createdBy: Int,
    val acceptedBy: Int?,
    val acceptedName: String?,
    val timestamp: Long
)

class UserInvitesOutputModel(
    val invites: Array<UserInviteOutputModel>
)

class RemoveUserInvitesInputModel(
    val toRemove: Array<String>
)