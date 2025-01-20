package dawchat.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import dawchat.http.model.ErrorResponseModel



class HttpErrors(
){

    companion object{

        private const val MEDIA_TYPE = "application/problem+json"

        fun buildErrorResponse(status: HttpStatus, resp: ErrorResponseModel): ResponseEntity<Any> = ResponseEntity
             .status(status)
             .header("Content-Type", MEDIA_TYPE)
             .body<Any>(resp)

        val InvalidRequestParameters =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/invalid-request-parameters",
                title = "Invalid parameters",
                detail = "Invalid or missing request arguments"
            )

        val ChannelNameTaken =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/channel-name-taken",
                title = "Channel name already exists",
                detail = "A channel with the given name already exists."
            )

        val InvalidChannelName =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/invalid-channel-name",
                title = "Channel name not valid",
                detail = "The given channel name cant be used."
            )

        val NoPermission =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/no-permission",
                title = "No permission",
                detail = "User does not have permission to perform the requested operation."
            )

        val ChannelNotFound =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/channel-not-found",
                title = "Channel not found",
                detail = "The given channel id does not index any existing channel."
            )

        val SelectNewOwner =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/select-owner",
                title = "Select new channel owner",
                detail = "Must pass new owner id when leaving a channel that you created."
            )

        val CantSelfRemove =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/cannot-self-remove",
                title = "Cannot remove yourself from a channel",
                detail = "You cannot remove yourself from a channel"
            )

        val CantRemoveOwner =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/cant-remove-owner",
                title = "Cannot remove owner",
                detail = "Cannot remove the owner of the channel."
            )


        val UserAlreadyInChannel =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/user-aleady-in-channel",
                title = "User already joined the channel",
                detail = "User is already a member of the channel."
            )

        val UserAlreadyInvited =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/user-already-invited",
                title = "User already invited",
                detail = "An invite for the given user for this channel already exists."
            )

        val UserNotFound =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/user-not-found",
                title = "User not found",
                detail = "No user found matching the given username."
            )

        val InvalidChannelInvite =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/invalid-channel-invite",
                title = "Channel invite is not valid",
                detail = "The given invite cant be used for this channel, has already been used, expired or has been revoked"
            )

        val ChannelInviteNotFound =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/channel-invite-not-found",
                title = "Invite not found",
                detail = "No invites matching the given invite id found."
            )

        val UserNotInChannel =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/user-not-in-channel",
                title = "User is not in channel",
                detail = "The given user is not a member of the channel."
            )

        val OwnerCantLeave =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/cant-leave-channel",
                title = "Cant leave the channel",
                detail = "The owner of the channel cannot be removed from the channel."
            )

        val InsecurePassword =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/insecure-password",
                title = "Password is not secure",
                detail = "The password must have at least 8 characters."
            )

        val InvalidUsername =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/invalid-username",
                title = "Username is not valid",
                detail = "The given user name cannot be used."
            )

        val UsernameTaken =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/username-taken",
                title = "User already exists",
                detail = "A user with the same username already exists."
            )

        val InvalidCredentials =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/invalid-credentials",
                title = "Invalid credentials",
                detail = "The given username and password do not match for any user"
            )

        val InvalidUserInvite =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/invalid-user-invite",
                title = "Invalid user invited",
                detail = "The user invite has already been used or is invalid"
            )

        val UserInviteNotFound =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/user-invite-not-found",
                title = "Invite not found",
                detail = "The given token does not mach any invitation"
            )

        val InvalidMessageId =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/invalid-message-id",
                title = "Invalid message id",
                detail = "Could not find a message for the given id"
            )


        val InvalidCredentials3 =
            ErrorResponseModel(
                type = "https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/invalid-credentials",
                title = "Invalid credentials",
                detail = "The given username and password do not match for any user"
            )





    }
}
