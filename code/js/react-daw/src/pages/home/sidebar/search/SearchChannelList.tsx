import * as React from 'react';

import { useEffect, useRef, useState } from 'react';

import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import Avatar from '@mui/material/Avatar';
import Typography from '@mui/material/Typography';
import { Channel } from '../channels/Channel';
import { APICall } from '../../../../APICall';
import { ChannelMan } from '../../../../AppMan';
import { SearchedChannelsModel, UserChannelModel } from '../../../../APIModels';
import { channel } from 'diagnostics_channel';
import { Button, Fab } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { CreateChannel } from '../channels/CreateChannel';
import { SearchChannel } from './SearchChannel';




export function SearchChannelList() {
    const [searchedChannels, setSearchedChannels] = useState<Array<SearchedChannelsModel>>()
    const inputValue = useRef("")
    const lastSearch = useRef("")

    useEffect(() => {

        const interval = setInterval(() => {
            if(inputValue.current.length > 3 && lastSearch.current != inputValue.current){
                APICall.SearchChannels({
                    keyword: inputValue.current
                },
                    channels => {
                        setSearchedChannels(channels)
                    }, 
                    error => console.log(error)
                )

                lastSearch.current = inputValue.current

            }
 
            if(inputValue.current.length < 4){
                lastSearch.current = ""
                setSearchedChannels(undefined)
            }
                


        }, 1000)
        

        return () => clearInterval(interval)
        }, [])

    return (
        <>
            <div className="h-svh n">
                <div className="h-15 bg-white p-2 flex flex-row items-center">

                    <div className="ms-2">
                        <input onChange={ev => {inputValue.current = ev.target.value}} className="rounded-md bg-slate-200 h-7" placeholder=" search" />
                    </div>

                    <div className="ms-6">
                        <Button variant="outlined" size='small'>
                            Search
                        </Button>
                    </div>

                </div>
                <List sx={{ height:"80vh", overflow:"auto", width: '100%', maxWidth: 360, bgcolor: 'background.paper' }}>
                    { searchedChannels && searchedChannels.length > 0 &&
                        searchedChannels?.map(channel =>
                            <SearchChannel key={channel.channelId} channel={channel} />
                        )
                    }

                    {
                        searchedChannels && searchedChannels.length == 0 &&
                            <div className="w-full text-center italic">
                                No results
                            </div>
                    }

                    {
                        searchedChannels == undefined &&
                            <div className="w-full text-center italic">
                                Type to search
                            </div>
                    }

                </List>
            </div>
        </>

    )
}
