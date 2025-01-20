import * as React from 'react';

import { useEffect, useReducer, useRef, useState } from 'react';

import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import Avatar from '@mui/material/Avatar';
import Typography from '@mui/material/Typography';
import { Channel } from './Channel';
import { APICall } from '../../../../APICall';
import { AlertMan, ChannelMan, SessionMan } from '../../../../AppMan';
import { MessageModel, ResultMessages, RequestMessages, UserChannelModel } from '../../../../APIModels';
import { channel } from 'diagnostics_channel';
import { Button, Fab } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { CreateChannel } from './CreateChannel';
import { error } from 'console';
import { useParams } from 'react-router-dom';


export function ChannelList2() {

    //const [, forceUpdate] = useReducer(x => x + 1, 0)
    const [userChannels, setUserChannels] = useState<UserChannelModel[]>()
    const [dialogueOpen, setDialogueOpen] = useState<boolean>(false)
    const {cid} = useParams()
    const user = SessionMan.currentUser
    useEffect(() => {
        if (!userChannels) {
            const channels = ChannelMan.getChannelList()
            if(!channels){
                ChannelMan.fetchChannelList(channels => {
                    //console.log(channels)
                    if(channels)
                        setUserChannels([...channels])
                    else
                        setUserChannels(undefined)
                },
                error => {
                    AlertMan.push({type:"error", content: error.title})
                    console.log(error)
                })
            }
            setUserChannels(channels)
        }

        const listener = ChannelMan.addChannelListListener(channels => {
            if(channels){
                //console.log(channels)
                setUserChannels([...channels])
            }
            else
                setUserChannels(undefined)
        })

        return () => ChannelMan.removeListener(listener)
    }, [userChannels])

    return (
        <>
            <div className="h-svh n">
                <div className="h-15 bg-white p-2 flex flex-row items-center">

                    <div className="ms-2">
                        <input className="rounded-md bg-slate-200 h-7" placeholder=" search" />
                    </div>


                    <div className="ms-6">
                        <Button variant="outlined" size='small' onClick={() => setDialogueOpen(true)}>
                            Create
                        </Button>
                    </div>

                </div>
                <List sx={{ height: "81vh", overflow: "auto", width: '100%', maxWidth: 360, bgcolor: 'background.paper' }}>
                    {
                        userChannels && userChannels.length > 0 &&
                            userChannels.sort((c1, c2) => c2.lastMessage - c1.lastMessage).map(channel =>
                                <Channel selected={cid && parseInt(cid) == channel.channelId ? true : false} key={channel.channelId} user={user} channel={channel}  />   
                            )
                    }

                    {
                        userChannels && userChannels.length == 0 &&
                            <div className="w-full text-center italic">
                                No channels
                            </div>
                    }

                    {
                        userChannels == undefined && 
                            <div className="w-full text-center italic">
                                Loading
                            </div>
                    }

                    

                </List>
            </div>

            <CreateChannel open={dialogueOpen} onClose={() => setDialogueOpen(false)} />

        </>

    )
}
