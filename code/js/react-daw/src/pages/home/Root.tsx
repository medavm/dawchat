import * as React from 'react';
import { Navigate } from "react-router-dom";

export function Root() {

    return (
        <Navigate to="/login" replace={true} />
    )
}