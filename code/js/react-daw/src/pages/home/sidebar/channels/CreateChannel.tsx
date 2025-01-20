import React, { ReactElement, useEffect, useState } from "react"
import { ChannelType, UserChannelModel } from "../../../../APIModels"
import { channel } from "diagnostics_channel"
import {Dialog, DialogTitle, DialogContent, DialogContentText, TextField, DialogActions, Checkbox, SelectChangeEvent, Select, MenuItem, Alert } from "@mui/material"
import { Button } from "@mui/material"
import { AlertMan, ChannelMan } from "../../../../AppMan"
import { APICall } from '../../../../APICall';

const ChannelTypes: Array<{type: string, value: ChannelType}>  = [
    {type: "Public",    value: "public"}, 
    {type: "Private",   value: "private"}
]

type Props = {
    open: boolean
    onClose: () => void
}

export function CreateChannel(props: Props) {
    const [channelName, setChannelName] = useState("")
    const [type, setType] = useState(ChannelTypes[0])
    const [error, setError] = useState<string | undefined>()

    const handleChannelNameChange = (event: any) => {
        setChannelName(event.target.value);
    };
    const handleTypeChange = (event: SelectChangeEvent) => {
        const type = ChannelTypes.find(e => e.value == event.target.value)
        setType(type ? type : ChannelTypes[0])
           
    };

    function onCreate(){

        APICall.CreateChannel({
            channelName: channelName,
            channelType: type.value
        },
            channel => {
                //console.log(channel)
                ChannelMan.addChannel(channel)
                setError(undefined)
                props.onClose()
                AlertMan.push({type: "success", content:"Channel created successfully"})
            },
            error => {
                setError(error.title)
            }
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
                <DialogTitle>Create Channel</DialogTitle>
                <DialogContent>
                   {error && <Alert className="-ml-1 mb-1" severity="error">{error}</Alert>}
                    <div className="flex flex-row">
                        <TextField value={channelName} onChange={e => handleChannelNameChange(e)} type="text" fullWidth label="Channel name" variant="standard" />
                        <Select
                            id="type-select"
                            value={type.value}
                            defaultValue={ChannelTypes[0].value}
                            label="Type"
                            onChange={handleTypeChange}
                            sx={{ marginLeft: 10, minWidth:"100px"}}
                        >
                            { ChannelTypes.map(it => 
                                <MenuItem key={counter++} value={it.value}>{it.type}</MenuItem>)
                            }
                            
                        </Select>
                    </div>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => {props.onClose(); setError(undefined)}} color="inherit">Cancel</Button>
                    <Button onClick={onCreate} color="info">Create</Button>
                </DialogActions>
            </Dialog>
        </>
    )


}