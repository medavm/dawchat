import React, { ReactElement, useEffect, useState } from "react"
import { SearchedChannelsModel, UserChannelModel } from "../../../../APIModels"
import { channel } from "diagnostics_channel"
import { ListItem, ListItemAvatar, Avatar, ListItemText, Typography, Divider, Button } from "@mui/material"
import { useNavigate } from 'react-router-dom';
import { AlertMan, ChannelMan } from "../../../../AppMan"
import { Check } from "@mui/icons-material"
import { APICall } from "../../../../APICall";


function getRandomInt(max) {
    return Math.floor(Math.random() * max);
}

type Props = {
    channel: SearchedChannelsModel
    children?: any
}

type JoinState = "joined" | "joining" | "not-join"



export function SearchChannel(props: Props) {

    const [joined, setJoined] = useState<JoinState>("not-join")

    useEffect(() => {
        const channel = ChannelMan.getChannel(props.channel.channelId)
        if(channel)
            setJoined("joined")
    }, [])

    let bold = false
    if (getRandomInt(100) < 20)
        bold = true

    function onJoin() {
        setJoined("joining")

        APICall.JoinChannel({
            channelId: props.channel.channelId
        },
            channel => {
                ChannelMan.addChannel(channel)
                setJoined("joined")
                //AlertMan.push({type:"success", content: "Channel joined succesfully"})
                
            },
            error => {
                setJoined("not-join") 
                AlertMan.push({type:"error", content: error.title})
            }
        )

    }


    return (
        <>
            <div className="hover:bg-gray-200 cursor-pointer w-full h-[80px] overflow-hidden text-center flex items-center">
                <div className="invite flex flex-row w-full px-2 justify-between">


                    <div className="title font-medium mt-1 mx-2">
                        {props.channel?.channelName}
                    </div>

                    <div className="">
                        {(joined == "not-join") &&
                            <Button onClick={onJoin} variant="outlined" color="success">
                                Join
                            </Button>}
                        {(joined == "joining") &&
                            <Button disabled variant="contained">
                                Joining...
                            </Button>}
                        {(joined == "joined") &&
                            <Button disabled variant="contained">
                                Joined
                            </Button>}
                    </div>
                </div>
            </div>
            <Divider variant="fullWidth" component="li" />
        </>
    )


}

