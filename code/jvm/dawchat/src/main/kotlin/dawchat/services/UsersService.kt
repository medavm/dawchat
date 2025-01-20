package dawchat.services

import dawchat.domain.user.*
import dawchat.repo.transaction.TransactionManager
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class UserInviteInfo(
    private val invite: UserInvite,
    private val user: User?
){
    val token = invite.token
    val createdBy = invite.createdBy
    val acceptedId = invite.acceptedBy
    val acceptedName = user?.username
    val timestamp = invite.timestamp
}

class LoginInfo(
    val token: String,
    val user: User
)

sealed class CreateUserError{
    data object InvalidUsername:            CreateUserError()
    data object InsecurePassword:           CreateUserError()
    data object InvalidUserInvite:          CreateUserError()
    data object UsernameTaken:              CreateUserError()
}

sealed class RemoveInviteError{
    data object UserInviteNotFound:     RemoveInviteError()
    data object NoPermission:           RemoveInviteError()
}

sealed class CreateTokenError{
    data object InvalidCredentials:         CreateTokenError()
}

sealed class AuthenticatedUserError{
    data object InvalidToken:         AuthenticatedUserError()
}

@Service
class UsersService(
    private val transactionManager: TransactionManager,
    private val usersDom: UsersDomain,
    private val sseManager: SseManager,
    private val serviceUtils: ServiceUtils,
    private val clock: Clock,
) {

    //prevents the same invite to be used simultaneously
    private val createUserLock = ReentrantLock()

    fun createUser(username: String, password: String, inviteToken: String):
            ServiceResult<User, CreateUserError> = createUserLock.withLock{

        if (!usersDom.validateUsername(username))
            return ServiceResult.Error(CreateUserError.InvalidUsername)

        if (!usersDom.isLegalPassword(password))
            return ServiceResult.Error(CreateUserError.InsecurePassword)

        val passwordValidationInfo = usersDom.createPasswordValidationInfo(password)
        return transactionManager.run {

            val inv = it.usersRepo.getInvite(inviteToken)
                ?: return@run ServiceResult.Error(CreateUserError.InvalidUserInvite)

            if(inv.acceptedBy!=null)
                return@run ServiceResult.Error(CreateUserError.InvalidUserInvite)

            if (it.usersRepo.getUser(username) != null) {
                return@run ServiceResult.Error(CreateUserError.UsernameTaken)
            }
            else {
                val user = it.usersRepo.createUser(username, passwordValidationInfo, clock.now())
                it.usersRepo.useInvite(user.id, inviteToken)
                return@run ServiceResult.Success(user)
            }
        }
    }

    fun createInvite(authUser: AuthenticatedUser): UserInviteInfo{
        return transactionManager.run {
            val invite = it.usersRepo.createInvite(authUser.user.id, usersDom.generateToken(),
                serviceUtils.clock.now())
            var user: User? = null
            if(invite.acceptedBy!=null)
                user = it.usersRepo.getUser(invite.acceptedBy)

            val inviteInfo = UserInviteInfo(invite, user)

            return@run inviteInfo
        }
    }

    fun getUserInvites(authUser: AuthenticatedUser): Array<UserInviteInfo>{
        return transactionManager.run {
            val invites = it.usersRepo.getInvites(authUser.user.id)
            val invitesInfo = invites.map { inv ->
                var user: User? = null
                if(inv.acceptedBy!=null)
                    user = it.usersRepo.getUser(inv.acceptedBy)
                UserInviteInfo(inv, user)}
            return@run invitesInfo.toTypedArray()
        }
    }

    fun removeInvites(authUser: AuthenticatedUser, tokens: Array<String>): ServiceResult<Boolean, RemoveInviteError>{
        return transactionManager.run {

            for (t in tokens){
                val inv = it.usersRepo.getInvite(t)
                    ?: return@run ServiceResult.Error(RemoveInviteError.UserInviteNotFound)

                if(inv.createdBy!=authUser.user.id)
                    return@run ServiceResult.Error(RemoveInviteError.NoPermission)

                it.usersRepo.removeInvite(t)
            }

            return@run ServiceResult.Success(true)
        }
    }

    fun createToken(username: String, password: String): ServiceResult<LoginInfo, CreateTokenError>{

        if (username.isBlank() || password.isBlank()) {
            return ServiceResult.Error(CreateTokenError.InvalidCredentials)
        }

        return transactionManager.run {

            val user = it.usersRepo.getUser(username)
            if(user == null || !usersDom.validatePassword(password, user.passwInfo))
                return@run ServiceResult.Error(CreateTokenError.InvalidCredentials)

            val tokenValue = usersDom.generateToken()
            val now = clock.now()
            val newToken = Token(
                userId = user.id,
                createdAt = now,
                lastUsed = now,
                tokenValidationInfo = usersDom.createTokenValidationInfo(tokenValue)
            )

            it.usersRepo.createToken(newToken, usersDom.maxUserTokens)
            return@run ServiceResult.Success(LoginInfo(tokenValue, user))
        }
    }

    fun removeToken(token: String) {
        //val tokenValidationInfo = usersDom.createTokenValidationInfo(token)
        val tokenValidationInfo = TokenValidationInfo(token)
        return transactionManager.run {
            it.usersRepo.removeToken(tokenValidationInfo) //todo if token does not exists?
        }
    }

    fun authenticatedUser(token: String): ServiceResult<AuthenticatedUser, AuthenticatedUserError> {

        if (!usersDom.canBeToken(token)) {
            return ServiceResult.Error(AuthenticatedUserError.InvalidToken)
        }

        return transactionManager.run {
            val tokenValidationInfo = usersDom.createTokenValidationInfo(token)
            val authUser = it.usersRepo.getAuthUser(tokenValidationInfo)

            if(authUser==null || usersDom.tokenExpired(authUser.token, clock))
                return@run ServiceResult.Error(AuthenticatedUserError.InvalidToken)

            it.usersRepo.updateTokenLastUsed(authUser.token)
            return@run ServiceResult.Success(authUser)
        }
    }

    fun registerListener(authUser: AuthenticatedUser, eventEmitter: EventEmitter){
        sseManager.addListener(authUser, eventEmitter)

        eventEmitter.onCompletion {
            //logger.info("onCompletion")
            //sseManager.removeListener(authUser, eventEmitter)
        }
        eventEmitter.onError {
            //logger.info("onError")
            //sseManager.removeListener(authUser, eventEmitter)
        }

    }

}