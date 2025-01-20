import * as React from 'react';

import { useEffect, useRef, useState } from 'react';
import Stack from '@mui/material/Stack';
import Divider from '@mui/material/Divider';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid2';
import { Navigate, Outlet, useNavigate } from 'react-router-dom';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import Avatar from '@mui/material/Avatar';
import Typography from '@mui/material/Typography';
import { Channel } from '../channels/Channel';
import { APICall } from '../../../../APICall';
import { AlertMan, ChannelMan } from '../../../../AppMan';
import { ChannelInviteModel, UserChannelModel } from '../../../../APIModels';
import { channel } from 'diagnostics_channel';
import { Invite } from './Invite';
import { Button, CircularProgress } from '@mui/material';



export function InviteList() {    
    const [channelInvites, setChannelInvites] = useState<Array<ChannelInviteModel>>()

    useEffect(() => {
        if(!channelInvites){
            APICall.GetChannelInvites(
                invites => {
                    //console.log(data)
                    setChannelInvites(invites)
                },
                (error) => console.log(error)
            )
        }
     
    }, [channelInvites])

    function ClearAll(){
        const inviteIds: Array<number> = []
        let idsCounter = 0
        channelInvites?.forEach((channelInvite) => {
            inviteIds[idsCounter++] = channelInvite.inviteId
        })

        APICall.CancelInvite(
            {   toRemove: inviteIds },
            () => {
                setChannelInvites(undefined)
            },
            (error) => {
                AlertMan.push({type:"error", content: error.title})
            }
        )

    }

    return (
        <div className="h-svh n">
           <div className="h-15 bg-white p-2 flex flex-row items-center">
        
            <div className='ms-2'>
                <input className="rounded-md bg-slate-200 h-7" placeholder=" search" />
            </div>

            <div className='ms-6'>
                <Button onClick={ClearAll} variant='outlined' size='small'>
                    Clear all
                </Button>
            </div>

            </div>
            <List sx={{ height:"80vh", overflow:"auto"}} className="overflow-y-scroll overflow-x-hidden h-[80%] w-full max-w-[360px] bg-white" >
                {
                    channelInvites == undefined &&  
                    <Box sx={{ display: 'flex' }}>
                        <CircularProgress />
                    </Box>
                }
                {
                    channelInvites && channelInvites.length == 0 &&
                        <div className="w-full text-center italic">
                            No invites
                        </div>
                }
                {
                    channelInvites && channelInvites.length > 0 && 
                        channelInvites?.sort((i1, i2) => i2.inviteId - i1.inviteId).map(invite =>
                            <Invite key={invite.inviteId} invite={invite}/>
                        )
                }
            </List>
        </div>

    )
}
