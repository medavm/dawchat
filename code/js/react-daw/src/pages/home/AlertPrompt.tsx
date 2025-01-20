import { useEffect, useState } from "react"
import { AlertMan } from "../../AppMan"
import { Alert, Snackbar } from "@mui/material"
import React from "react"




export function AlertPrompt(){

    const [alert, setAlert] = useState<any>()

    useEffect(() => {
        const ref = AlertMan.subscribe(alert => {
            setAlert(alert)
        })

        return  () => AlertMan.clearSubscription(ref)
    })
    


    return (
        <Snackbar open={alert ? true : false} autoHideDuration={3000} onClose={() => setAlert(undefined)} >
            <div className="ms-10 mb-10">
                <Alert
                    severity={alert?.type}
                    variant="filled"
                    sx={{ width: '100%' }}
                >
                    {alert?.content}
                </Alert>
            </div>
            
        </Snackbar>
    )


}