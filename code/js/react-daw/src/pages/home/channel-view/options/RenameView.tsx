import * as React from 'react';

import { useRef, useState, useEffect } from 'react';
import Grid from '@mui/material/Grid2';
import Box from '@mui/material/Box';
import { useParams } from 'react-router-dom';
import { APICall } from '../../../../APICall';
import { MessageList, UserChannelModel, ChannelUserModel } from '../../../../APIModels';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, ListItemText, Stack, Alert } from '@mui/material';
import { AlertMan, ChannelMan } from '../../../../AppMan';

type Props = {
    open: boolean
    onClose: () => void
    channel: UserChannelModel
}

export function RenameView(props: Props): React.ReactElement {

    const [channelName, setChannelName] = useState("")
    const [error, setError] = useState<string | undefined>()

    function OnChangeName() {

        const channelId = props.channel.channelId
        APICall.RenameChannel(
            {
                channelId: channelId,
                name: channelName
            },
            () => {

                const channel = ChannelMan.getChannel(channelId)
                if(channel){
                    channel.channelName = channelName
                    ChannelMan.updateChannel(channel)
                }

                AlertMan.push({type:"success", content: "Renamed channel successfully"})
                props.onClose()

            },
            error => setError(error.title) 
        )

    }

    let counter = 0
    return (
        <>
            <Dialog open={props.open}>
                <DialogTitle>Rename Channel</DialogTitle>
                <DialogContent>
                    <div className="min-w-[600] text-start">
                        {
                            error && 
                                <Alert className="-ml-1 mb-1" severity="error">{error}</Alert>
                        }

                        <div className="w-full text-start">
                            <b className='me-2'>Current name:</b>  {props.channel.channelName} 
                        </div>

                        <div className="w-full text-start mt-4">
                            <b className='me-2'>New name:</b>
                            <input value={channelName} onChange={(ev) => setChannelName(ev.target.value)} className="rounded-md bg-slate-200 ps-2 h-7" placeholder="name" />   
                        </div>


                    </div>
                </DialogContent>
                <DialogActions>
                    <Button onClick={props.onClose}>Cancel</Button>                    
                    <Button onClick={OnChangeName} color="error"> Rename</Button>
                </DialogActions>
            </Dialog>
        </>
    )
}