import React, { ReactElement, useEffect, useReducer, useState } from "react"
import { MessageModel, UserChannelModel, UserData } from "../../../../APIModels"
import { channel } from "diagnostics_channel"
import { ListItem, ListItemAvatar, Avatar, ListItemText, Typography, Divider } from "@mui/material"
import { Button } from "@mui/base/Button"
import { Link, useNavigate } from 'react-router-dom';
import { ChannelMan, MessageMan } from "../../../../AppMan"



function getMEssageTime(m: MessageModel) {
    var dt = new Date(m.timestamp * 1000)
    const t = dt.toLocaleTimeString()
    return t.split(":")[0] + ":" + t.split(":")[1]
}

function getSystemMessageText(m: MessageModel) {
    if (m.type == "text") {
        return ""
    }

    if (m.type == "channel-create") {
        return "Channel created"
    }

    if (m.type == "channel-rename") {
        return `Channel renamed`
    }
}

type Props = {
    user?: UserData
    channel: UserChannelModel
    selected: boolean
}


export function Channel(props: Props) {

    const [, forceUpdate] = useReducer(x => x + 1, 0)
    const [lastMessage, setLastMessage] = useState<MessageModel>()
    const link = "/home/channel/" + props.channel.channelId
    const channelId = props.channel.channelId
    

    useEffect(() => {
        const listener = ChannelMan.addChannelListener(props.channel.channelId, channel => {
            forceUpdate()
        })

        return () => ChannelMan.removeListener(listener)
    }, [])

    useEffect(() => {

        const messages = MessageMan.messages[channelId]
        if(!messages){
            MessageMan.fetchMessages(channelId, messages_ => {
                if(messages_)
                    setLastMessage(messages_[messages_.length-1])
                
            }, error => {

            })
        }
        if(messages)
            setLastMessage(messages[messages.length-1])

        const listener = MessageMan.addMessagesListener(channelId, messages => {
            if(messages)
                setLastMessage(messages[messages.length-1])
        })

        return () => MessageMan.removeMessagesListener(listener)
    }, [])

    //console.log(`lastMessage: ${props.channel.lastMessage } lastRead: ${props.channel.lastRead}`)
    const bold = props.channel.lastMessage > props.channel.lastRead

    return (
        <>
            <Link to={link} >
            <Button className={`hover:bg-gray-200 cursor-pointer w-full h-[80px] overflow-hidden ${props.selected ? "bg-gray-200 " : ""}`}>
                <div className={`channel flex flex-col w-full items-start p-2 ${bold ? "font-bold" : ""}`}>
                    <div className="title font-medium">
                        {props.channel.channelName}
                    </div>

                    {lastMessage &&
                        <div className={`last-message flex flex-row w-full justify-between text-sm `}>

                            {lastMessage.type == "text" &&

                                <div className="last-message flex flex-row justify-start w-[85%] ">
                                    <div className="last-message-user w-[40%] overflow-hidden text-start">
                                        {props.user && lastMessage.userId==props.user.userId ? "You" : lastMessage.username}:
                                    </div>
                                    <div className="last-message-content ps-4 text-start w-full overflow-hidden">
                                        {lastMessage.content}
                                    </div>
                                </div>
                            }

                            {
                                lastMessage.type != "text" &&
                                <div className="last-message flex flex-row justify-start max-w-[80%]">
                                    <div className="last-message-content ps-1 text-start italic">
                                        {getSystemMessageText(lastMessage)}
                                    </div>
                                </div>

                            }

                            <div className="last-message-time font-extralight">
                                {getMEssageTime(lastMessage)}
                            </div>


                        </div>

                    }


                </div>
            </Button>
            </Link>
            <Divider variant="fullWidth" component="li" />
        </>
    )


}


/*

        <ListItem alignItems="flex-start" sx={{marginTop: -1, height: 100, overflow: "hidden", cursor: "pointer"}}>
            <ListItemText
                primary="Channel title here"
                secondary={
                    <>
                        <Typography
                            component="span"
                            variant="body2"
                            sx={{ color: 'text.primary', display: 'inline', paddingRight: 1 }}
                        >
                            mark55: 
                        </Typography>
                   
                        {"I'll be in your neighborhood doing errands this… I'll be in your neighborhood doing errands this… I'll be in your neighborhood doing errands this…"}
                    </>
                }
            />
        </ListItem>
*/