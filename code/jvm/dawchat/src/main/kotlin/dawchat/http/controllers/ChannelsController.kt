package dawchat.http.controllers

import dawchat.domain.user.AuthenticatedUser
import dawchat.http.HttpErrors
import dawchat.http.Uris
import dawchat.http.model.*
import dawchat.services.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ChannelsController (
    private val channelsService: ChannelsService,
    private val invitesService: InvitesService,
    private val messageService: MessageService
){

    @PostMapping(Uris.Channel.CREATE)
    fun create(
        @RequestBody input: ChannelCreateInputModel,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = channelsService.createChannel(user, input.channelName, input.channelType)
        return when (res) {
            is ServiceResult.Success -> {
                ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserChannelOutputModel(
                    channelId = res.result.channelId,
                    channelName = res.result.channelName,
                    channelType = res.result.channelType,
                    channelPerms = res.result.permissions,
                    lastMessage = res.result.lastMessage?:0,
                    lastRead = res.result.lastRead?:0,
                    channelOwner = res.result.ownerId,
                    joinedAt = res.result.joinedAt.epochSeconds
                ))
            }

            is ServiceResult.Error -> {
                return when(res.error){
                    CreateChannelError.ChannelNameTaken ->
                        HttpErrors.buildErrorResponse(HttpStatus.CONFLICT, HttpErrors.ChannelNameTaken)
                    CreateChannelError.InvalidChannelName ->
                        HttpErrors.buildErrorResponse(HttpStatus.BAD_REQUEST, HttpErrors.InvalidChannelName)
                }
            }
        }
    }

    @GetMapping(Uris.Channel.INFO)
    fun getChannelInfo(
        @RequestParam channelId: Int,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = channelsService.channelInfo(user, channelId)
        return when (res) {
            is ServiceResult.Success -> ResponseEntity
                .status(HttpStatus.OK)
                .body(UserChannelOutputModel(
                    channelId = res.result.channelId,
                    channelName = res.result.channelName,
                    channelType = res.result.channelType,
                    channelPerms = res.result.permissions,
                    lastMessage = res.result.lastMessage?:0,
                    lastRead = res.result.lastRead?:0,
                    channelOwner = res.result.ownerId,
                    joinedAt = res.result.joinedAt.epochSeconds
                ))

            is ServiceResult.Error ->
                return when(res.error) {
                    ChannelInfoError.NoPermission ->
                        HttpErrors.buildErrorResponse(HttpStatus.FORBIDDEN, HttpErrors.NoPermission)
                    ChannelInfoError.ChannelNotFound ->
                        HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.ChannelNotFound)
                }
        }
    }

    @PostMapping(Uris.Channel.RENAME)
    fun renameChannel(
        @RequestBody input: RenameChannelInputModel,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = channelsService.renameChannel(user, input.channelId, input.name)
        return when (res) {
            is ServiceResult.Success -> ResponseEntity
                .status(HttpStatus.OK)
                .build<Nothing>()

            is ServiceResult.Error ->
                return when(res.error){
                    RenameChannelError.ChannelNameTaken ->
                        HttpErrors.buildErrorResponse(HttpStatus.CONFLICT, HttpErrors.ChannelNameTaken)
                    RenameChannelError.InvalidChannelName ->
                        HttpErrors.buildErrorResponse(HttpStatus.BAD_REQUEST, HttpErrors.InvalidChannelName)
                    RenameChannelError.NoPermission ->
                        HttpErrors.buildErrorResponse(HttpStatus.FORBIDDEN, HttpErrors.NoPermission)
                    RenameChannelError.ChannelNotFound ->
                        HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.ChannelNotFound)
                }
        }
    }

    @GetMapping(Uris.Channel.SEARCH)
    fun searchChannel(
        @RequestParam keyword: String,
        authUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = channelsService.searchChannel(keyword)
        val out = ArrayList<SearchChannelResult>()
        res.forEach { it ->
            out.add(
                SearchChannelResult(
                    it.id,
                    it.name,
                    it.type
                ))
        }
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(SearchChannelOutputModel(out.toTypedArray()))
    }

    @PostMapping(Uris.Channel.INVITE)
    fun createChannelInvite(
        @RequestBody input: CreateInviteInputModel,
        authUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = invitesService.createChannelInvite(authUser, input.channelId, input.username, input.invitePerms)
        return when (res) {
            is ServiceResult.Success -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateInviteOutputModel(
                    inviteId = res.result.inviteId,
                    senderId = res.result.senderId,
                    recipientId = res.result.recipientId,
                    channelId = res.result.channelId,
                    invitePerms = res.result.permissions,
                    timestamp = res.result.createdAt.epochSeconds
                ))

            is ServiceResult.Error ->
                return when(res.error){
                    CreateChannelInviteError.ChannelNotFound ->
                        HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.ChannelNotFound)
                    CreateChannelInviteError.NoPermission ->
                        HttpErrors.buildErrorResponse(HttpStatus.FORBIDDEN, HttpErrors.NoPermission)
                    CreateChannelInviteError.UserAlreadyInChannel ->
                        HttpErrors.buildErrorResponse(HttpStatus.CONFLICT, HttpErrors.UserAlreadyInChannel)
                    CreateChannelInviteError.UserAlreadyInvited ->
                        HttpErrors.buildErrorResponse(HttpStatus.CONFLICT, HttpErrors.UserAlreadyInvited)
                    CreateChannelInviteError.UserNotFound ->
                        HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.UserNotFound)
                }
        }
    }

    @GetMapping(Uris.Channel.INVITES)
    fun getChannelInvites(
        @RequestParam channelId: Int,
        authUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = invitesService.channelInvites(authUser, channelId)
        return when(res){
            is ServiceResult.Success -> {
                val invites = ArrayList<ChannelInviteModel>()
                for (invite in res.result)
                    invites.add(
                        ChannelInviteModel(
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
                        )
                    )
                return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ChannelInvitesOutputModel(invites.toTypedArray()))
            }
            is ServiceResult.Error -> {
                return when(res.error){
                    ChannelInvitesError.ChannelNotFound ->
                        HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.ChannelNotFound)
                    ChannelInvitesError.NoPermission ->
                        HttpErrors.buildErrorResponse(HttpStatus.FORBIDDEN, HttpErrors.NoPermission)
                }
            }
        }

    }

    @GetMapping(Uris.Channel.USERS)
    fun getChannelUsers(
        @RequestParam channelId: Int,
        authUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = channelsService.channelUsers(authUser, channelId)
        return when(res){
            is ServiceResult.Success -> {
                val channelUsers = res.result.map {
                    ChannelUserOutputModel(
                        userId = it.userId,
                        username = it.username,
                        userPerms = it.permissions,
                        joinedAt = it.joinedAt.epochSeconds
                    )
                }
                return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ChannelUsersOutputModel(channelUsers.toTypedArray()))
            }
            is ServiceResult.Error -> {
                return when(res.error){

                    ChannelUsersError.ChannelNotFound ->
                        HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.ChannelNotFound)
                    ChannelUsersError.NoPermission ->
                        HttpErrors.buildErrorResponse(HttpStatus.FORBIDDEN, HttpErrors.NoPermission)
                }
            }
        }

    }

    @PostMapping(Uris.Channel.JOIN)
    fun joinChannel(
        @RequestBody input: JoinChannelInputModel,
        authUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = channelsService.joinChannel(authUser, input.channelId, input.inviteId)
        return when (res) {
            is ServiceResult.Success -> ResponseEntity
                .status(HttpStatus.OK)
                .body(UserChannelOutputModel(
                    channelId = res.result.channelId,
                    channelName = res.result.channelName,
                    channelType = res.result.channelType,
                    channelPerms = res.result.permissions,
                    lastMessage = res.result.lastMessage?:0,
                    lastRead = res.result.lastRead?:0,
                    channelOwner = res.result.ownerId,
                    joinedAt = res.result.joinedAt.epochSeconds
                ))

            is ServiceResult.Error ->
                return when(res.error){
                    JoinChannelError.ChannelNotFound ->
                        HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.ChannelNotFound)
                    JoinChannelError.InvalidChannelInvite ->
                        HttpErrors.buildErrorResponse(HttpStatus.FORBIDDEN, HttpErrors.InvalidChannelInvite)
                    JoinChannelError.ChannelInviteNotFound ->
                        HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.ChannelInviteNotFound)
                    JoinChannelError.NoPermission ->
                        HttpErrors.buildErrorResponse(HttpStatus.FORBIDDEN, HttpErrors.NoPermission)
                    JoinChannelError.UserAlreadyInChannel ->
                        HttpErrors.buildErrorResponse(HttpStatus.CONFLICT, HttpErrors.UserAlreadyInChannel)
                }
        }
    }

    @PostMapping(Uris.Channel.LEAVE)
    fun leaveChannel(
        @RequestBody input: LeaveChannelInputModel,
        authUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = channelsService.leaveChannel(authUser, input.channelId, input.newOwner)
        return when (res) {
            is ServiceResult.Success -> ResponseEntity
                .status(HttpStatus.OK)
                .build<Nothing>()

            is ServiceResult.Error ->
                return when(res.error){
                    LeaveChannelError.ChannelNotFound ->
                        HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.ChannelNotFound)
                    LeaveChannelError.SelectNewOwner ->
                        HttpErrors.buildErrorResponse(HttpStatus.BAD_REQUEST, HttpErrors.SelectNewOwner)
                    LeaveChannelError.UserNotInChannel ->
                        HttpErrors.buildErrorResponse(HttpStatus.CONFLICT, HttpErrors.UserNotInChannel)
                }
        }
    }

    @PostMapping(Uris.Channel.REMOVE_INVITES)
    fun removeInvites(
        @RequestBody input: RemoveChannelInvitesInputModel,
        user: AuthenticatedUser
    ): ResponseEntity<*>{
        val res = invitesService.removeChannelInvites(user, input.toRemove)
        return when (res) {
            is ServiceResult.Success -> {
                return ResponseEntity.status(HttpStatus.OK).build<Nothing>()
            }
            is ServiceResult.Error ->
                return when(res.error){
                    RemoveChannelInviteError.ChannelInviteNotFound ->
                        HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.ChannelInviteNotFound)
                    RemoveChannelInviteError.NoPermission ->
                        HttpErrors.buildErrorResponse(HttpStatus.FORBIDDEN, HttpErrors.NoPermission)
                }
        }
    }

    @PostMapping(Uris.Channel.REMOVE_USERS)
    fun removeUsers(
        @RequestBody input: RemoveUsersInputModel,
        user: AuthenticatedUser
    ): ResponseEntity<*>{
        val res = channelsService.removeUsers(user, input.channelId, input.toRemove)
        return when (res) {
            is ServiceResult.Success -> {
                return ResponseEntity.status(HttpStatus.OK).build<Nothing>()
            }
            is ServiceResult.Error ->
                return when(res.error){
                    RemoveUserError.ChannelNotFound ->
                        HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.ChannelNotFound)
                    RemoveUserError.NoPermission ->
                        HttpErrors.buildErrorResponse(HttpStatus.FORBIDDEN, HttpErrors.NoPermission)
                    RemoveUserError.CantRemoveOwner ->
                        HttpErrors.buildErrorResponse(HttpStatus.CONFLICT, HttpErrors.CantRemoveOwner)
                    RemoveUserError.UserNotInChannel ->
                        HttpErrors.buildErrorResponse(HttpStatus.CONFLICT, HttpErrors.UserNotInChannel)
                    RemoveUserError.CantSelfRemove ->
                        HttpErrors.buildErrorResponse(HttpStatus.CONFLICT, HttpErrors.CantSelfRemove)
                }
        }
    }

    @PostMapping(Uris.Channel.Message.SEND)
    fun sendMessage(
        @RequestBody input: CreateMessageInputModel,
        user: AuthenticatedUser
    ): ResponseEntity<*>{
        val res = messageService.createMessage(
            user,
            input.channelId,
            input.type,
            input.content)
        return when (res) {
            is ServiceResult.Success -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MessageOutputModel(
                    messageId = res.result.messageId,
                    userId = res.result.userId,
                    username = res.result.userName,
                    type = res.result.type,
                    content = res.result.content,
                    timestamp = res.result.timestamp
                ))

            is ServiceResult.Error ->
                return when(res.error){
                    CreateMessageError.ChannelNotFound ->
                        HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.ChannelNotFound)
                    CreateMessageError.NoPermission ->
                        HttpErrors.buildErrorResponse(HttpStatus.FORBIDDEN, HttpErrors.NoPermission)
                }
        }
    }

    @PutMapping(Uris.Channel.Message.READ)
    fun updateLastRead(
        @RequestParam channelId: Int,
        @RequestParam messageId: Int,
        user: AuthenticatedUser
    ): ResponseEntity<*>{
        val res = messageService.updateLastRead(user, channelId, messageId)
        return when (res) {
            is ServiceResult.Success -> {
                return ResponseEntity.status(HttpStatus.OK).build<Nothing>()
            }

            is ServiceResult.Error ->
                return when(res.error){
                    UpdateLastReadError.ChannelNotFound ->
                        HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.ChannelNotFound)
                    UpdateLastReadError.UserNotInChannel ->
                        HttpErrors.buildErrorResponse(HttpStatus.CONFLICT, HttpErrors.UserNotInChannel)
                    UpdateLastReadError.InvalidMessageId ->
                        HttpErrors.buildErrorResponse(HttpStatus.CONFLICT, HttpErrors.InvalidMessageId)
                }
        }
    }


    @PostMapping(Uris.Channel.Message.GET)
    fun getMessages(
        @RequestBody input: LoadMessagesInputModel,
        user: AuthenticatedUser
    ): ResponseEntity<*>{
        val res = messageService.channelMessages(
            user,
            input.load.map { LoadMessagesOptions(
                channelId = it.channelId,
                lastMessage = it.lastMessage,
                limit = it.limit,
                types = it.filter
            ) }.toTypedArray()
        )

        return when (res) {
            is ServiceResult.Success -> {
                val respBody = res.result.map { entry -> entry.channelId to ChannelMessagesOutputModel(
                    channelId = entry.channelId,
                    channelName = entry.channelName,
                    channelMessages = entry.messages.map { m -> MessageOutputModel(
                        messageId = m.messageId,
                        userId = m.userId,
                        username = m.userName,
                        type = m.type,
                        content = m.content,
                        timestamp = m.timestamp
                    ) }.toTypedArray(),
                ) }.toMap()

                ResponseEntity.status(HttpStatus.OK).body(
                    LoadMessagesOutputModel(respBody)
                )
            }

            is ServiceResult.Error ->
                return when(res.error){
                    ChannelMessagesError.ChannelNotFound ->
                        HttpErrors.buildErrorResponse(HttpStatus.NOT_FOUND, HttpErrors.ChannelNotFound)
                    ChannelMessagesError.NoPermission ->
                        HttpErrors.buildErrorResponse(HttpStatus.FORBIDDEN, HttpErrors.NoPermission)
                }
        }
    }



}