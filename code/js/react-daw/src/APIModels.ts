

export type AppUser = {
    userId: number,
    username: string,
}

export type MessageType = "channel-create" | "channel-rename" | "text"
export type MessageModel = {
    messageId: number,
    userId: number,
    username: string
    type: MessageType,
    content: string,
    timestamp: number
}

export type MessageEventModel = {
    channelId: number,
    channelName: string,
    messageId: number,
    userId: number,
    username: string
    type: MessageType,
    content: string,
    timestamp: number
}

export type RenameEventModel = {
    channelId: number,
    channelName: string,
}

export type UserInfoModel = {
    userId: number,
    username: string,
    createdAt: number
}

export type UserInviteModel = {
    token: string
    createdBy: number
    acceptedBy?: number
    acceptedName?: string
    timestamp: number
}

export type UserPerm = "read" | "write" | "invite" | "rename" | "remove-users"
export type ChannelType = "private" | "public"

export type UserChannelModel = {
    channelId: number,
    channelName: string,
    channelType: ChannelType,
    channelPerms: Array<UserPerm>,
    channelOwner: number,
    lastMessage: number,
    lastRead: number,
    joinedAt: number,
}

export type SearchedChannelsModel = {
        channelId: number,
        channelName: string,
        channelType: ChannelType
}


export type ChannelUserModel = {
    userId: number,
    username: string,
    userPerms: Array<UserPerm>,
    joinedAt: number,
}

export type ChannelInviteModel = {
    inviteId: number,
    senderId: number,
    senderName: string,
    recipientId: number,
    recipientName: string,
    channelId: number,
    channelName: string,
    channelType: ChannelType
    invitePerms: Array<UserPerm>
    timestamp: number
}

export type CreatedChannelInviteModel = {
    inviteId: number,
    senderId: number,
    recipientId: string,
    channelId: number,
    invitePerms: Array<UserPerm>
    timestamp: number
}

export type UserData = {
    userId: number,
    username : string
}




export type MessageList = Array<MessageModel>


export type RequestMessages = {
    channelId: number,
    lastMessage?: number,
    limit?: number
}

export type ResultMessages = {
    [channedId: number]: Array<MessageModel>
}


