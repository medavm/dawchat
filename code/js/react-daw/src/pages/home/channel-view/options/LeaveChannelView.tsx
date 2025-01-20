import * as React from 'react';

import { useRef, useState, useEffect } from 'react';
import Grid from '@mui/material/Grid2';
import Box from '@mui/material/Box';
import { useParams } from 'react-router-dom';
import { APICall } from '../../../../APICall';
import {  MessageList, UserChannelModel } from '../../../../APIModels';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, ListItemText, MenuItem, Select, Stack, TextField, Alert, SelectChangeEvent } from '@mui/material';
import { AlertMan, ChannelMan } from '../../../../AppMan';

type Props = {
    channel: UserChannelModel,
    open: boolean
    onClose: () => void
}

export function LeaveChannelView(props: Props): React.ReactElement {

    const [error, setError] = useState<string | undefined>()
    
    const channelId = props.channel.channelId
    

    function onLeave(){

        APICall.LeaveChannel({
            channelId: channelId
        },
            () => {
                AlertMan.push({type: "info", content: `You left channel '${props.channel.channelName}'`})
                ChannelMan.removeChannel(channelId)
                props.onClose()
            },
            error => {
                setError(error.title)
            }
        )
    }

    return (
        <>
            <Dialog open={props.open}>
                <DialogTitle>Leave channel</DialogTitle>
                <DialogContent>   
                    <div className="">
                        {
                            error && 
                                <Alert className="-ml-1 mb-1" severity="error">{error}</Alert>
                        }
                        {
                            !error && <label>Are you sure?</label>
                        }
                        
                    </div>
                </DialogContent>
                <DialogActions>
                    <Button onClick={props.onClose}>Cancel</Button>
                    <Button onClick={onLeave} color="error" type='submit'>Leave</Button>
                </DialogActions>
            </Dialog>
        </>
    )
}