import * as React from 'react';

import { useRef, useState, useEffect } from 'react';
import Grid from '@mui/material/Grid2';
import Box from '@mui/material/Box';
import { useParams } from 'react-router-dom';
import { APICall } from '../../../../APICall';
import { MessageList, UserChannelModel, ChannelUserModel, ChannelInviteModel } from '../../../../APIModels';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, ListItemText, Stack, Alert, List, IconButton } from '@mui/material';
import { AlertMan, ChannelMan } from '../../../../AppMan';
import { Check, Close } from '@mui/icons-material';
import ReplayIcon from '@mui/icons-material/Replay';

type Props = {
    open: boolean
    onClose: () => void
    channel: UserChannelModel
    channelUsers?: Array<ChannelUserModel>
}



function User(name: string) {


    return (
        <div className="user">

        </div>

    )

}

export function UsersView(props: Props) {

    const [error, setError] = useState<string>()
    const [users, setUsers] = useState<ChannelUserModel[]>()
    const [invites, setInvites] = useState<ChannelInviteModel[]>()
    const [selectedUsers, setSelectedUsers] = useState<ChannelUserModel[]>([])
    const [selectedInvites, setSelectedInvites] = useState<ChannelInviteModel[]>([])
    const readyToClose = useRef(false)
    const channelId = props.channel.channelId

    useEffect(() => {
        //fech users

        APICall.GetChannelUsers({
            channelId: props.channel.channelId
        }, users => {
            setUsers(users)
        }, error => {
            setError(error.title)
        })

    }, [])


    useEffect(() => {
        //fetch invites

        APICall.GetChannelUserInvites({ channelId: channelId },
            invites => {
                setInvites(invites)
            },
            error => {
                setError(error.title)
            }
        )

    }, [])



    function onRemove(){

        if(selectedUsers.length > 0){
            APICall.RemoveChannelUsers({ 
                channelId: channelId,
                toRemove: selectedUsers.map(user => user.userId) 
            },
            () => {
                setUsers(users?.filter(u => !selectedUsers.includes(u)))
                setSelectedUsers([])
                if(readyToClose.current){
                    AlertMan.push({type:"success", content: "Success"})
                    props.onClose();
                }
                    

                readyToClose.current = true
                     
            },
            error => {
                    setError(error.title)
                    readyToClose.current = false
                }
            )
        }
        else{
            readyToClose.current = true
        }



        if(selectedInvites.length > 0){
            APICall.RemoveChannelInvites({ 
                toRemove: selectedInvites.map(invite => invite.inviteId) 
            },
            () => {
                setInvites(invites?.filter(i => !selectedInvites.includes(i)))
                setSelectedInvites([])
                if(readyToClose.current){
                    AlertMan.push({type:"success", content: "Success"})
                    props.onClose();
                }
                    

                readyToClose.current = true
            },
            error => {
                    setError(error.title)
                    readyToClose.current = false
                }
            )

        } else {
            readyToClose.current = true
        }
        


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
                <DialogTitle>Users/Invites</DialogTitle>
                <DialogContent className="">

                    
                {
                            error &&
                            <Alert className="-ml-1 mb-1" severity="error">{error}</Alert>
                        }
                    <div className="w-min-[500px] flex flex-row">

                        <div className="px-4">


                            <div className="ms-2 text-xl w-full text-center">
                                Users
                            </div>
                            <div className="user-list -ms-1 px-2 max-h-[500px] overflow-x-hidden overflow-y-auto mt-4">
                                {
                                    users == undefined &&
                                    <div className="italic w-full text-center">
                                        No users
                                    </div>
                                }

                                {
                                    users &&
                                    users.map(user =>
                                        <div key={user.userId} className={`user m-1 w-full flex flex-row justify-between rounded-2xl outline outline-1 0 ${selectedUsers.includes(user) ? "bg-gray-300" : ""}`}>
                                            <div className="font-semibold italic p-2 overflow-hidden">
                                                {user.username}
                                            </div>

                                            {
                                                !selectedUsers.includes(user) &&
                                                <IconButton onClick={() => { setSelectedUsers([...selectedUsers, ...[user]]) }} color="error">
                                                    <Close />
                                                </IconButton>
                                            }

                                            {
                                                selectedUsers.includes(user) &&
                                                <IconButton onClick={() => { setSelectedUsers(selectedUsers.filter(u => u != user)) }} color="info">
                                                    <ReplayIcon />
                                                </IconButton>
                                            }

                                        </div>
                                    )
                                }
                            </div>
                        </div>




                        <div className="px-4">


                            <div className="ms-2 text-xl w-full text-center">
                                Invites
                            </div>
                            <div className="invite-list -ms-1 px-2 max-h-[500px] overflow-x-hidden overflow-y-auto mt-4">
                                {
                                    invites == undefined &&
                                    <div className="italic w-full text-center">
                                        Loading invites
                                    </div>
                                }
                                {
                                    invites && invites.length == 0 &&
                                    <div className="italic w-full text-center">
                                        No invites
                                    </div>
                                }
                                {
                                    invites && invites.length > 0 &&
                                    invites.map(invite =>
                                        <div key={invite.inviteId} className={`invite m-1 w-full flex flex-row justify-between rounded-2xl outline outline-1 0 ${selectedInvites.includes(invite) ? "bg-gray-300" : ""}`}>
                                            <div className="font-semibold italic p-2 overflow-hidden">
                                                {invite.recipientName}
                                            </div>

                                            {
                                                !selectedInvites.includes(invite) &&
                                                <IconButton onClick={() => { setSelectedInvites([...selectedInvites, ...[invite]]) }} color="error">
                                                    <Close />
                                                </IconButton>
                                            }

                                            {
                                                selectedInvites.includes(invite) &&
                                                <IconButton onClick={() => { setSelectedInvites(selectedInvites.filter(i => i != invite)) }} color="info">
                                                    <ReplayIcon />
                                                </IconButton>
                                            }

                                        </div>
                                    )
                                }
                            </div>
                        </div>

                    </div>

                </DialogContent>
                <DialogActions>
                    <Button onClick={props.onClose}>Cancel</Button>
                    <Button onClick={() => {onRemove()}} color="error">Remove</Button>
                </DialogActions>
            </Dialog>
        </>
    )
}