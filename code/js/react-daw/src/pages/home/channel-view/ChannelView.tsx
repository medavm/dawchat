import React, { useEffect, useReducer, useState } from "react"
import { ChannelUserModel, UserChannelModel } from "../../../APIModels"
import { IconButton, Menu, MenuItem } from "@mui/material"
import MoreIcon from '@mui/icons-material/MoreVert';
import { MessageView } from "./messages/MessageView";
import { MessageInput } from "./messages/MessageInput";
import { Navigate, useParams } from "react-router-dom";
import { ChannelMan, SessionMan } from "../../../AppMan";
import { SettingsView } from "./options/SettingsView";
import { CreateInvite } from "./options/CreateInvite";
import { LeaveChannelView } from "./options/LeaveChannelView";
import { channel } from "diagnostics_channel";
import { APICall } from "../../../APICall";
import { RenameView } from "./options/RenameView";
import { UsersView } from "./options/UsersView";

type Option = "users" | "invite" | "rename" | "leave"

//const options: OptionType[] = ["users", "invite", "rename", "leave"]


export function ChannelView() {

    const [, forceUpdate] = useReducer(x => x + 1, 0)
    const {cid} = useParams()
    const [selectedChannel, setSelectedChannel] = useState<UserChannelModel>()
    const [currentOption, setCurrentOption] = useState<Option>()
    const [anchorEl, setAnchorEl] = useState<HTMLElement>()
    const [redirectHome, setRedirectHome] = useState(false)
    const user = SessionMan.currentUser!

    const handleOptions = (option: Option) => {
        setAnchorEl(undefined)
        setCurrentOption(option)
    }
    
    useEffect(() => {

        if(cid){ 
            const channelId = parseInt(cid)
            const channel = ChannelMan.getChannel(channelId)
            setSelectedChannel(channel)

            const listenter = ChannelMan.addChannelListener(channelId, ch => {
                if(!ch)
                    setRedirectHome(true)

                setSelectedChannel(ch)
                forceUpdate()
            })

            return () => ChannelMan.removeListener(listenter)
        } else { 
            setSelectedChannel(undefined)
        }
            
    }, [cid])

    if(redirectHome){
        return (
            <Navigate to="/home" />
        )
    }

    if(!selectedChannel){
        return (
            <div className="w-full text-center italic text-xl text-gray-300 mt-12">
                Select a channel
            </div>
        )
    }
    
    return (
        <div className="bg-sky-300 bg-opacity-40 w-full">

            <div className="title-bar bg-slate-300 flex flex-row h-12 w-full justify-between">

                <div>


                </div>

                <h3 className="text-xl font-mono mt-2 ms-2">{selectedChannel.channelName}</h3>


                <div className="self-end">
                    <IconButton
                        aria-label="more"
                        id="long-button"
                        aria-controls={anchorEl ? 'long-menu' : undefined}
                        aria-expanded={anchorEl ? 'true' : undefined}
                        aria-haspopup="true"
                        onClick={e => setAnchorEl(e.currentTarget)}
                    >
                        <MoreIcon />
                    </IconButton>
                    <Menu
                        id="basic-menu"
                        anchorEl={anchorEl}
                        open={anchorEl ? true : false}
                        onClose={() => setAnchorEl(undefined)}
                        MenuListProps={{
                            'aria-labelledby': 'basic-button',
                        }}
                    >
                        
                        {
                            selectedChannel.channelPerms.includes("remove-users") && 
                                <MenuItem onClick={v => handleOptions("users")} value={0}>Users</MenuItem>
                        }

                        {
                            selectedChannel.channelPerms.includes("invite") && 
                                <MenuItem onClick={v => handleOptions("invite")} value={1}>Invite</MenuItem>
                        }

                        {
                            selectedChannel.channelPerms.includes("rename") && 
                                <MenuItem onClick={v => handleOptions("rename")} value={2}>Rename</MenuItem>
                        }

                        <MenuItem onClick={v => handleOptions("leave")} value={3}>Leave</MenuItem>
                    </Menu>
                </div>
            </div>

         
            <MessageView user={user} channel={selectedChannel}/>
            
            {
                currentOption == "users" && 
                    <UsersView open={true} onClose={() => setCurrentOption(undefined)} channel={selectedChannel} />
            }


            {
                currentOption == "rename" && 
                    <RenameView open={true} onClose={() => setCurrentOption(undefined)}  channel={selectedChannel} />
            }

            {
                currentOption == "invite" && 
                    <CreateInvite  open={true} onClose={() => setCurrentOption(undefined)}  channel={selectedChannel}  />
            }

            {
                currentOption == "leave" && 
                    <LeaveChannelView  open={true} onClose={() => setCurrentOption(undefined)}  channel={selectedChannel}/>
            }

        </div>
    )



}

