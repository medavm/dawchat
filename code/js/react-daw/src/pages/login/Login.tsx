
import { Alert, TextField, Button, Typography } from "@mui/material";
import WhatsAppIcon from '@mui/icons-material/WhatsApp';
import React, { useEffect, useRef, useState } from "react";
import {  useNavigate, useSearchParams } from "react-router-dom";
import { APICall } from "../../APICall";
import { SessionMan } from "../../AppMan";
import { Session } from "inspector/promises";

function Inputs(){
    return {
        username: "",
        password1: "",
        password2: "",
        token: "",
    }
}

type Mode = "singup" | "login"

export function Login() {
	const [mode, setMode] = useState<Mode>("login")
	const [busy, setBusy] = useState(false)
	const [error, setError] = useState("")
	const inputs = useRef(Inputs())
	const navigate = useNavigate()
	const [searchParams] = useSearchParams();
  	const token = searchParams.get("t"); // "value"

	useEffect(() =>{

		APICall.GetUserInfo((data) => { 
			if(data.userId && data.username){
				console.log(`Already logged in has ${data.username}, redirecting to /home`)
				SessionMan.updateUser({userId: data.userId, username: data.username})
				navigate("/home", {replace: true})
			}
		}, (error) => {
			//console.log("GetUserInfo() failed: "+ error.detail)
		})
		
	}, [])

	async function onSubmit() {

		if (inputs.current.username.length < 3)
			return setError("Invalid username")

		if (inputs.current.password1.length < 8)
			return setError("Password must have 8 chars")

		if (token || mode == "singup") {

			if (inputs.current.password1 != inputs.current.password2)
				return setError("Passwords are not the same")

			if (!token && inputs.current.token.length < 12)
				return setError("Invalid registration code")
			
			let t = token
			if(!token)
				t = inputs.current.token
	
			setBusy(true)
			APICall.CreateUser({
				username: inputs.current.username, 
				password: inputs.current.password1,
				token: t!
			}, user => {
				console.log("Created user account, loggin in...")
				APICall.Login({
					username: inputs.current.username, 
					password: inputs.current.password1
				}, user_ => {
					SessionMan.updateUser({
						userId: user_.userId, 
						username: user_.username
					})

					setTimeout(() => {
						navigate("/home")
						setBusy(false)
					}, 500)
	
				}, (error) => {
					setError(error.title)
					setBusy(false)
				})

			}, (error) => {
				setError(error.title)
				setBusy(false)
			})
			
		}
		else {

			setBusy(true)
			APICall.Login({username: inputs.current.username, password: inputs.current.password1}, user => {
				SessionMan.updateUser({userId: user.userId, username: user.username})
				setTimeout(() => {
					navigate("/home")
					setBusy(false)
				}, 500)

				
			}, (error) => {
				setError(error.title)
				setBusy(false)
			})

		}


	}

	const loginText = (token || mode == "singup")  ? "Register" : "Login"
	const registerText = (token || mode == "singup")  ? "Login?" : "Register?"

	return (
		<div className="bg-slate-400">
			<div className="flex flex-row min-h-screen justify-center items-center">

				<div onKeyDown={e => e.key === 'Enter' ? onSubmit(): ''} className="w-[400px] h-[400px] bg-gray-100 p-10 rounded-l-lg">
					<h1 className="font-mono text-2xl mb-2">SignIn</h1>
					{
						error.length > 0 &&
						<Alert className="-ml-1 mb-1" severity="error">{error}</Alert>
					}

					<TextField type="email" disabled={busy} onChange={ev => inputs.current.username = ev.target.value} fullWidth label="Username" variant="standard" />
					<TextField type="password" disabled={busy} onChange={ev => inputs.current.password1 = ev.target.value} id="standard-basic" fullWidth label="Password" variant="standard" />

					{
						(token || mode == "singup") &&
							<>
								<TextField type="password" disabled={busy} onChange={ev => inputs.current.password2 = ev.target.value} fullWidth label="Repeat Password" variant="standard" />
								
								{
									token &&
										<TextField type="url" disabled={true} fullWidth value={token} label="Registration code" variant="standard" />
								}

								{
									!token &&
										<TextField type="url" disabled={busy} onChange={ev => inputs.current.token = ev.target.value} fullWidth label="Registration code" variant="standard" />
								}
								
							</>
					}

					<div className="flex flow-row justify-between mt-6">
						<Button disabled={busy} sx={{ width: 130 }} onClick={onSubmit} variant="contained">{loginText}</Button>
						
						{ !token &&
							<Button disabled={busy} variant="text" onClick={() => {
								setMode(mode == "login" ? "singup" : "login")
								setError("")
								}} color="primary" >
									{registerText}
							</Button>
						}
						
					</div>

				</div>

				<div className="w-[400px] h-[400px] bg-blue-500 p-10 rounded-r-lg">
					<div className="flex h-full justify-center items-center ">
					<WhatsAppIcon sx={{ color:"white", fontSize: 80 , mr: 1 }} />
                    
                    <Typography
                        variant="h3"
                        noWrap
                        component="a"
                        sx={{
                            mr: 2,
                            display: { xs: 'none', md: 'flex' },
                            fontFamily: 'monospace',
                            fontWeight: 700,
                            color: 'white',
                            textDecoration: 'none',
                        }}
                    >
                        DAWCHAT
                    </Typography>
					</div>

				</div>


			</div>
		</div>
	)
}