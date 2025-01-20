
import {
    AppUser, UserData, MessageList, CreatedChannelInviteModel, ChannelInviteModel,
    MessageModel, RequestMessages, ResultMessages, UserPerm, UserChannelModel,
    ChannelType,
    SearchedChannelsModel,
    MessageType,
    MessageEventModel,
    RenameEventModel
} from "./APIModels"
import { APICall, Problem } from "./APICall"



type ChannelListUpdate = (channels?: Array<UserChannelModel>) => void
type ChannelUpdate = (channel?: UserChannelModel) => void
type ChannelMessagesUpdate = (messages?: Array<MessageModel>) => void
type OnSessionCallback = (user?: UserData) => void
type ErrorCallback = (error: Problem) => void

type ListenerRef = {
    type: "channel-list" | "channel-info" | "channel-messages" | "user-session"
    channelId?: number
    callback: ChannelListUpdate | ChannelUpdate | ChannelMessagesUpdate | OnSessionCallback
}

class _ChannelMan {

    private channels: {
        [channelId: number]: UserChannelModel
    } | undefined = undefined

    private callbacks = {
        channelUpdate:    <{[channelId: number]: Array<ChannelUpdate> }> {},
        channelListUpdate: <Array<ChannelListUpdate>>[],
    }


    fetchChannelList(onSuccess: ChannelListUpdate, onError?: ErrorCallback) {
        

        let cancel = false
        APICall.GetUserChannels(list => {

            this.channels = {}
            list.forEach(channel => this.channels![channel.channelId] = channel)

            if(!cancel)
                onSuccess(list)
            
            //channel list updated callbacks
            this.callbacks.channelListUpdate.forEach(cb => cb(list))

            //channel updated callbacks
            list.forEach(channel => {
                if(this.callbacks.channelUpdate[channel.channelId]){
                    this.callbacks.channelUpdate[channel.channelId].forEach(cb => {
                        cb(channel)
                    })
                }
            })

            //tell components about channels that dont exist anymore
            Object.entries(this.callbacks.channelUpdate).forEach(([channelId, callbacks]) => {
                if (!this.channels![channelId]) {
                    callbacks.forEach(callback => {
                        callback(undefined)
                    })
                }
            })

        }, error => {
            if (onError && !cancel)
                onError(error)
        })


        return () => {
            cancel = true
        }
    }

    getChannelList() {
        if(!this.channels)
            return undefined

        const channels = Object.values(this.channels)
        return channels
    }

    getChannel(channedId: number){
        if(!this.channels)
            return undefined
        return this.channels[channedId]
    }

    addChannelListListener(onUpdate: ChannelListUpdate): ListenerRef {
        if (!this.callbacks.channelListUpdate.find(cb => cb == onUpdate))
            this.callbacks.channelListUpdate.push(onUpdate)

        return {
            type: "channel-list",
            callback: onUpdate
        }
    }

    addChannelListener(channelId: number, onUpdate: ChannelUpdate): ListenerRef {
        if (!this.callbacks.channelUpdate[channelId])
            this.callbacks.channelUpdate[channelId] = []

        if (!this.callbacks.channelUpdate[channelId].find(cb => cb == onUpdate))
            this.callbacks.channelUpdate[channelId].push(onUpdate)

        return {
            type: "channel-info",
            channelId: channelId,
            callback: onUpdate
        }
    }

    addChannel(channel: UserChannelModel){
        if(!this.channels)
            return

        this.channels[channel.channelId] = channel
        const list = Object.values(this.channels)
        this.callbacks.channelListUpdate.forEach(cb => cb(list))
    }

    removeChannel(channelId: number){
        if(!this.channels)
            return
        
        if(this.channels[channelId]){
            delete this.channels[channelId]
            
            const list = Object.values(this.channels)
            this.callbacks.channelListUpdate.forEach(cb => cb(list))

            //tell components about channels that dont exist anymore
            if(this.callbacks.channelUpdate[channelId]){
                this.callbacks.channelUpdate[channelId].forEach(cb => {
                    cb(undefined)
                })
            }
        }


        MessageMan.clearMessages(channelId)
    }

    updateChannel(channel: UserChannelModel){
        if(!this.channels)
            return

        this.channels[channel.channelId] = channel
        const list = Object.values(this.channels)
        this.callbacks.channelListUpdate.forEach(cb => cb(list))

        if(this.callbacks.channelUpdate[channel.channelId]){
            this.callbacks.channelUpdate[channel.channelId].forEach(cb => {
                cb(channel)
            })
        }

    }

    removeListener(update: ListenerRef) {
        if (update.type == "channel-list")
            this.callbacks.channelListUpdate = this.callbacks.channelListUpdate.filter(cb => cb == update.callback)

        if (update.type == "channel-info" && update.channelId)
            if (this.callbacks.channelUpdate[update.channelId])
                this.callbacks.channelUpdate[update.channelId] = this.callbacks.channelUpdate[update.channelId].filter(cb => cb != update.callback)

    }

    clearAll(){
        this.callbacks.channelListUpdate = []
        this.callbacks.channelUpdate = {}
        this.channels = undefined
    }

    
}

export const ChannelMan = new _ChannelMan()


class _UserEvenMan{

    private listener: any = undefined
    
    onListeningState(){
        
    }

    startListening(){

        if(this.listener)
            this.listener.close();

        this.listener = new EventSource("/api/user/listener");

        this.listener.onopen = () => {
            console.log("EventSource connection established.");
        }

        this.listener.onmessage = (event) => {
            console.log("New event message:", event.data);
        }

        this.listener.addEventListener("channel-message", (event: MessageEvent) => {
            const message = JSON.parse(event.data) as MessageEventModel
            MessageMan.newMessage(message.channelId, message, false)
        })

        this.listener.addEventListener("channel-rename", (event) => {
            const eventData = JSON.parse(event.data) as RenameEventModel
            const channel = ChannelMan.getChannel(eventData.channelId)
            if(channel){
                channel.channelName = eventData.channelName
                ChannelMan.updateChannel(channel)
            }
        })

        this.listener.addEventListener("channel-join", (event) => {
            const eventData = JSON.parse(event.data) as UserChannelModel
            AlertMan.push({type: "info", content: `You joined channel '${eventData.channelName}'`})
            ChannelMan.addChannel(eventData)
        })

        this.listener.addEventListener("channel-leave", (event) => {
            const eventData = JSON.parse(event.data) as {channelId: number}
            const channel = ChannelMan.getChannel(eventData.channelId)
            if(channel)
                AlertMan.push({type: "info", content: `You left channel '${channel.channelName}'`})
            ChannelMan.removeChannel(eventData.channelId)
        })

        this.listener.addEventListener("channel-remove", (event) => {
            const eventData = JSON.parse(event.data) as {channelId: number}
            const channel = ChannelMan.getChannel(eventData.channelId)
            if(channel)
                AlertMan.push({type: "error", content: `You've been removed from '${channel.channelName}'`})
            ChannelMan.removeChannel(eventData.channelId)
        })

        this.listener.onerror = (err) => {
            console.error("EventSource failed:", err);
                setTimeout(() => {
                    if(this.listener){
                        console.log("Reconnecting listener...");
                        this.startListening()
                    }
                }, 1000)
            
            this.listener.close();
        }
    }

    stopListening(){
        if(this.listener){
            this.listener.close();
            this.listener = undefined
        }
            
    }


}
export const UserEventMan = new _UserEvenMan()


class _MessageMan {

    private updateCallbacks: { 
        [channelId: number]: Array<ChannelMessagesUpdate> 
    } = {}

    private fetching: Array<number> = []

    private waitingForFetch : {
        [channelId: number] : Array<{success: ChannelMessagesUpdate, error?: ErrorCallback}>
    } = {}

    messages : {
        [channelId: number] : Array<MessageModel>
    } = {}


    newMessage(channelId: number, message: MessageModel, asRead = true){
        
        if(!this.messages[channelId])
            this.messages[channelId] = []

        const latest = this.messages[channelId][this.messages[channelId].length-1]
        if(latest.messageId < message.messageId)
            this.messages[channelId] = [...this.messages[channelId], ...[message]]
        //else its probably the event, ignore

        const channel = ChannelMan.getChannel(channelId)
        if(channel){
            channel.lastMessage = message.messageId
            if(asRead)
                channel.lastRead = message.messageId
            ChannelMan.updateChannel(channel)
        }
        
        this.updateCallbacks[channelId]?.forEach(cb => {
            cb(this.messages[channelId])
        })
    }

    fetchMessages(channelId: number, onSuccess: ChannelMessagesUpdate,  onError?: ErrorCallback){
        
        if(!this.waitingForFetch[channelId])
            this.waitingForFetch[channelId] = []

        this.waitingForFetch[channelId].push({success: onSuccess, error: onError})

        const t = setTimeout(() => {
            //this timeout is going to fech all waiting list minus whats already being feched
            const toFetch = Object.keys(this.waitingForFetch)
            .filter(chId => !this.fetching.includes(parseInt(chId)))
            .map(chId => parseInt(chId))

            if(toFetch.length == 0){
                //nothing remaining to fech for this timeout
                return
            }
            
            this.fetching.push(...toFetch)
            const requets = toFetch.map(chId => ({
                channelId: chId,
                lastMessage: this.messages[chId] ? this.messages[chId][0].messageId : undefined,
                limit: 20
            }))
            
            APICall.GetChannelMessages(requets, results => {
    
                const resultList = Object.values(results)
                resultList.forEach(result => {

                    if(!this.messages[result.channelId])
                        this.messages[result.channelId] = []

                    this.messages[result.channelId] = [...result.channelMessages, ...this.messages[result.channelId]]
                    this.messages[result.channelId].sort((m1, m2) => m1.messageId - m2.messageId)
                })

                //call componets waiting for this fetch
                toFetch.forEach(chId => {
                    if(this.waitingForFetch[chId]){
                        this.waitingForFetch[chId].forEach(e => {
                            e.success(this.messages[chId])
                        })

                        delete this.waitingForFetch[chId]
                    }

                    this.fetching = this.fetching.filter(chId2 => chId2 != chId)
                })


            }, 
            error => {
                
                toFetch.forEach(chId => {
                    if(this.waitingForFetch[chId]){
                        this.waitingForFetch[chId].forEach(e => {
                            if(e.error)
                                e.error(error)
                        })

                        delete this.waitingForFetch[chId]
                    }

                    this.fetching = this.fetching.filter(chId2 => chId2 != chId)
                })
            })

        }, 200);

        //cancel function
        return () => {
            if(this.waitingForFetch[channelId])
                this.waitingForFetch[channelId].filter(e => e.success != e.success)
        }

    }
    
    addMessagesListener(channelId: number, onUpdate: ChannelMessagesUpdate): ListenerRef{
        if (!this.updateCallbacks[channelId])
            this.updateCallbacks[channelId] = []

        if (!this.updateCallbacks[channelId].find(cb => cb == onUpdate))
            this.updateCallbacks[channelId].push(onUpdate)
        
        return {
            type: "channel-messages",
            channelId: channelId,
            callback: onUpdate
        }
    }

    removeMessagesListener(listener: ListenerRef){
        if (listener.type == "channel-messages" && listener.channelId){
            if (this.updateCallbacks[listener.channelId]){
                //const cb = this.updateCallbacks[listener.channelId].find(cb => cb == listener.callback)

                this.updateCallbacks[listener.channelId] = this.updateCallbacks[listener.channelId].filter(cb => cb != listener.callback)
            }
        }
    }

    clearMessages(channedId: number){
        if(this.messages && this.messages[channedId])
            delete this.messages[channedId]
    }

    clearAll(){
        this.updateCallbacks = {}
        this.fetching = []
        this.waitingForFetch = {}
        this.messages = {}
    }
}


export const MessageMan = new _MessageMan()


class _SessionMan {

    private _user: UserData | undefined
    private _callbacks: Array<OnSessionCallback> = []

    constructor() {
        this._user = this.loadLocal()
    }

    private storeLocal(user?: UserData) {
        if (user)
            localStorage.setItem("UserData", JSON.stringify(user))
        else
            localStorage.removeItem("UserData")
    }

    private loadLocal(): UserData | undefined {
        const local = localStorage.getItem("UserData")
        if (local)
            return JSON.parse(local)
        else
            return undefined
    }

    get currentUser() {
        return this._user
    }

    updateUser(user?: UserData) {

        if(!user){
            MessageMan.clearAll()
            ChannelMan.clearAll()
        }

        this.storeLocal(user)
        this._user = user
        this._callbacks.forEach(cb => cb(user))

    } 

    onSessionUpdate(onUpdate: OnSessionCallback): ListenerRef {
        if (!this._callbacks.find(cb => cb == onUpdate))
            this._callbacks.push(onUpdate)

        return {
            type: "user-session",
            callback: onUpdate
        }
    }

    clearUpdate(update: ListenerRef) {
        this._callbacks = this._callbacks.filter(cb => cb != update.callback)
    }



}
export const SessionMan = new _SessionMan()




type Alert = {
    type: "error" | "success" | "info"
    content: string
}
type AlertCallback = (alert: Alert) => void

class _AlertMan{
    

    private callbacks: Array<AlertCallback> = []

    push(alert: Alert){
        this.callbacks.forEach(cb => {
            cb(alert)
        })
    }

    subscribe(onAlert: AlertCallback){
        if(!this.callbacks.find(cb => cb == onAlert)){
            this.callbacks.push(onAlert)
        }

        return onAlert
    }

    clearSubscription(onAlert: AlertCallback){
        this.callbacks = this.callbacks.filter(cb => cb != onAlert)
    }

}


export const AlertMan = new _AlertMan()