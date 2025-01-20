import * as React from 'react';
import { createBrowserRouter, RouterProvider } from "react-router-dom";

import './app.css';
import { Home } from './pages/home/Home';
import { Login } from './pages/login/Login';
import { Root } from './pages/home/Root';
import { ChannelView } from './pages/home/channel-view/ChannelView';



export function log(tag: string, message: any){
	console.log(`(${tag}) ${String(message)}`)
}


const router = createBrowserRouter(
	[
		{
			path: '/',
			element: <Root />,
		},
		{
			path: '/home',
			element: <Home />,
			children: [{
				path: '/home/channel/:cid',
				element: <ChannelView/>
			}]
		}, 
		{
			path: '/login',
			element: <Login />
		}
	]
)






export function App() {

	return(
		<RouterProvider router={router} />
	)
}
