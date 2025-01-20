import * as React from 'react';

import { useRef, useState, useEffect } from 'react';
import Grid from '@mui/material/Grid2';
import Box from '@mui/material/Box';
import { useParams } from 'react-router-dom';
import { APICall } from '../../../../APICall';
import { MessageList, UserChannelModel, UserPerm } from '../../../../APIModels';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, ListItemText, MenuItem, Select, Stack, TextField, Alert, Autocomplete, Snackbar, SnackbarCloseReason } from '@mui/material';
import { AlertMan, ChannelMan } from '../../../../AppMan';

type Props = {
    open: boolean,
    channel: UserChannelModel,
    onClose: () => void
}

export function CreateInvite(props: Props): React.ReactElement {

    const [username, setUsername] = useState("")
    const [invitePerms, setInvitePerms] = useState<Array<UserPerm>>(["read"])
    const [error, setError] = useState<string | undefined>()

    const handleUsernameChange = (event: any) => {
        setUsername(event.target.value);
    }

    const handleTypeChange = (value: UserPerm[]) => {
        setInvitePerms(value!!);
    }

    function onInvite() {
        
        if(username.length < 3){
            setError("Invalid username")
            return
        }

        APICall.CreateInvite({
            channelId: props.channel.channelId,
            username: username,
            invitePerms: invitePerms!!
        },
            invite => {
                AlertMan.push({type:"success", content: "Invite has been created"})
                props.onClose()
                //console.log(invite)
            },
            error => {
                setError(error.title)
            })
    }

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
                <DialogTitle>Invite user to channel</DialogTitle>
                <DialogContent>
                    
                    {
                        error && 
                            <Alert className="-ml-1 mb-1" severity="error">{error}</Alert>
                    }

                    <div className="flex flex-row mx-5 my-5">
                        <TextField value={username} onChange={e => handleUsernameChange(e)} type="text" fullWidth label="Username" variant="standard" />
                        <Autocomplete
                            multiple
                            limitTags={2}
                            id="multiple-limit-tags"
                            options={props.channel.channelPerms}
                            getOptionLabel={(option) => option}
                            defaultValue={[props.channel.channelPerms[0]]}
                            value={invitePerms}
                            onChange={(event, newInputValue) =>
                                handleTypeChange(newInputValue)
                            }
                            renderInput={(params) => (
                                <TextField {...params} label="Permissions" placeholder="Permissions" />
                            )}
                            sx={{ marginLeft: "20px", width: '600px' }}
                        />
                    </div>

                    
                </DialogContent>
                <DialogActions>
                    <Button onClick={props.onClose}>Cancel</Button>
                    <Button onClick={onInvite}>Invite</Button>

                </DialogActions>
            </Dialog>

        </>
    )
}