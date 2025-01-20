import * as React from 'react';

import { useState } from 'react';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Box from '@mui/material/Box';
import { Outlet, Link } from 'react-router-dom';
import {ChannelList2} from './channels/ChannelList';
import { InviteList } from './invites/InviteList';
import { SearchChannelList } from './search/SearchChannelList';


function a11yProps(index: number) {
    return {
        id: `simple-tab-${index}`,
        'aria-controls': `simple-tabpanel-${index}`,
    };
}

export function SideBar() {
    const [value, setValue] = useState(0);

    const handleChange = (event: React.SyntheticEvent, newValue: number) => {
        setValue(newValue);
    };

    return (
        <div className="h-full min-w-[350px]">
            <Box sx={{borderBottom: 1, borderColor: 'divider' }}>
                <Tabs value={value} onChange={handleChange} aria-label="basic tabs example" className='bg-gray-200'>
                    <Tab label="Channels"   {...a11yProps(1)} />
                    <Tab label="Invites"    {...a11yProps(2)} />
                    <Tab label="Search"     {...a11yProps(3)} />
                </Tabs>
            </Box>
            <CustomTabPanel value={value} index={0}>
                <ChannelList2/>
            </CustomTabPanel>
            <CustomTabPanel value={value} index={1}>
                <InviteList/>
            </CustomTabPanel>
            <CustomTabPanel value={value} index={2}>
                <SearchChannelList/>
            </CustomTabPanel>
        </div>
    )
}

interface TabPanelProps {
    children?: React.ReactNode;
    index: number;
    value: number;
}

function CustomTabPanel(props: TabPanelProps) {
    const { children, value, index, ...other } = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`simple-tabpanel-${index}`}
            aria-labelledby={`simple-tab-${index}`}
            {...other}
        >
            {value === index && <Box sx={{ p: 0 }}>{children}</Box>}
        </div>
    );
}

