import React, { ReactElement, useEffect, useState } from "react"
import { ChannelInviteModel, UserChannelModel } from "../../../../APIModels"
import { channel } from "diagnostics_channel"
import { Button, Divider, IconButton, Popover, Typography } from "@mui/material"
import { useNavigate } from 'react-router-dom';
import InfoIcon from '@mui/icons-material/Info';
import { AlertMan, ChannelMan } from '../../../../AppMan';
import { APICall } from '../../../../APICall';

import { Check, Close } from "@mui/icons-material"


function getRandomInt(max) {
    return Math.floor(Math.random() * max);
}

type Props = {
    invite?: ChannelInviteModel,
    children?: any
}


export function Invite(props: Props) {

    const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);
    const [disabled, setDisabled] = useState(false)
    const [hidden, setHidden] = useState(false)

    const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
        setAnchorEl(event.currentTarget);
    };

    const handleClose = () => {
        setAnchorEl(null);
    };

    function onConfirm() {
        setDisabled(true)

        APICall.JoinChannel({
            channelId: props.invite?.channelId!!,
            inviteId: props.invite?.inviteId!!
        },
            channel => {
                ChannelMan.addChannel(channel)
                //AlertMan.push({type:"success", content: "Channel joined succesfully"})
                setHidden(true)
                
            },
            error => {
                setDisabled(false)
                AlertMan.push({type:"error", content: error.title})
                console.error(error)
            }
        )

       
    }

    function onCancel() {
        setDisabled(true)
        APICall.CancelInvite(
            { toRemove: [props.invite?.inviteId!!] },
            () => setHidden(true),
            error => {
                setDisabled(false)
                console.log(error)
            }
        )
    }




    const open = Boolean(anchorEl);
    let counter = 0
    const id = open ? 'simple-popover' : undefined;


    return (
        <>
            {!hidden &&
                <>
                    <div className="hover:bg-gray-200 cursor-pointer w-full h-[80px] overflow-hidden text-center flex items-center">
                        <div className="invite flex flex-row w-full px-2 justify-between">


                            <div className="title font-medium mt-1 mx-2">
                                {props.invite?.channelName}
                            </div>

                            <div className="">
                                {disabled && <>
                                    <IconButton disabled>
                                        <Check />
                                    </IconButton>
                                    <IconButton disabled>
                                        <Close />
                                    </IconButton>
                                    <IconButton disabled>
                                        <InfoIcon />
                                    </IconButton>
                                </>
                                }
                                {!disabled && <>
                                    <IconButton onClick={onConfirm} color="success">
                                        <Check />
                                    </IconButton>
                                    <IconButton onClick={onCancel} color="error">
                                        <Close />
                                    </IconButton>
                                    <IconButton onClick={handleClick} aria-label="delete">
                                        <InfoIcon />
                                    </IconButton>
                                </>
                                }

                                <Popover
                                    id={id}
                                    open={open}
                                    anchorEl={anchorEl}
                                    onClose={handleClose}
                                    anchorOrigin={{
                                        vertical: 'bottom',
                                        horizontal: 'left',
                                    }}
                                >
                                    {props.invite?.invitePerms.map(invitePerm =>
                                        <Typography key={counter++} sx={{ p: 2 }}>{invitePerm}</Typography>
                                    )}

                                </Popover>
                            </div>
                        </div>
                    </div>
                    <Divider variant="fullWidth" component="li" />

                </>
            }
        </>
    )


}

