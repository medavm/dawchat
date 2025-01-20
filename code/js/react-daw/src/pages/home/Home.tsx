import * as React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import {  } from '../../APIModels';
import { useEffect, useReducer, useRef, useState } from 'react';
import { TopBar } from './topbar/TopBar';
import { SideBar } from './sidebar/SideBar';
import { ChannelView } from './channel-view/ChannelView';
import { Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, Snackbar } from '@mui/material';
import { AlertMan, ChannelMan, SessionMan, UserEventMan } from '../../AppMan';
import { AlertPrompt } from './AlertPrompt';




function LostAuthDial(props: {onClose: () => void}){

    return(

        <Dialog
            open={true}
        >
        <DialogTitle>Authentication error</DialogTitle>
        <DialogContent>   
            <div className="flex flex-row">
                You are not authenticated, please login again
            </div>
        </DialogContent>
        <DialogActions>
            <Button hidden></Button>
            <Button onClick={props.onClose} color="info" type='submit'>Login</Button>
        </DialogActions>
        </Dialog>


    )
}


export function Home() {
    const [, forceUpdate] = useReducer(x => x + 1, 0)
    const [redirect, setRedirect] = useState(false)

    useEffect(() => {
        UserEventMan.startListening()

        return () => {
            UserEventMan.stopListening()
        }
	}, [])

    useEffect(() => {
		const upRef = SessionMan.onSessionUpdate(user => {
            if(!user)
                UserEventMan.stopListening()
			forceUpdate()
		})

		return () => SessionMan.clearUpdate(upRef)
    }, [])

    const authenticated = SessionMan.currentUser
    return (
        <>
            { authenticated &&
                <div className="absolute w-full h-full min-w-[500px] min-h-[400px] overflow-hidden">
                    <TopBar />
                        <div className="flex h-[100%] bg-slate-400">
                            <SideBar />
                            <Outlet />
                        </div>
                        
                    <AlertPrompt />
                </div>
            }

            {
                !authenticated && 
                    <LostAuthDial onClose={() => {setRedirect(true)}}></LostAuthDial>
            }

            {
                redirect && 
                    <Navigate to="/login" state={{ source: location.pathname }} replace={true} />
            }
        </>
        
    )

}

