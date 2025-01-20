import * as React from 'react';

import { useState, useEffect, useRef } from 'react';
import Box from '@mui/material/Box';
import { MessageInput } from './MessageInput';
import {MessageList, MessageModel, ResultMessages, UserChannelModel, UserData } from '../../../../APIModels';
import { CircularProgress, ListItemText, Stack } from '@mui/material';
import { Message } from './Message';
import { AlertMan, ChannelMan, MessageMan, SessionMan } from '../../../../AppMan';
import { channel } from 'diagnostics_channel';
import { APICall, Problem } from '../../../../APICall';
import { error } from 'console';


type State =
  | { type: 'begin' }
  | { type: 'loading'; url: string }
  | { type: 'idle'; payload: string; url: string }
  | { type: 'error'; error: Error; url: string };

type Action = {
    type: "idle" | "loading" | "load-error"
}


function reducer(state, action) {
    switch (action.type) {
      case "increment":
        return { ...state, count: state.count + 1 };
      case "decrement":
        return { ...state, count: state.count - 1 };
      default:
        return "Unrecognized command";
    }
  }


/*
function loadMore(
    channelId: number, 
    currentMessages: Array<MessageModel> | undefined, 
    onSucess: (messages: Array<MessageModel>) => void, 
    onError?: (error: Problem) => void){
    
    const request: {
        channelId: number,
        lastMessage?: number
    } = {
        channelId: channelId
    }
    if(currentMessages && currentMessages.length > 0)
        request.lastMessage = currentMessages[0].messageId


    ChannelMan

    ChannelMan.loadMessages([request],
        results => {
            const messages = results[channelId]

            onSucess(messages)
        },
        onError
    )
}
*/
type ScrollPos = "top" | "botttom" | "middle"


type Props = {
    user: UserData
    channel: UserChannelModel
}

export function MessageView(props: Props) {
    const divRef = useRef<any>()
    const loadedAll = useRef(false)
    const blockLoadMore = useRef(false)
    const lastRead = useRef(props.channel.lastRead)
    const [scrollPos, setScrollPos ]= useState<ScrollPos>("botttom")
    const [messageList, setMessageList] = useState<MessageList>()
    const [loading, setLoading] = useState(false)

    const currentUser = SessionMan.currentUser!
    const channelId = props.channel.channelId
    const scrollToBottom = () => {
        const container = divRef.current;
        if (container) {
            container.scrollTop = container.scrollHeight;
        }
    }

    useEffect(() => {
        //when channel changes, clear messages from previous channel and load from current channel
        lastRead.current = props.channel.lastRead
        loadedAll.current = false
        setScrollPos("botttom")
        setMessageList(undefined)
        
        let messages = MessageMan.messages[channelId]
        if(!messages){
            setLoading(true)
            const cancel = MessageMan.fetchMessages(channelId, messages_ => {
                if(messages_ && messages_.length < 20)
                    loadedAll.current = true
                setMessageList(messages_)
                setLoading(false)
            }, error => console.log(error)) //TODO error
        }

        if(messages && messages.length < 20)
            loadedAll.current = true
        
        setMessageList(messages)

        const listener = MessageMan.addMessagesListener(channelId, messages => {
            setMessageList(messages)
        })

        return () => MessageMan.removeMessagesListener(listener)
    }, [props.channel])


    useEffect(() => {
        //when new message received 

        if(scrollPos == "botttom")
            scrollToBottom()

        
    }, [messageList, scrollPos])


    useEffect(() => {
        //keep track of the scroll position

        const interval = setInterval(() => {
            const { scrollTop, scrollHeight, clientHeight } = divRef.current;
        

            //console.log(scrollTop)
            if(scrollTop < 400){
                if(scrollPos != "top" && (scrollHeight > clientHeight+150)){
                    //console.log("scrollTop: "+scrollTop)
                    //console.log("scrollHeight: "+scrollHeight)
                    //console.log("clientHeight: "+clientHeight)
                    //console.log("")
                    setScrollPos("top")
                }
            } else if (scrollTop + clientHeight > scrollHeight - 10){
                if(scrollPos != "botttom"){
                    setScrollPos("botttom")
                }       
            } else {
                if(scrollPos != "middle"){
                    blockLoadMore.current = false
                    setScrollPos("middle")
                }
            }

            
        }, 100)


        return () => clearInterval(interval)

    }, [scrollPos])


    useEffect(() => {
        //load more messages when user scrolls to top´

        //console.log("scrollPos: "+scrollPos)
        //console.log("loading: "+loading)
        //console.log("messageList: "+messageList?.length)
        //console.log("loadedAll: "+loadedAll.current)
        //console.log("blockLoadMore: "+blockLoadMore.current)
        //console.log("")

    
        if(scrollPos == "top" && !loading && messageList && !loadedAll.current && !blockLoadMore.current){
            blockLoadMore.current = true
            setLoading(true)
            //console.log("fetching more!!")
            MessageMan.fetchMessages(channelId, messages => {
                if(messages && messageList.length == messages.length)
                    loadedAll.current = true
    
                setMessageList(messages)
                setLoading(false) 
            }, error => {
                AlertMan.push({type: "error", content:"Failed to load channel messages: "+ error.title})
                setLoading(false) 
            })
        }

    }, [scrollPos, messageList, loading])

    useEffect(() => {
        //update last message read


        if(scrollPos == "botttom"){
            if(messageList && messageList.length > 0){
                const lastMessage = messageList[messageList.length-1]
                if(lastMessage.userId != props.user.userId || lastMessage.type != "text"){
                    if(lastMessage.messageId > lastRead.current){
                        APICall.UpdateLastRead({
                            channelId: channelId, 
                            messageId: lastMessage.messageId
                        },
                            () => {
                                
                            },
                            error => console.log(error)
                        )
                        const channel = ChannelMan.getChannel(channelId)
                        if(channel){
                            channel.lastRead = lastMessage.messageId
                            ChannelMan.updateChannel(channel)
                        }
                        lastRead.current = lastMessage.messageId
                    }
                }
            }
        }

    }, [props.channel, scrollPos, messageList])

    return (
        <div className="flex flex-col h-full">
            
            <div className="overflow-y-auto h-[calc(100vh-180px)]" ref={divRef} >
                {(!messageList || loading) && 
                    <div className="w-full text-center">
                        <CircularProgress />
                    </div>
                 }
                {messageList && messageList.sort((m1, m2) => m1.messageId - m2.messageId).map(it => 
                    <Message key={it.messageId} user={currentUser} message={it} />
                ) 
                }
            </div>
            

            <div className="message-input p-2 mt-1">
         
                <MessageInput channel={props.channel} />
            </div>
            
        
        </div>
    )
}