import * as React from 'react';

import { useRef, useState, useEffect } from 'react';
import Grid from '@mui/material/Grid2';
import Box from '@mui/material/Box';
import { MessageInput } from './MessageInput';
import { useParams } from 'react-router-dom';
import { APICall } from '../../../../APICall';
import { ListItemText, Stack, TextField } from '@mui/material';
import { timeStamp } from 'console';
import { MessageModel, UserData } from '../../../../APIModels';
import { SessionMan } from '../../../../AppMan';


function getContent(m: MessageModel){
    if(m.type == "text"){
        return m.content
    }

    if(m.type == "channel-create"){
        return "Channel has been created"
    }

    if(m.type == "channel-rename"){
        return `Channel renamed to '${m.content}'`
    }
}

function getPos(userId: number,  m: MessageModel){
    if(m.type == "text"){
        if(m.userId == userId)
            return "right"
        return "left"
    }

    return "center"
}


function getMessageDate(m: MessageModel){
    var dt = new Date(m.timestamp * 1000)
    const t = dt.toLocaleTimeString()
    const timeMin = t.split(":")[0] + ":" + t.split(":")[1]
    const d = dt.toLocaleDateString()
    return d + " " + timeMin
}



type Prop = {
    user: UserData
    message: MessageModel
}

export function Message(props: Prop) {


    const pos = getPos(props.user.userId, props.message)
    //console.log(props.user.userId)
    //console.log(props.message.userId)
    //console.log("")

    if(pos == "center"){
        return (
            <div className='items-center justify-center flex flex-row'>
                <Box className="bg-slate-300 rounded-md m-2" sx={{ alignContent:"center", minHeight:"40px", minWidth: "200px", maxWidth: "300px" }}>
                    <p className="italic font-extralight " style={{ textAlign: "start", paddingLeft: "50px", paddingRight: "20px" }}>{getContent(props.message)}</p>
                </Box>
            </div>
        )
    }
 
    if(pos == "left"){
        return (
            <div className='content-end'>
            <Box className="bg-slate-100 rounded-md m-2" sx={{marginRight:"80px", minWidth: "200px", maxWidth: "400px" }}>
                <b style={{ textAlign: "start", paddingLeft: "10px" }}>{props.user.userId==props.message.userId? "You" : props.message.username}</b>
                <p style={{ textAlign: "start", paddingLeft: "50px", paddingRight: "20px" }}>{getContent(props.message)}</p>
                <p className=" italic font-extralight text-xs" style={{ textAlign: "end",   paddingRight: "10px" }}>{getMessageDate(props.message)}</p>
            </Box>
        </div>
        )
    }

    if(pos == "right"){
        return (
            <div className='content-end'>
            <Box className="bg-slate-100 rounded-md float-right m-2" sx={{ marginLeft:"80px", minWidth: "300px", maxWidth: "400px" }}>
                <b style={{ textAlign: "start", paddingLeft: "10px" }}>{props.user.userId==props.message.userId? "You" : props.message.username}</b>
                <p style={{ textAlign: "start", paddingLeft: "50px", paddingRight: "20px" }}>{getContent(props.message)}</p>
                <p className="italic font-extralight text-xs" style={{ textAlign: "end", paddingRight: "10px"}}>{getMessageDate(props.message)}</p>
            </Box>
        </div>
        )
    }

        
    return (
        <div className="italic font-extralight">
            Invalid message type
        </div>
    )

}