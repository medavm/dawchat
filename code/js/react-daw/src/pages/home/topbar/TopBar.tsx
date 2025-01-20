import * as React from 'react';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import Menu from '@mui/material/Menu';
import MenuIcon from '@mui/icons-material/Menu';
import WhatsAppIcon from '@mui/icons-material/WhatsApp';
import Container from '@mui/material/Container';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import Tooltip from '@mui/material/Tooltip';
import MenuItem from '@mui/material/MenuItem';
import AdbIcon from '@mui/icons-material/Adb';
import { APICall } from '../../../APICall';
import { redirect, useNavigate } from 'react-router-dom';
import { SessionMan } from '../../../AppMan';
import { useState } from 'react';
import { AppInviteView } from './AppInviteView';

const options = ["Account invite", "Logout"]

export function TopBar() {
    const [anchorElUser, setAnchorElUser] = useState<null | HTMLElement>(null)
    const navigate = useNavigate()

    const [inviteModal, setInviteModal] = useState(false)

    const handleOpenUserMenu = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorElUser(event.currentTarget)
    }

    const handleCloseUserMenu = () => {
        setAnchorElUser(null)
    }

    const handleButtonUserMenu = (buttonText) => {

        handleCloseUserMenu()

        if(buttonText == options[0]){  
            setInviteModal(true)
        }

        if(buttonText == options[1]){

            APICall.Logout(()=>{
                console.log("Logged out")
                SessionMan.updateUser(undefined)
                navigate("/login", {replace: true})
            }, (error) => {
                console.error(error)
            })
        
        }



    }

    return (
        <>
        <AppBar position="static">
            <Container maxWidth="xl">
                <Toolbar disableGutters>
                    <WhatsAppIcon sx={{ display: { xs: 'none', md: 'flex' }, mr: 1 }} />
                    
                    <Typography
                        variant="h6"
                        noWrap
                        component="a"
                        href="/home"
                        sx={{
                            mr: 2,
                            display: { xs: 'none', md: 'flex' },
                            fontFamily: 'monospace',
                            fontWeight: 700,
                            letterSpacing: '.3rem',
                            color: 'inherit',
                            textDecoration: 'none',
                        }}
                    >
                        DAWCHAT
                    </Typography>
                    <WhatsAppIcon sx={{ display: { xs: 'flex', md: 'none' }, mr: 1 }} />
                    <Typography
                        variant="h5"
                        noWrap
                        component="a"
                        href="/home"
                        sx={{
                            mr: 2,
                            display: { xs: 'flex', md: 'none' },
                            flexGrow: 1,
                            fontFamily: 'monospace',
                            fontWeight: 700,
                            color: 'inherit',
                            textDecoration: 'none',
                        }}
                    >
                        DAWCHAT
                    </Typography>
                    <Box sx={{ flexGrow: 1, display: { xs: 'none', md: 'flex' } }}>
         
                    </Box>
                    <Box sx={{ flexGrow: 0, flexDirection:"flex-row"}}>
                    <Typography
                        variant="h6"
                        noWrap
                        component="a"
                        sx={{
                            mr: 2,
                            fontFamily: 'monospace',
                            fontWeight: 700,
                            color: 'inherit',
                            textDecoration: 'none',
                        }}
                    >
                        {SessionMan.currentUser?.username}
                    </Typography>
                        <Tooltip title="Open settings">
                            <IconButton onClick={handleOpenUserMenu} sx={{ p: 0 }}>
                                <Avatar alt="ARemy Sharp"  />
                            </IconButton>
                        </Tooltip>
                        <Menu
                            sx={{ mt: '45px' }}
                            id="menu-appbar"
                            anchorEl={anchorElUser}
                            anchorOrigin={{
                                vertical: 'top',
                                horizontal: 'right',
                            }}
                            keepMounted
                            transformOrigin={{
                                vertical: 'top',
                                horizontal: 'right',
                            }}
                            open={Boolean(anchorElUser)}
                            onClose={handleCloseUserMenu}
                        >
                            {options.map((setting) => (
                                <MenuItem key={setting} onClick={() => handleButtonUserMenu(setting)}>
                                    <Typography sx={{ textAlign: 'center' }}>{setting}</Typography>
                                </MenuItem>
                            ))}
                        </Menu>
                    </Box>
                </Toolbar>
            </Container>
        </AppBar>
        
        {
            inviteModal &&
                <AppInviteView onClose={() => setInviteModal(false)} />
        }

        </>
    )

}

