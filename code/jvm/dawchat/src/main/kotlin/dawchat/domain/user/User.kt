package dawchat.domain.user

import kotlinx.datetime.Instant

data class User (
    val id: Int,
    val username: String,
    val passwInfo: PasswordValidationInfo,
    val createdAt: Instant
) {

    //todo validatePassword here?

}