package dawchat.http.controllers

import dawchat.domain.user.AuthenticatedUser
import dawchat.http.AuthInterceptor.Companion.SESS_C
import dawchat.http.HttpErrors
import dawchat.http.HttpEventEmitter
import dawchat.http.Uris
import dawchat.http.model.*
import dawchat.services.*
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.TimeUnit

@RestController
class UsersController(
    private val usersService: UsersService,
    private val channelService: ChannelsService,
    private val invitesService: InvitesService
){
    @PostMapping(Uris.User.CREATE)
    fun create(
        @RequestBody input: CreateUserInputModel
    ): ResponseEntity<*> {
        val res = usersService.createUser(input.username, input.password, input.inviteToken)
        return when (res) {
            is ServiceResult.Success -> ResponseEntity.status(HttpStatus.CREATED)
                .body(UserInfoOutputModel(
                    res.result.id,
                    res.result.username,
                    res.result.createdAt.epochSeconds))

            is ServiceResult.Error ->
                return when(res.error){
                    CreateUserError.InsecurePassword ->
                        HttpErrors.buildErrorResponse(HttpStatus.BAD_REQUEST, HttpErrors.InsecurePassword)
                    CreateUserError.InvalidUsername ->
                        HttpErrors.buildErrorResponse(HttpStatus.BAD_REQUEST, HttpErrors.InvalidUsername)
                    CreateUserError.UsernameTaken ->
                        HttpErrors.buildErrorResponse(HttpStatus.CONFLICT, HttpErrors.UsernameTaken)
                    CreateUserError.InvalidUserInvite ->
                        HttpErrors.buildErrorResponse(HttpStatus.FORBIDDEN, HttpErrors.InvalidUserInvite)
                }
        }
    }

    @PostMapping(Uris.User.CREATE_USER_INVITE)
    fun createUserInvite(
        user: AuthenticatedUser,
    ) : ResponseEntity<*> {
        val res = usersService.createInvite(user)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(UserInviteOutputModel(
                token = res.token,
                createdBy = res.createdBy,
                acceptedBy = res.acceptedId,
                acceptedName = res.acceptedName,
                timestamp = res.timestamp
            ))
    }

    @GetMapping(Uris.User.USER_INVITES)
    fun getUserInvites(
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val invites = usersService.getUserInvites(user)
        val invitesOut = invites.map { UserInviteOutputModel(
            token = it.token,
            createdBy = it.createdBy,
            acceptedBy = it.acceptedId,
            acceptedName = it.acceptedName,
            timestamp = it.timestamp
        ) }
        return ResponseEntity.status(HttpStatus.OK)
            .body(UserInvitesOutputModel(invitesOut.toTypedArray()))
    }

    @PostMapping(Uris.User.REMOVE_USER_INVITES)
    fun removeUserInvites(
        @RequestBody input: RemoveUserInvitesInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val res = usersService.removeInvites(user, input.toRemove)
        return when (res) {
            is ServiceResult.Success ->
                ResponseEntity.status(HttpStatus.OK).build<Nothing>()

            is ServiceResult.Error ->
                return when(res.error){
                    RemoveInviteError.UserInviteNotFound ->
                        HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.UserInviteNotFound)
                    RemoveInviteError.NoPermission ->
                        HttpErrors.buildErrorResponse(HttpStatus.FORBIDDEN, HttpErrors.NoPermission)
                }
        }
    }


    @PostMapping(Uris.User.LOGIN)
    fun login(
        @RequestBody input: LoginInputModel,
        response: HttpServletResponse
    ): ResponseEntity<*> {
        val res = usersService.createToken(input.username, input.password)
        return when (res) {
            is ServiceResult.Success -> {
                val c = Cookie(SESS_C, res.result.token)
                c.maxAge = 7 * 24 * 60 * 60 //TODO from config
                c.isHttpOnly = true
                c.path = "/"
                response.addCookie(c)
                ResponseEntity.status(HttpStatus.OK)
                    .body(UserInfoOutputModel(
                        res.result.user.id,
                        res.result.user.username,
                        res.result.user.createdAt.epochSeconds)
                    )
            }

            is ServiceResult.Error ->
                return when(res.error){
                    CreateTokenError.InvalidCredentials ->
                        HttpErrors.buildErrorResponse(HttpStatus.UNAUTHORIZED, HttpErrors.InvalidCredentials)
                }
        }
    }

    @PostMapping(Uris.User.LOGOUT) //TODO
    fun logout(
        user: AuthenticatedUser,
        response: HttpServletResponse
    ): ResponseEntity<*> {
        usersService.removeToken(user.token.tokenValidationInfo.value)
        val cookie = Cookie(SESS_C, "")
        cookie.maxAge = 0
        cookie.path = "/"
        cookie.isHttpOnly = true
        response.addCookie(cookie)
        return  ResponseEntity.status(HttpStatus.OK).build<Nothing>()
    }

    @GetMapping(Uris.User.INFO)
    fun info(
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(UserInfoOutputModel(
                user.user.id,
                user.user.username,
                user.user.createdAt.epochSeconds))
    }

    @GetMapping(Uris.User.CHANNELS)
    fun getUserChannels(
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val res = channelService.userChannels(authUser = user)
        val userChannels = res.map {
            UserChannelOutputModel(
                channelId = it.channelId,
                channelName = it.channelName,
                channelType = it.channelType,
                channelPerms = it.permissions,
                lastMessage = it.lastMessage?:0,
                lastRead = it.lastRead?:0,
                channelOwner = it.ownerId,
                joinedAt = it.joinedAt.epochSeconds
            )
        }
        return ResponseEntity.status(HttpStatus.OK).body(UserChannelsOutputModel(userChannels.toTypedArray()))
    }

    @GetMapping(Uris.User.CHANNEL_INVITES)
    fun channelInvites(
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val res = invitesService.userInvites(authUser = user)
        val invites = ArrayList<ChannelInviteModel>()
        for (invite in res)
            invites.add(ChannelInviteModel(
                inviteId = invite.inviteId,
                senderId = invite.senderId,
                senderName = invite.senderName,
                recipientId = invite.recipientId,
                recipientName = invite.recipientName,
                channelId = invite.channelId,
                channelName = invite.channelName,
                channelType = invite.channelType,
                invitePerms = invite.channelPermissions,
                timestamp = invite.createdAt.epochSeconds
            ))

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ChannelInvitesOutputModel(invites.toTypedArray()))
    }

    @PostMapping(Uris.User.CLEAR_CHANNEL_INVITES)
    fun clearChannelInvites(
        @RequestBody invites: RemoveChannelInvitesInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val res = invitesService.removeChannelInvites(user, invites.toRemove)
        return when(res) {
            is ServiceResult.Success -> {
                return ResponseEntity.status(HttpStatus.OK).build<Nothing>()
            }
            is ServiceResult.Error -> {
               return when(res.error){
                   RemoveChannelInviteError.ChannelInviteNotFound ->
                       HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.ChannelInviteNotFound)
                   RemoveChannelInviteError.NoPermission ->
                       HttpErrors.buildErrorResponse(HttpStatus.FORBIDDEN, HttpErrors.NoPermission)
               }
            }
        }
    }


    @GetMapping(Uris.User.LISTENER)
    fun listen(
        user: AuthenticatedUser,
    ): SseEmitter {
        val sseEmitter = SseEmitter(TimeUnit.HOURS.toMillis(1))
        usersService.registerListener(user, HttpEventEmitter(sseEmitter))
        return sseEmitter
    }

}