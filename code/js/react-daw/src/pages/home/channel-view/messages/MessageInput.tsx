import React, { useEffect, useState } from 'react';

import Button from '@mui/material/Button';
import SendIcon from '@mui/icons-material/Send';
import Paper from '@mui/material/Paper';
import InputBase from '@mui/material/InputBase';
import { common } from '@mui/material/colors';
import { TextField } from '@mui/material';
import { APICall } from '../../../../APICall';
import { useParams } from 'react-router-dom';
import { AlertMan, ChannelMan, MessageMan } from '../../../../AppMan';
import { UserChannelModel } from '../../../../APIModels';
import { channel } from 'diagnostics_channel';


type Props = {
    channel: UserChannelModel
}


export function MessageInput(props: Props){

    const [content, setContent] = useState("")
    const {cid} = useParams() //TODO disable button

    function onSend(){
        if(!props.channel.channelPerms.includes("write") || !cid || !content || !content.length)
            return
        
        const channelId = parseInt(cid);
        APICall.CreateMessage({
            channelId: channelId,
            content: content
        },
            message => {
                MessageMan.newMessage(channelId, message)
                setContent("")
            },
            error => {
               AlertMan.push({type: "error", content: error.title})
            }
        )
    }
    
    return (
        <div className="flex items-center w-full p-2 bg-gray-100 rounded shadow-md"
        >
            {/*<TextField sx={{ ml: 1, flex: 1 }} className="" id="outlined-basic" value={content} onChange={e => setContent(e.target.value)} label="Type a message"  />*/}
            <InputBase disabled={!props.channel.channelPerms.includes("write")}
                sx={{ ml: 1, flex: 1 }}
                placeholder="Type a message"
                inputProps={{ 'aria-label': 'type message' }}
                onKeyDown={e => e.key === 'Enter' ? onSend(): ''}
                value={content} onChange={e => setContent(e.target.value)}
            />
            <Button disabled={!props.channel.channelPerms.includes("write")} onClick={() => onSend()} 
                sx={{backgroundColor: common.black}} variant="contained" endIcon={<SendIcon />}> Send </Button>
        </div>
    )
}