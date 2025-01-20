import { error } from "console";
import { MessageList, MessageType, UserChannelModel, MessageModel, 
    ChannelInviteModel, UserPerm, CreatedChannelInviteModel, ChannelType, 
    ChannelUserModel,
    SearchedChannelsModel,
    RequestMessages,
    UserInfoModel,
    UserInviteModel} from "./APIModels";
import exp from "constants";
import { APIError } from "./Errors";
import { SessionMan } from "./AppMan";


function removeAuthentication(){
    SessionMan.updateUser(undefined)
}

export type Problem = {
    type: string
    title: string
    detail: string
}

function responseProblem(response: APIFetchResponse): Problem{

    if(response.body && response.body.type && response.body.title && response.body.detail) //probem+json
            return response.body as Problem
    
    //no data?
    return {
        type: "fetch-error",
        title: `Failed to fetch (${response.status} ${response.statusText})`,
        detail: `Failed to fetch data from api (${response.status} ${response.statusText})`
    } as Problem
}

function exceptionProblem(e: any){
    if(e && e instanceof Error){
            return {
                type: "fech-exception",
                title: `Failed to fetch (${e.name})`,
                detail: `Failed to fetch data from api: ${e.message}`
            } as Problem
    }

    return {
        type: "fetch-exception",
        title: `Failed to fetch (unknown exception)`,
        detail: `Failed to fetch data from api: unknown exception`
    } as Problem
}

function isJson(response: Response){
    return response.headers.has("Content-type") && 
    (response.headers.get("Content-type")?.includes("application/json")  ||
        response.headers.get("Content-type")?.includes("application/problem+json"))
}

type APIFetchParams = {
    readonly url: string,
    readonly method: "get" | "post" | "put" | "delete"
    readonly credentials?: "include" | "omit" | "same-origin"
    readonly headers?: {[key: string]: string}
    readonly queryParams?: {[key: string]: string}
    readonly body? : object
}


type APIFetchResponse= {
    status: number
    statusText: string
    body?: any
}


type APIFetchCallback = {
    [status: number] : (result: APIFetchResponse) => void
    other: (result: APIFetchResponse) => void
    error: (e: any) => void
}


async function APIFetch(params: APIFetchParams, callbacks: APIFetchCallback){
        try{
            const response = await fetch(params.url, {
                method: params.method,
                credentials: params.credentials,
                headers: params.headers,
                body: params.body ? JSON.stringify(params.body) : undefined
            })
 
            let data: any = undefined
            if(isJson(response))
                data = await response.json()

            //call matching callback
            if(callbacks[response.status])
                return callbacks[response.status]({status: response.status, statusText: response.statusText, body: data})
            return callbacks.other({status: response.status, statusText: response.statusText, body: data})
        }
        catch(e){
           callbacks.error(e)
        }
}


type CreateUserResult = {
    userId: number,
    username: string,
    createdAt: number
}

/*
type CreateUserError = {
    InsecurePassword:   (e: Problem) => void
    InvalidUsername:    (e: Problem) => void
    UsernameTaken:      (e: Problem) => void 
    InvalidUserInvite:  (e: Problem) => void
    other: (e: Problem) => void
}
*/

function _CreateUser (
    params: {username: string, password: string, token: string}, 
    onSuccess:  (result: CreateUserResult) => void,
    onError:    (error: Problem) => void
){
        APIFetch({
            url: "/api/user/create",
            method: "post",
            headers: {
                "Content-Type": "application/json"
            },
            body: {
                username: params.username,
                password: params.password,
                inviteToken: params.token
            }
        },{
            201:    (response) => onSuccess(response.body as CreateUserResult),
            other:  (response) => onError(responseProblem(response)),
            error: e => onError(exceptionProblem(e)),
        })
}

function _CreateInvite (
    params: { channelId: number, username: string, invitePerms: Array<UserPerm> }, 
    onSuccess:  (result: CreatedChannelInviteModel) => void,
    onError:    (error: Problem) => void
){
        APIFetch({
            url: "/api/channel/invite",
            method: "post",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include",
            body: {
                channelId: params.channelId,
                username: params.username,
                invitePerms: params.invitePerms
            }
        },{
            201:    (response) => onSuccess(response.body as CreatedChannelInviteModel),
            401:    (response) => {
                removeAuthentication()
                onError(responseProblem(response))
            },
            other:  (response) => onError(responseProblem(response)),
            error: e => onError(exceptionProblem(e)),
        })
}

function _CancelInvite (
    params:     { toRemove: Array<number> }, 
    onSuccess:  () => void,
    onError:    (error: Problem) => void
){
        APIFetch({
            url: "/api/user/channel/invites/clear",
            method: "post",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include",
            body: params
        },{
            200:    (response) => onSuccess(),
            401:    (response) => {
                removeAuthentication()
                onError(responseProblem(response))
            },
            other:  (response) => onError(responseProblem(response)),
            error: e => onError(exceptionProblem(e)),
        })
}

function _RemoveChannelInvites (
    params:     { toRemove: Array<number> }, 
    onSuccess:  () => void,
    onError:    (error: Problem) => void
){
        APIFetch({
            url: "/api/channel/invites/remove",
            method: "post",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include",
            body: params
        },{
            200:    (response) => onSuccess(),
            401:    (response) => {
                removeAuthentication()
                onError(responseProblem(response))
            },
            other:  (response) => onError(responseProblem(response)),
            error: e => onError(exceptionProblem(e)),
        })
}

function _RemoveChannelUsers (
    params:     { channelId: number, toRemove: Array<number> }, 
    onSuccess:  () => void,
    onError:    (error: Problem) => void
){
        APIFetch({
            url: "/api/channel/users/remove",
            method: "post",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include",
            body: params
        },{
            200:    (response) => onSuccess(),
            401:    (response) => {
                removeAuthentication()
                onError(responseProblem(response))
            },
            other:  (response) => onError(responseProblem(response)),
            error: e => onError(exceptionProblem(e)),
        })
}

function _JoinChannel (
    params: { channelId: number, inviteId?: number }, 
    onSuccess:  (result: UserChannelModel) => void,
    onError:    (error: Problem) => void
){
        APIFetch({
            url: "/api/channel/join",
            method: "post",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include",
            body: params
        },{
            200:    (response) => onSuccess(response.body as UserChannelModel),
            401:    (response) => {
                removeAuthentication()
                onError(responseProblem(response))
            },
            other:  (response) => onError(responseProblem(response)),
            error: e => onError(exceptionProblem(e)),
        })
}

function _CreateChannel (
    params: {channelName: string, channelType: ChannelType}, 
    onSuccess:  (result: UserChannelModel) => void,
    onError:    (error: Problem) => void
){
        APIFetch({
            url: "/api/channel/create",
            method: "post",
            headers: {
                "Content-Type": "application/json"
            },
            body: {
                channelName: params.channelName,
                channelType: params.channelType
            }
        },{
            201:    (response) => onSuccess(response.body as UserChannelModel),
            401:    (response) => {
                removeAuthentication()
                onError(responseProblem(response))
            },
            other:  (response) => onError(responseProblem(response)),
            error: e => onError(exceptionProblem(e)),
        })
}

function _CreateUserInvite (
    onSuccess:  (invite: UserInviteModel) => void,
    onError:    (error: Problem) => void
){
        APIFetch({
            url: "/api/user/invites/create",
            method: "post"
        },{
            201:    response => onSuccess(response.body as UserInviteModel),
            401:    (response) => {
                removeAuthentication()
                onError(responseProblem(response))
            },
            other:  (response) => onError(responseProblem(response)),
            error: e => onError(exceptionProblem(e)),
        })
}

function _RenameChannel (
    params: {channelId: number, name: string}, 
    onSuccess:  () => void,
    onError:    (error: Problem) => void
){
        APIFetch({
            url: "/api/channel/rename",
            method: "post",
            headers: {
                "Content-Type": "application/json"
            },
            body: params
        },{
            200:    (response) => onSuccess(),
            401:    (response) => {
                removeAuthentication()
                onError(responseProblem(response))
            },
            other:  (response) => onError(responseProblem(response)),
            error: e => onError(exceptionProblem(e)),
        })
}

function _SearchChannels(
    params: { keyword: string }, 
    onSuccess:  (result: Array<SearchedChannelsModel>) => void,
    onError:    (error: Problem) => void
){
    APIFetch({
        url: "/api/channel/search?keyword=" + params.keyword, 
        method: "get",
        credentials: "include"
        },{
        200:    (response) => onSuccess(response.body.results as Array<SearchedChannelsModel>),
        401:    (response) => {
            removeAuthentication()
            onError(responseProblem(response))
        },
        other:  (response) => onError(responseProblem(response)),
        error: e => onError(exceptionProblem(e)),
    })
}

function _GetChannelUsers(
    params: { channelId: number }, 
    onSuccess:  (result: Array<ChannelUserModel>) => void,
    onError:    (error: Problem) => void
){
    APIFetch({
        url: "/api/channel/users?channelId=" + params.channelId, 
        method: "get",
        credentials: "include"
        },{
        200:    (response) => onSuccess(response.body.users as Array<ChannelUserModel>),
        401:    (response) => {
            removeAuthentication()
            onError(responseProblem(response))
        },
        other:  (response) => onError(responseProblem(response)),
        error: e => onError(exceptionProblem(e)),
    })
}

function _LeaveChannel (
    params: {channelId: number, newOwner?: number}, 
    onSuccess:  () => void,
    onError:    (error: Problem) => void
){
        APIFetch({
            url: "/api/channel/leave",
            method: "post",
            headers: {
                "Content-Type": "application/json"
            },
            body: {
                channelId: params.channelId,
                newOwner: params.newOwner
            }
        },{
            200:    (response) => onSuccess(),
            401:    (response) => {
                removeAuthentication()
                onError(responseProblem(response))
            },
            other:  (response) => onError(responseProblem(response)),
            error: e => onError(exceptionProblem(e)),
        })
}

function _Login (
    params: {username: string, password}, 
    onSuccess:  (user: UserInfoModel) => void,
    onError:    (error: Problem) => void
){
        APIFetch({
            url: "/api/user/login",
            method: "post",
            headers: {
                "Content-Type": "application/json"
            },
            body: {
                username: params.username,
                password: params.password
            }
        },{
            200:    (response) => onSuccess(response.body as UserInfoModel),
            401:    (response) => {
                removeAuthentication()
                onError(responseProblem(response))
            },
            other:  (response) => onError(responseProblem(response)),
            error: e => onError(exceptionProblem(e)),
        })
}

function _Logout (
    onSuccess:  () => void,
    onError:    (error: Problem) => void
){
        APIFetch({
            url: "/api/user/logout",
            method: "post",
            credentials: "include"
        },{
            200:    (response) => {
                removeAuthentication()
                onSuccess()
            },
            401:    (response) => {
                removeAuthentication()
                onError(responseProblem(response))
            },
            other:  (response) => onError(responseProblem(response)),
            error: e => onError(exceptionProblem(e)),
        })
}

function _GetUserInfo(
    onSuccess:  (data: UserInfoModel) => void,
    onError:    (error: Problem) => void
)  {

    APIFetch({
        url: "/api/user/info",
        method: "get",
        credentials: "include",
    },{
        200:    (response) => onSuccess(response.body as UserInfoModel),
        401:    (response) => {
            removeAuthentication()
            onError(responseProblem(response))
        },
        other:  (response) => onError(responseProblem(response)),
        error: e => onError(exceptionProblem(e)),
    })
}

function _GetChannel(
    params: { channelId: number },
    onSuccess:  (data: UserChannelModel) => void,
    onError:    (error: Problem) => void
)  {

    APIFetch({
        url: "/api/channel/info",
        method: "get",
        credentials: "include",
        headers: {
            "Content-Type": "application/json"
        },
        body: {
            channelId: params.channelId
        }
    },{
        200:    (response) => onSuccess(response.body.channels as UserChannelModel),
        401:    (response) => {
            removeAuthentication()
            onError(responseProblem(response))
        },
        other:  (response) => onError(responseProblem(response)),
        error: e => onError(exceptionProblem(e)),
    })
}

function _GetUserChannels(
    onSuccess:  (data: Array<UserChannelModel>) => void,
    onError:    (error: Problem) => void
)  {

    APIFetch({
        url: "/api/user/channels",
        method: "get",
        credentials: "include"
    },{
        200:    (response) => onSuccess(response.body.channels as Array<UserChannelModel>),
        401:    (response) => {
            removeAuthentication()
            onError(responseProblem(response))
        },
        other:  (response) => onError(responseProblem(response)),
        error: e => onError(exceptionProblem(e)),
    })
}

function _GetUserInvites(
    onSuccess:  (data: Array<UserChannelModel>) => void,
    onError:    (error: Problem) => void
)  {

    APIFetch({
        url: "/api/user/channels",
        method: "get",
        credentials: "include"
    },{
        200:    (response) => onSuccess(response.body.channels as Array<UserChannelModel>),
        401:    (response) => {
            removeAuthentication()
            onError(responseProblem(response))
        },
        other:  (response) => onError(responseProblem(response)),
        error: e => onError(exceptionProblem(e)),
    })
}

function _GetChannelInvites(
    onSuccess:  (data: Array<ChannelInviteModel>) => void,
    onError:    (error: Problem) => void
) {

    APIFetch({
        url: "/api/user/channel/invites",
        method: "get",
        credentials: "include"
    },{
        200:    (response) => onSuccess(response.body.invites as Array<ChannelInviteModel>),
        401:    (response) => {
            removeAuthentication()
            onError(responseProblem(response))
        },
        other:  (response) => onError(responseProblem(response)),
        error: e => onError(exceptionProblem(e)),
    })
}

function _GetChannelUserInvites(
    params: { channelId: number }, 
    onSuccess:  (result: ChannelInviteModel[]) => void,
    onError:    (error: Problem) => void
){
    APIFetch({
        url: "/api/channel/invites?channelId=" + params.channelId, 
        method: "get",
        credentials: "include"
        },{
        200:    (response) => onSuccess(response.body.invites as ChannelInviteModel[]),
        401:    (response) => {
            removeAuthentication()
            onError(responseProblem(response))
        },
        other:  (response) => onError(responseProblem(response)),
        error: e => onError(exceptionProblem(e)),
    })
}


type MessagesResults = {
    [channedId: string]: {
        channelId: number,
        channelName: string,
        channelMessages: Array<MessageModel>
    }
}

function _GetChannelMessages(
    params: Array<RequestMessages>,
    onSuccess:  (data: MessagesResults) => void,
    onError:    (error: Problem) => void
)  {

    APIFetch({
        url: "/api/channel/messages",
        method: "post",
        credentials: "include",
        headers: {
            "Content-Type": "application/json"
        },
        body: {load: params}
    },{
        200:    (response) => {
            onSuccess(response.body.results as MessagesResults)
        },
        401:    (response) => {
            removeAuthentication()
            onError(responseProblem(response))
        },
        other:  (response) => onError(responseProblem(response)),
        error: e => onError(exceptionProblem(e)),
    })
}

function _createMessage(
    params: {channelId: number, content: string},
    onSuccess:  (message: MessageModel) => void,
    onError:    (error: Problem) => void
)  {

    APIFetch({
        url: "/api/channel/messages/create",
        method: "post",
        credentials: "include",
        headers: {
            "Content-Type": "application/json"
        },
        body: {
            type: "text",
            ...params
        }
    },{
        201:   (response) => {
            onSuccess(response.body as MessageModel)
        },
        401:    response => {
            removeAuthentication()
            onError(responseProblem(response))
        },
        other:  (response) => onError(responseProblem(response)),
        error: e => onError(exceptionProblem(e)),
    })
}

function _UpdateLastRead (
    params: {channelId: number, messageId: number},
    onSuccess:  () => void,
    onError:    (error: Problem) => void
){
        APIFetch({
            url: `/api/channel/messages/read?channelId=${params.channelId}&messageId=${params.messageId}`,
            method: "put",
            credentials: "include"
        },{
            200:    (response) => {
                onSuccess()
            },
            401:    (response) => {
                removeAuthentication()
                onError(responseProblem(response))
            },
            other:  (response) => onError(responseProblem(response)),
            error: e => onError(exceptionProblem(e)),
        })
}


export const APICall = {

    CreateUser: _CreateUser,
    CreateInvite: _CreateInvite,
    CreateChannel: _CreateChannel,
    JoinChannel: _JoinChannel,
    RenameChannel: _RenameChannel,
    SearchChannels: _SearchChannels, 
    CancelInvite: _CancelInvite,
    LeaveChannel: _LeaveChannel,
    Login: _Login,
    Logout: _Logout,
    GetUserInfo: _GetUserInfo,
    GetUserChannels: _GetUserChannels,
    GetChannel: _GetChannel,
    GetUserInvites: _GetUserInvites,
    GetChannelInvites: _GetChannelInvites,
    GetChannelMessages: _GetChannelMessages,
    GetChannelUsers: _GetChannelUsers,
    GetChannelUserInvites: _GetChannelUserInvites,
    CreateMessage: _createMessage,
    UpdateLastRead: _UpdateLastRead,
    RemoveChannelInvites: _RemoveChannelInvites,
    RemoveChannelUsers: _RemoveChannelUsers,
    CreateUserInvite: _CreateUserInvite
}