








import * as React from 'react';

import { useRef, useState, useEffect } from 'react';
import Grid from '@mui/material/Grid2';
import Box from '@mui/material/Box';
import { useParams } from 'react-router-dom';
import { APICall } from '../../../APICall';
import {  MessageList, UserChannelModel, UserInviteModel } from '../../../APIModels';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, ListItemText, MenuItem, Select, Stack, TextField, Alert, SelectChangeEvent } from '@mui/material';
import { AlertMan, ChannelMan } from '../../../AppMan';


function getLink(invite: UserInviteModel){
    const host = window.location.host
    return `http://${host}/login?t=${invite.token}`
}

type Props = {
    onClose: () => void
}

export function AppInviteView(props: Props){

    const [invite, setInvite] = useState<UserInviteModel>()
    const [error, setError] = useState<string>()
    
    useEffect(() => {
        APICall.CreateUserInvite(
            invite => {
                setInvite(invite)
            },
            error => {
                setError(error.title)
            }
        )
    }, [])


    function onCopy(){

        if(invite && navigator && navigator.clipboard){
            navigator.clipboard.writeText(getLink(invite))
            .then(() => {
               
            })
            .catch((err) => {
            
            });
        }
        
    }

    return (
        <>
            <Dialog open={true}>
                <DialogTitle>Account invite</DialogTitle>
                <DialogContent>   
                    <div className="min-w-[300px]">
                        {
                            error && 
                                <Alert className="-ml-1 mb-1" severity="error">{error}</Alert>
                        }
                        {
                            invite && 
                              
                                <div className="text-gray-500 italic">
                                    {getLink(invite)}
                                </div>
                        }
                        
                    </div>
                </DialogContent>
                <DialogActions>
                    <Button onClick={props.onClose} color="error">Close</Button>
                    <Button onClick={onCopy} color="info">Copy</Button>
                </DialogActions>
            </Dialog>
        </>
    )
}