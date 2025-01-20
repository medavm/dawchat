



/*
interface Problem {
    readonly title: string, 
    readonly type: string,
    readonly detail: string
}

export const APIError = {
    InvalidCredentials: class implements Problem {
        title: ""
        type: ""
        detail: ""
    },

    InvalidUsername: class implements Problem {
        title: string
        type: string
        detail: string
    },
}
*/

export const APIError = {
    InvalidCredentials: {
        type: "",
    },
    InvalidUsername: "",
}
