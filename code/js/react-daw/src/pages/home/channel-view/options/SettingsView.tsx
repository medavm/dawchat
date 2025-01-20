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
    channelUsers?: Array<ChannelUserModel>
}


export function SettingsView(props: Props): React.ReactElement {

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
            <Dialog
                open={props.open}
                PaperProps={{
                    component: 'form',
                    onSubmit: (event: React.FormEvent<HTMLFormElement>) => {
                        event.preventDefault();
                        props.onClose();
                    },
                }}
            >
                <DialogTitle>Settings</DialogTitle>
                <DialogContent>
                    <div className="min-w-[800]">
                        {error && <Alert className="-ml-1 mb-1" severity="error">{error}</Alert>}

                        <div className="flex flex-col items-center">
                            <div className='flex flex-row justify-center mb-6'>
                                <b className='me-2'>Channel name:</b>  {props.channel.channelName}
                            </div>
                        </div>


                        <div className="flex flex-row mb-10">
                            <div className="flex flex-col min-w-[250] ms-2 me-[80px]">
                                <h4>Channel Users</h4>
                                <Box sx={{ backgroundColor: "rgb(226 232 240 / var(--tw-bg-opacity, 1))", padding: 2, maxHeight: "15vh", overflow: "auto" }}>
                                    {props.channelUsers?.map(
                                        user => <h6 key={counter++}> {user.username} </h6>
                                    )}
                                </Box>
                            </div>
                            <div className="flex flex-col min-w-[250] me-2">
                                <h4>User Permissions</h4>
                                <Box sx={{ backgroundColor: "rgb(226 232 240 / var(--tw-bg-opacity, 1))", padding: 2, maxHeight: "15vh", overflow: "auto" }}>
                                    {props.channel.channelPerms!!.map(
                                        perm => <h6 key={counter++}> {perm} </h6>
                                    )}
                                </Box>
                            </div>
                        </div>

                        <div className="flex flex-col items-center">
                            <div className='flex flex-row justify-center'>
                                <b className='me-2'>Rename channel:</b>
                                <input value={channelName} onChange={(ev) => setChannelName(ev.target.value)} className="rounded-md bg-slate-200 ps-2 h-7" placeholder="name" />
                                
                            </div>
                        </div>
                    </div>
                </DialogContent>
                <DialogActions>
                    <Button onClick={props.onClose}>Cancel</Button>                    
                    <Button onClick={OnChangeName} color="error"> Rename </Button>
                </DialogActions>
            </Dialog>
        </>
    )
}