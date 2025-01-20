

## API Documentantion
<br>
<br>



#### API endpoints

<details>
<summary>User Registration <code>POST /api/user/create</code></summary>

### User Registration

Create a user account.

**Request** 

``POST /api/user/create``

Body Parameters - *JSON*
- ``username: string`` (Required) - Unique username
- ``password: string`` (Required) - User password
- ``inviteToken: string`` (Required) - Registration invite token

Example

```http
POST http://localhost:8080/api/user/create HTTP/1.1
User-Agent: vscode-restclient
Content-Type: application/json
accept-encoding: gzip, deflate
content-length: 130

{
  "username": "John11",
  "password": "password123",
  "inviteToken": "irQuMYz3OCQq8548CPDN2ONYA6TN-Cu3wDShEitDX5g="
}
```

**Success Response**


- **201 Created** - User successfully created
    - Body - *JSON*
        - ``userId: number`` - User unique identifier
        - ``username: string`` - Username
        - ``createdAt: number`` - timestamp 

Example
```http
HTTP/1.1 201 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 24 Nov 2024 16:16:34 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "userId": 3,
  "username": "John11",
  "createdAt": 1732464994
}
```


**Error Response**

- **400 Bad Request**
    - *InsecurePassword*
    - *InvalidUsername*
- **403 Forbidden**
  - *InvalidUserInvite*
- **409 Conflict**
  - *UsernameTaken*

--- 
<br>
</details>

<br>












<details>
<summary>User Login <code>POST /api/user/login</code></summary>

### User Login

User login

**Request**

``POST /api/user/login``

Body Parameters - *JSON*
- ``username: string`` (Required) 
- ``password: string`` (Required)


Example

```http
POST http://localhost:8080/api/user/login HTTP/1.1
User-Agent: vscode-restclient
Content-Type: application/json
accept-encoding: gzip, deflate
content-length: 62
cookie: t=mdFaZul94g-hBDbEzSCbQERugtgX_ZSOhD1XrpmJTj0=

{
  "username": "John11",
  "password": "password123"
}
```

**Success Response**
- **200 OK** - Authentication successful
    - Header (Set-Cookie)
        - ``t: string`` - session token

Example
```http
HTTP/1.1 200 
Set-Cookie: t=iyKaI1tAIqM033hDAGXuyJo4ZPPRpFrEiXOFr6jUwfI=; Max-Age=604800; Expires=Sun, 01 Dec 2024 16:18:20 GMT; Path=/; HttpOnly
Content-Length: 0
Date: Sun, 24 Nov 2024 16:18:20 GMT
Keep-Alive: timeout=60
Connection: keep-alive
```

**Error Response**

- **400 Bad Request**
- **401 Unauthorized**
    - *InvalidCredentials*
  
--- 
<br>
</details>

<br>














<details>
<summary>User Logout <code>POST /api/user/logout</code></summary>

### User Logout

Terminate user session.

**Request**

``POST /api/user/logout``

Example

```http
POST http://localhost:8080/api/user/logout HTTP/1.1
User-Agent: vscode-restclient
accept-encoding: gzip, deflate
cookie: t=iyKaI1tAIqM033hDAGXuyJo4ZPPRpFrEiXOFr6jUwfI=
```

**Success Response**
- **200 OK** - Session terminated
    - Header (Set-Cookie)
        - ``t: string`` - session token

**Error Response**

- **400 Bad Request**
- **401 Unauthorized**

  
--- 

</details>
<br>





<details>
<summary>User Info <code>GET /api/user/info</code></summary>

### User Login

Info about current authenticated user

**Request**

``GET /api/user/info``


Example

```http
GET http://localhost:8080/api/user/info HTTP/1.1
User-Agent: vscode-restclient
accept-encoding: gzip, deflate
cookie: t=fuVmqp93yREb9TWGP0w2Y4E8dbPHV8Stvhwg6cB14eQ=
```

**Success Response**
- **200 OK** - Authentication successful
    - Body - *JSON*
        - ``userId: number`` - User unique id
        - ``username: string`` - Username
        - ``createdAt: number`` - timestamp

Example
```http
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 24 Nov 2024 16:20:36 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "userId": 3,
  "username": "John11",
  "createdAt": 1732464994
}
```

**Error Response**
- **400 Bad Request**
- **401 Unauthorized**

  
--- 
<br>
</details>
<br>














<details>
<summary>List User Registation Invites <code>GET /api/user/invites</code></summary>

### User Registation Invites

Obtain list of user registration created invites

**Request**

``GET /api/user/invites``

Example

```http
GET http://localhost:8080/api/user/invites HTTP/1.1
User-Agent: vscode-restclient
accept-encoding: gzip, deflate
cookie: t=fuVmqp93yREb9TWGP0w2Y4E8dbPHV8Stvhwg6cB14eQ=
```

**Success Response**
- **200 OK**
    - Body - *JSON*
        - ``invites: list`` - List of invites
          - ``token: number`` - Invite token
          - ``createdBy: string`` - Emitter users id (my userId)
          - ``acceptedBy: string`` - Created user id (null invite not used)
          - ``acceptedName: list`` - Created username  (null invite not used)
          - ``timestamp: number`` - timestamp when invite created

Example
```http
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 24 Nov 2024 18:17:11 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "invites": [
    {
      "token": "ypAjx6cOE0SkHTY3KILI5uEmPS7bARtGcwk2N3d0SO4=",
      "createdBy": 45,
      "acceptedBy": null,
      "acceptedName": null,
      "timestamp": 1732472215
    },
    {
      "token": "p3BtWh5KZ7TGajTbF2k3RWV9WgDEFO4kk4SK7pvVY0w=",
      "createdBy": 45,
      "acceptedBy": 54,
      "acceptedName": "Mike67",
      "timestamp": 1732472216
    }
  ]
}
```


**Error Response**
- **400 Bad Request**
- **401 Unauthorized**

--- 
<br>
</details>
<br>














<details>
<summary>Create User Registration Invite <code>POST /api/user/invites/create</code></summary>

### Create User Registration Invite

Create user registration invite

**Request**
``POST /api/user/invites/create``

Example

```http
POST http://localhost:8080/api/user/invites/create HTTP/1.1
User-Agent: vscode-restclient
Content-Type: application/json
accept-encoding: gzip, deflate
cookie: t=fuVmqp93yREb9TWGP0w2Y4E8dbPHV8Stvhwg6cB14eQ=
```

**Success Response**
- **201 Created** - Created user registartion invite.
    - Body - *JSON*
        - ``token: number`` - Invite token
        - ``createdBy: string`` - Emitter users id (my userId)
        - ``acceptedBy: string`` - Created user id (null invite not used)
        - ``acceptedName: list`` - Created username  (null invite not used)
        - ``timestamp: number`` - timestamp when invite created

Example
```http
HTTP/1.1 201 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 24 Nov 2024 16:22:06 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "token": "a5HHXo8yNtS3gjYR5NERMOTF47Z-T6wn0Rqgwwkll1M=",
  "createdBy": 3,
  "acceptedBy": null,
  "acceptedName": null,
  "timestamp": 1732465326
}
```

**Error Response**
- **400 Bad Request**
- **401 Unauthorized**

--- 
<br>
</details>

<br>













<details>
<summary>Remove User Registration Invites <code>POST /api/user/invites/remove</code></summary>

### Remove User Registration Invites

Remove user registration invites.

**Request**

``POST /api/user/invites/remove``

Body Parameters - *JSON*
- ``toRemove: list string`` (Required) - List of invite tokens to remove

Example

```http
POST http://localhost:8080/api/user/invites/remove HTTP/1.1
User-Agent: vscode-restclient
Content-Type: application/json
accept-encoding: gzip, deflate
content-length: 142
cookie: t=fuVmqp93yREb9TWGP0w2Y4E8dbPHV8Stvhwg6cB14eQ=

{
  "toRemove": [
    "Qt_Ge536JbZOvOU7lhFaH8H5JGoA54oKmo5AmH9aGNc=",
    "a5HHXo8yNtS3gjYR5NERMOTF47Z-T6wn0Rqgwwkll1M="
  ]
}
```

**Success Response**
- **201 Created** - Succefully removed invites

Example
```http
HTTP/1.1 200 
Content-Length: 0
Date: Sun, 24 Nov 2024 16:24:08 GMT
Keep-Alive: timeout=60
Connection: keep-alive
```

**Error Response**

- **400 Bad Request**
- **403 Forbidden**
    - *NoPermission* - You cant remove an invite the you didnt create.
- **404 Not Found**
    - *UserInviteNotFound* - Invalid invite token

--- 
<br>
</details>

<br>





<details>
<summary>List User Channel Invites <code>GET /api/user/channel/invites</code></summary>

### User Channel Invites

Obtain list of pending channels invites

**Request**
``GET /api/user/channel/invites``

Example

```http
GET http://localhost:8080/api/user/channel/invites HTTP/1.1
User-Agent: vscode-restclient
accept-encoding: gzip, deflate
cookie: t=fuVmqp93yREb9TWGP0w2Y4E8dbPHV8Stvhwg6cB14eQ=
```

**Success Response**
- **200 OK**
    - Body - *JSON*
        - ``invites: list``
            - ``inviteId: number``
            - ``senderId: string``
            - ``senderName: string``
            - ``channelId: number``
            - ``channelName: string``
            - ``channelType: string``
            - ``invitePerms: list (string)``
            - ``timestamp: number``

Example
```http
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 24 Nov 2024 16:29:54 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "invites": [
    {
      "inviteId": 2,
      "senderId": 3,
      "senderName": "John11",
      "channelId": 4,
      "channelName": "News13",
      "channelType": "private",
      "invitePerms": [
        "read",
        "write"
      ],
      "timestamp": 1732465708
    }
  ]
}
```


**Error Response**
- **400 Bad Request**
- **401 Unauthorized**

--- 
<br>
</details>

<br>






<details>
<summary>Remove User Channel Invites <code>POST /api/user/channel/invites/remove</code></summary>

### Remove User Channel Invites

Remove (deny) channel invites

**Request**
``POST /api/user/channel/invites/remove``

Body Parameters - *JSON*
- ``toRemove: list (number)`` (Required) - List of invite ids be remove

Example

```http
POST http://localhost:8080/api/user/channel/invites/clear HTTP/1.1
User-Agent: vscode-restclient
Content-Type: application/json
accept-encoding: gzip, deflate
content-length: 40
cookie: t=PXb5DzwZVgaVFLcG5CApK3w44ZVh_IN9mQm5wFcVuIE=

{
  "toRemove": [
    2,
    7
  ]
}
```

**Success Response**
- **200 OK** - Invites removed

Example
```http
HTTP/1.1 200 
Content-Length: 0
Date: Sun, 24 Nov 2024 16:33:35 GMT
Keep-Alive: timeout=60
Connection: keep-alive
```

**Error Response**

- **400 Bad Request**
- **403 Forbidden**
    - *NoPermission* - You cant remove invites that you didnt receive or create
- **404 Not Found**
    - *UserInviteNotFound* - Invalid invite id

--- 
<br>
</details>

<br>








<details>
<summary>List User Channels <code>GET /api/user/channels</code></summary>

### User Channels

Obtain list of user channels.

**Request**

``GET /api/user/channels``

Example

```http
GET http://localhost:8080/api/user/channels HTTP/1.1
User-Agent: vscode-restclient
accept-encoding: gzip, deflate
cookie: t=PXb5DzwZVgaVFLcG5CApK3w44ZVh_IN9mQm5wFcVuIE=
```

**Success Response**

- **200 OK**
    - Body - *JSON*
        - ``channels: list`` - List of user channels
          - ``channelId: number`` - Channel identifier
          - ``channelName: string`` - Channel name
          - ``channelType: string`` - Channel type
          - ``channelPerms: list (string)`` - Users permissions for this channel
          - ``channelOwner: number`` - User id of channel owner
          - ``lastMessage: number`` - Id of the last messages sent to this channel
          - ``joinedAt: number`` - timestamp when user joined the channel

Example
```http
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 24 Nov 2024 16:35:25 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "channels": [
    {
      "channelId": 5,
      "channelName": "News20",
      "channelType": "private",
      "channelPerms": [
        "read",
        "write",
        "invite",
        "rename",
        "remove-users"
      ],
      "channelOwner": 1,
      "lastMessage": 13,
      "joinedAt": 1732466122
    },
    {
      "channelId": 6,
      "channelName": "News30",
      "channelType": "private",
      "channelPerms": [
        "read",
        "write",
        "invite",
        "rename",
        "remove-users"
      ],
      "channelOwner": 1,
      "lastMessage": 14,
      "joinedAt": 1732466125
    }
  ]
}
```

**Error Response**
- **400 Bad Request**
- **401 Unauthorized**

--- 
<br>
</details>

<br>









<details>
<summary>Create a channel <code>POST /api/channel/create</code></summary>

### Create a Channel

Creates a new channel.

**Request**

``POST /api/channel/create``

Body Parameters - *JSON*
- ``channelName: string`` (Required) - The channel name.
- ``channelType: string`` (Required) - The channel type.

Example

```http
POST http://localhost:8080/api/channel/create HTTP/1.1
User-Agent: vscode-restclient
Content-Type: application/json
accept-encoding: gzip, deflate
content-length: 64
cookie: t=PXb5DzwZVgaVFLcG5CApK3w44ZVh_IN9mQm5wFcVuIE=

{
  "channelName": "News100",
  "channelType": "public"
}
```

**Success Response**
- **201 Created** - Created channel successfully.
    - Body - *JSON*
        - ``channelId: number`` - Channel identifier
        - ``channelName: string`` - Channel name
        - ``channelType: string`` - Channel type
        - ``channelPerms: list (string)`` - Users permissions for this channel
        - ``channelOwner: number`` - User id of channel owner
        - ``lastMessage: number`` - Id of the last messages sent to this channel
        - ``joinedAt: number`` - timestamp

Example
```http
HTTP/1.1 201 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 24 Nov 2024 16:36:30 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "channelId": 7,
  "channelName": "News100",
  "channelType": "public",
  "channelPerms": [
    "read",
    "write",
    "invite",
    "rename",
    "remove-users"
  ],
  "channelOwner": 1,
  "lastMessage": 15,
  "joinedAt": 1732466190
}
```

**Error Response**

- **400 Bad Request**
    - *InvalidChannelName* - Channel name is not valid 
- **409 Conflict**
    - *ChannelNameTaken* - An channel with same name already exists.

--- 
<br>
</details>

<br>







<details>
<summary>Channel Info <code>GET /api/channel/info</code></summary>

### Channel Info
Info about current users channel

**Request**
``GET /api/channel/info``

Query Parameters
- ``channelId: string`` (Required) - Channel id


Example

```http
GET http://localhost:8080/api/channel/info?channelId=7 HTTP/1.1
User-Agent: vscode-restclient
Content-Type: application/json
accept-encoding: gzip, deflate
cookie: t=PXb5DzwZVgaVFLcG5CApK3w44ZVh_IN9mQm5wFcVuIE=
```

**Success Response**
- **209 Created** - Channel info
    - Body - *JSON*
        - ``channelId: number`` - Channel identifier
        - ``channelName: string`` - Channel name
        - ``channelType: string`` - Channel type
        - ``channelPerms: list (string)`` - Users permissions for this channel
        - ``channelOwner: number`` - User id of channel owner
        - ``lastMessage: number`` - Id of the last messages sent to this channel
        - ``joinedAt: number`` - timestamp

Example
```http
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 24 Nov 2024 16:38:36 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "channelId": 7,
  "channelName": "News100",
  "channelType": "public",
  "channelPerms": [
    "read",
    "write",
    "invite",
    "rename",
    "remove-users"
  ],
  "channelOwner": 1,
  "lastMessage": 15,
  "joinedAt": 1732466190
}
```

**Error Response**

- **400 Bad Request**
- **403 Forbidden**
    - *NoPermission* - You cant get info about a channel you arent member
- **404 Not Found**
    - *ChannelNotFound* - Invalid channel id

--- 
<br>
</details>
<br>



















<details>
<summary>Rename a channel <code>POST /api/channel/rename</code></summary>

### Rename a Channel

Renames a channel.

**Request**

``POST /api/channel/rename``

Body Parameters - *JSON*
- ``channelId: number`` (Required) - Channel unique identifier
- ``name: string`` (Required) - New channel name.


Example

```http
POST http://localhost:8080/api/channel/rename HTTP/1.1
User-Agent: vscode-restclient
Content-Type: application/json
accept-encoding: gzip, deflate
content-length: 48
cookie: t=PXb5DzwZVgaVFLcG5CApK3w44ZVh_IN9mQm5wFcVuIE=

{
  "channelId": 7,
  "name": "News101"
}
```

**Success Response**
- **200 OK** - Successfully renamed the channel

Example
```http
HTTP/1.1 200 
Content-Length: 0
Date: Sun, 24 Nov 2024 16:39:47 GMT
Keep-Alive: timeout=60
Connection: keep-alive
```

**Error Response**

- **400 Bad Request**
    - *InvalidChannelName* - Channel name is not valid
- **401 Unauthorized**
- **403 Forbidden**
    - *NoPermission* - You dont have permission to rename this channel
- **404 Forbidden**
    - *ChannelNotFound* - Invalid channel id
- **409 Conflict**
    - *ChannelNameTaken* - A channel with same name already exists

--- 
</details>
<br>











<details>
<summary>Search for a Channel <code>GET /api/channel/search</code></summary>

### Search for a Channel

Searches for a public channels

**Request**
``GET /api/channel/search``

Query Parameters
- ``keyword: string`` (Required) - A channels name or part of a channels name

Example

```http
GET http://localhost:8080/api/channel/search?keyword=News HTTP/1.1
User-Agent: vscode-restclient
accept-encoding: gzip, deflate
cookie: t=PXb5DzwZVgaVFLcG5CApK3w44ZVh_IN9mQm5wFcVuIE=
```

**Success Response**

- **200 OK**
    - Body - *JSON*
      - ``results: list`` - search results
          - ``channelId: number`` - Channels unique identifier
          - ``channelName: string`` - Channels name
          - ``channelType: string`` - Channels type

Example
```http
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 24 Nov 2024 16:41:00 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "results": [
    {
      "channelId": 7,
      "channelName": "News101",
      "channelType": "public"
    },
    {
      "channelId": 11,
      "channelName": "Other33",
      "channelType": "public"
    }
  ]
}
```

**Error Response**
- **400 Bad Request**
- **401 Unauthorized** - Must be logged in to search a channel

--- 
<br>
</details>

<br>











<details>
<summary>Join a Channel <code>POST /api/channel/join</code></summary>

### Join a Channel

Join a channel.

**Request**

``POST /api/channel/join``

Body Parameters - *JSON*
- ``channelId: number`` (Required) - Channel unique id
- ``inviteId: number`` (Optional) - Use this invite to join the channel

Example

```http
POST http://localhost:8080/api/channel/join HTTP/1.1
User-Agent: vscode-restclient
Content-Type: application/json
accept-encoding: gzip, deflate
content-length: 44
cookie: t=P0hAGmO7Dp4KbmhfyJeeSnzEQAyuLiXaVgjb8LQ0ZTA=

{
  "channelId": 7,
  "inviteId": 3
}
```

**Success Response**
- **200 OK** - User joined the channel
    - Body - *JSON*
        - ``channelId: number`` - Channel identifier
        - ``channelName: string`` - Channel name
        - ``channelType: string`` - Channel type
        - ``channelPerms: list (string)`` - Users permissions for this channel
        - ``channelOwner: number`` - User id of channel owner
        - ``lastMessage: number`` - Id of the last messages sent to this channel
        - ``joinedAt: number`` - timestamp

Example
```http
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 24 Nov 2024 16:45:06 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "channelId": 7,
  "channelName": "News101",
  "channelType": "public",
  "channelPerms": [
    "read",
    "write"
  ],
  "channelOwner": 1,
  "lastMessage": 15,
  "joinedAt": 1732466706
}
```

**Error Response**

- **400 Bad Request**
- **401 Unauthorized**
- **403 Forbidden**
    - *NoPermission* - You need an invite to join this channel
    - *InvalidChannelInvite* - Invite is not for this channnel or has expired
- **404 Not Found**
    - *ChannelInviteNotFound* - Invalid channel invite id
- **409 Conflict**
    - *UserAlreadyInChannel* - You are already a member of the channel  

--- 
<br>
</details>

<br>








<details>
<summary>Leave a Channel <code>POST /api/channel/leave</code></summary>

### Leave a Channel

Leave a channel.

**Request**

``POST /api/channel/leave``


Body Parameters - *JSON*
- ``channelId: number`` (Required) - Channel unique id
- ``newOwner: number`` (Required?) - If you created the channel and are not the only user in the channel, must pass the id of a user in the channel to be the new owner (this garantees theres always at least 1 users in the channel with access to all the permissions)

Example

```http
POST http://localhost:8080/api/channel/leave HTTP/1.1
User-Agent: vscode-restclient
Content-Type: application/json
accept-encoding: gzip, deflate
content-length: 24
cookie: t=P0hAGmO7Dp4KbmhfyJeeSnzEQAyuLiXaVgjb8LQ0ZTA=

{
  "channelId": 9,
  "newOwner": 33
}

```

**Success Response**
- **200 OK** - You left the channel

Example
```http
HTTP/1.1 200 
Content-Length: 0
Date: Sun, 24 Nov 2024 16:47:18 GMT
Keep-Alive: timeout=60
Connection: keep-alive
```

**Error Response**

- **400 Bad Request**
    - *SelectNewOwner* - Must set a new owner for the channel
- **404 Not Found**
    - *ChannelNotFound* - Invalid channel id
- **409 Conflict**
    - *UserNotInChannel* - You are already not a member of this channel

--- 
<br>
</details>

<br>


















<details>
<summary>Invite to a Channel <code>POST /api/channel/invite</code></summary>

### Invite to a Channel

Creates an invitation for user to join the channel.

**Request**

``POST /api/channel/invite``

Body Parameters - *JSON*
- ``channelId: number`` (Required) - Channel unique identifier
- ``username: string`` (Required) - Username the user to invite
- ``invitePerms: list`` (Required) - The permissions user will have on the channel.

Example

```http
POST http://localhost:8080/api/channel/invite HTTP/1.1
User-Agent: vscode-restclient
Content-Type: application/json
accept-encoding: gzip, deflate
content-length: 89
cookie: t=PXb5DzwZVgaVFLcG5CApK3w44ZVh_IN9mQm5wFcVuIE=

{
  "channelId": 7,
  "username": "user1",
  "invitePerms": [
    "read",
    "write"
  ]
}
```

**Success Response**

- **201 Created** - Successful created the invite
    - Body - *JSON*
        - ``inviteId: number``
        - ``senderId: string``
        - ``recipientId: string``
        - ``channelId: number``
        - ``invitePerms: list (string)``
        - ``timestamp: number``

Example
```http
HTTP/1.1 201 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 24 Nov 2024 16:43:00 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "inviteId": 3,
  "senderId": 1,
  "recipientId": 2,
  "channelId": 7,
  "invitePerms": [
    "read",
    "write"
  ],
  "timestamp": 1732466580
}
```

**Error Response**

- **400 Bad Request**
- **401 Unauthorized**
- **403 Forbidden**
    - *NoPermission* - You dont have permission to create invites for this channel
- **404 Not Found**
    - *ChannelNotFound* - Invalid channel id
    - *UserNotFound* - Invalid username
- **409 Conflict**
    - *UserAlreadyInChannel* - User is already a member of the channel
    - *UserAlreadyInvited* - User has already been invited for the channel

--- 

<br>
</details>

<br>







<details>
<summary>List Channel Invites <code>GET /api/channel/invites</code></summary>

### Channel Invites

List of users invited to the channel
**Request**
``GET /api/channel/invites``


Query Parameters
- ``channelId: string`` (Required) - Channel id

Example

```http
GET http://localhost:8080/api/channel/invites?channelId=11 HTTP/1.1
User-Agent: vscode-restclient
accept-encoding: gzip, deflate
cookie: t=r7NsEh2aC7XKB3hC1HtTWmGLKyr544RQhJjsL1zusP8=
```

**Success Response**

- **200 OK**
    - Body - *JSON*
      - ``invites: list`` - channel invites
          - ``inviteId: number``
          - ``senderId: string``
          - ``senderName: string``
          - ``recipientId: string``
          - ``recipientName: string``
          - ``channelId: string``
          - ``channelName: string``
          - ``channelType: string``
          - ``invitePerms: string``
          - ``timestamp: string``

Example
```http
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 24 Nov 2024 17:48:29 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "invites": [
    {
      "inviteId": 5,
      "senderId": 1,
      "senderName": "startuser",
      "recipientId": 34,
      "recipientName": "John34",
      "channelId": 11,
      "channelName": "News1000",
      "channelType": "public",
      "invitePerms": [
        "read",
        "write",
        "invite"
      ],
      "timestamp": 1732470468
    }
  ]
}
```

**Error Response**

- **400 Bad Request**
- **401 Unauthorized**
- **403 Forbidden**
    - *NoPermission* - You must be a member of the channel to access this information
- **404 Not Found**
    - *ChannelNotFound* - Invalid channel id

--- 
<br>
</details>

<br>








<details>
<summary>Remove Channel Invites <code>POST /api/channel/invites/remove</code></summary>

### Remove Channel Invites

Cancel a channel invite

**Request**
``POST /api/channel/invites/remove``

Body Parameters - *JSON*
- ``toRemove: list (number)`` (Required) - List of invite ids be remove

Example

```http
POST http://localhost:8080/api/channel/invites/remove HTTP/1.1
User-Agent: vscode-restclient
Content-Type: application/json
accept-encoding: gzip, deflate
content-length: 40
cookie: t=r7NsEh2aC7XKB3hC1HtTWmGLKyr544RQhJjsL1zusP8=

{
  "toRemove": [
    45,
    223
  ]
}
```

**Success Response**
- **200 OK** - Invites removed

Example
```http
HTTP/1.1 200 
Content-Length: 0
Date: Sun, 24 Nov 2024 17:50:56 GMT
Keep-Alive: timeout=60
Connection: keep-alive
```

**Error Response**

- **400 Bad Request**
- **401 Unauthorized**
- **403 Forbidden**
    - *NoPermission* - You must have channel permission or own the invite to remove an invite
- **404 Not Found**
    - *ChannelInviteNotFound* - Invalid invite id


--- 
<br>
</details>

<br>








<details>
<summary>List Channel Users <code>GET /api/channel/users</code></summary>

### Channel Users

List of users in the channel
**Request**
``GET /api/channel/users``


Query Parameters
- ``channelId: string`` (Required) - Channel id

Example

```http
GET http://localhost:8080/api/channel/users?channelId=11 HTTP/1.1
User-Agent: vscode-restclient
accept-encoding: gzip, deflate
cookie: t=r7NsEh2aC7XKB3hC1HtTWmGLKyr544RQhJjsL1zusP8=
```

**Success Response**

- **200 OK**
    - Body - *JSON*
      - ``users: list``- list of users in the channel
          - ``userId: number``
          - ``username: string``
          - ``userPerms: list (string)``
          - ``joinedAt: string``

Example
```http
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 24 Nov 2024 17:52:55 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "users": [
    {
      "userId": 1,
      "username": "startuser",
      "userPerms": [
        "read",
        "write",
        "invite",
        "rename",
        "remove-users"
      ],
      "joinedAt": 1732470458
    },
    {
      "userId": 5,
      "username": "test-user",
      "userPerms": [
        "read",
        "invite",
      ],
      "joinedAt": 1732470958
    }
  ]
}
```

**Error Response**

- **400 Bad Request**
- **401 Unauthorized**
- **403 Forbidden**
    - *NoPermission* - You must be a member of the channel to access this information
- **404 Not Forbidden**
    - *ChannelNotFound* - Invalid channel id

--- 
<br>
</details>

<br>









<details>
<summary>Remove Channel Users <code>POST /api/channel/users/remove</code></summary>

### Remove Channel Users

Remove users from the channel

**Request**
``POST /api/channel/users/remove``

Body Parameters - *JSON*
- ``toRemove: list (number)`` (Required) - List of users ids be remove

Example

```http
POST http://localhost:8080/api/channel/invites/remove HTTP/1.1
User-Agent: vscode-restclient
Content-Type: application/json
accept-encoding: gzip, deflate
content-length: 40
cookie: t=r7NsEh2aC7XKB3hC1HtTWmGLKyr544RQhJjsL1zusP8=

{
  "toRemove": [
    100,
    200
  ]
}
```

**Success Response**
- **200 OK** - Users removed from the channel

Example
```http
HTTP/1.1 200 
Content-Length: 0
Date: Sun, 24 Nov 2024 17:50:56 GMT
Keep-Alive: timeout=60
Connection: keep-alive
```

**Error Response**

- **400 Bad Request**
- **401 Unauthorized**
- **403 Forbidden**
    - *NoPermission* - You dont have permission to remove users from the channel
- **404 Not Found**
    - *ChannelNotFound* - Invalid user id
- **409 Conflict**
    - *CantRemoveOwner* - Channel owner cant be removed
    - *CantSelfRemove* - You cant remove yourself, leave the channel instead
    - *UserNotInChannel* - User you trying to remove is not in the channel



--- 
<br>
</details>

<br>












<details>
<summary>Create a Message<code>POST /api/channel/messages/create</code></summary>

### Create a Massage

Send a message to channel.

**Request**

``POST /api/channel/messages/create``

Body Parameters - *JSON*
- ``channelId: number`` (Required)
- ``type: string`` (Required) 
- ``content: string`` (Required)

Example

```http
POST http://localhost:8080/api/channel/messages/create HTTP/1.1
User-Agent: vscode-restclient
Content-Type: application/json
accept-encoding: gzip, deflate
content-length: 90
cookie: t=r7NsEh2aC7XKB3hC1HtTWmGLKyr544RQhJjsL1zusP8=

{
  "channelId": 11,
  "type": "text",
  "content": "hello there!"
}
```

**Success Response**

- **200 OK** - The message was added to the channel
    - Body - *JSON*
        - ``messageId: number``
        - ``userId: number``
        - ``channusernameelId: number``
        - ``type: string``
        - ``content: string``
        - ``timestamp: number``

Example
````http
HTTP/1.1 201 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 24 Nov 2024 17:54:37 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "messageId": 21,
  "userId": 1,
  "username": "startuser",
  "type": "text",
  "content": "hello there!",
  "timestamp": 1732470877
}
````

**Error Response**

- **400 Bad Request**
- **401 Unauthorized**
- **403 Forbidden**
    - *NoPermission* - You dont have permission to write to this channel
- **404 Not Found**
    - *ChannelNotFound* - INvalid channel id

--- 


<br>

</details>

<br>





<details>
<summary>Get Channel Messages<code>POST /api/channel/messages</code></summary>

### Get Channel Messages

Obtain latest messages that happened on a channel

**Request**

``POST /api/channel/messages``


Body Parameters - *JSON*
- ``load: list`` (Required) - list of channels to load message form
    - ``channelId: string`` (Required) 
    - ``lastMessage: string`` (Optional) - return only messages previous to this id (default returns from the most recent)
    - ``limit: string`` (Optional) - max number of messages to return (default 100)
    - ``filter: list (string)`` (Optional) - return only messages with this types (default returns all message types)


Example

```http
POST http://localhost:8080/api/channel/messages HTTP/1.1
User-Agent: vscode-restclient
Content-Type: application/json
accept-encoding: gzip, deflate
content-length: 238
cookie: t=r7NsEh2aC7XKB3hC1HtTWmGLKyr544RQhJjsL1zusP8=

{
  "load": [
    {
      "channelId": 11
    },
    {
      "channelId": 7,
      "lastMessage": 10,
      "limit": 5,
      "filter": [
        "channel-create",
        "text"
      ]
    }
  ]
}
```

**Success Response**

- **200 OK** - Result list
    - Body - *JSON*
        - ``results: list``
          - ``channelId: number``
            - ``channelId: number``
            - ``channelName: number``
            - ``channelMessages: list``
                - ``messageId: string``
                - ``userId: number``
                - ``username: number``
                - ``type: number``
                - ``content: number``
                - ``timestamp: number``

Example
```http
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 24 Nov 2024 17:56:52 GMT
Keep-Alive: timeout=60
Connection: keep-alive

{
  "results": {
    "11": {
      "channelId": 11,
      "channelName": "News1000",
      "channelMessages": [
        {
          "messageId": 21,
          "userId": 1,
          "username": "startuser",
          "type": "text",
          "content": "this is a message to ch3",
          "timestamp": 1732470877
        },
        {
          "messageId": 20,
          "userId": 1,
          "username": "startuser",
          "type": "channel-create",
          "content": null,
          "timestamp": 1732470458
        }
      ]
    },
    "7": {
      "channelId": 7,
      "channelName": "News101",
      "channelMessages": []
    }
  }
}
```

**Error Response**

- **400 Bad Request**
    - *InsecurePassword*
    - *InvalidUsername*
- **403 Forbidden**
    - *InvalidUserInvite*
- **409 Conflict**
    - *UsernameTaken*

--- 

<br>
</details>

<br>







































#### Channel Types
- ``public`` - 
- ``private`` -

#### Channel Permissons
- ``write`` -
- ``invite`` - 
- ``rename`` - 


#### Errors

<details>
<summary><i>InvalidRequestParameters</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/invalid-request-parameters``
- **Title:**    Invalid parameters
- **Details:** Invalid or missing request arguments.

</details>
<br>

<details>
<summary><i>ChannelNameTaken</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/channel-name-taken``
- **Title:** Channel with same name already exists
- **Details:** A channel with the given name already exists.

</details>
<br>

<details>
<summary><i>InvalidChannelName</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/invalid-channel-name``
- **Title:** Channel name is not valid
- **Details:** The given channel name does not meet minimum length requirements.

</details>
<br>

<details>
<summary><i>NoPermission</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/no-permission``
- **Title:** No permission to perform operation
- **Details:** User does not have permission to perform the requested operation.

</details>
<br>

<details>
<summary><i>ChannelNotFound</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/channel-not-found``
- **Title:** Channel not found
- **Details:** The given channel id does not index any existing channel.

</details>
<br>


<details>
<summary><i>SelectNewOwner</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/select-owner``
- **Title:** Must select a new channel owner
- **Details:** Must pass new owner id when leaving a channel that you created.

</details>
<br>


<details>
<summary><i>CantSelfRemove</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/cannot-self-remove``
- **Title:** Cannot remove yourself
- **Details:** You cannot remove yourself from a channel, leave the channel instead.

</details>
<br>

<details>
<summary><i>CantRemoveOwner</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/cant-remove-owner``
- **Title:** Cannot remove owner
- **Details:** The owner of the channel cannot be removed.

</details>
<br>

<details>
<summary><i>UserAlreadyInChannel</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/user-aleady-in-channel``
- **Title:** User already joined the channel
- **Details:** The given user is already a member of the channel.

</details>
<br>

<details>
<summary><i>UserAlreadyInvited</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/user-already-invited``
- **Title:** User already invited
- **Details:** An invite for the given user for this channel already exists.

</details>
<br>

<details>
<summary><i>UserNotFound</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/user-not-found``
- **Title:** User not found
- **Details:** No user found matching the given username or id

</details>
<br>

<details>
<summary><i>InvalidChannelInvite</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/invalid-channel-invite``
- **Title:** Invalid channel invite
- **Details:** The given invite cant be used for this channel, has benn already used or has been removed

</details>
<br>

<details>
<summary><i>ChannelInviteNotFound</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/channel-invite-not-found``
- **Title:** Channel invite not found
- **Details:** No invites matching the given invite id found.

</details>
<br>

<details>
<summary><i>UserNotInChannel</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/user-not-in-channel``
- **Title:** User is not a member of the channel
- **Details:** The given user is not a member of the channel.

</details>
<br>


<details>
<summary><i>InsecurePassword</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/insecure-password``
- **Title:** Password is not secure
- **Details:** The password must have at least 8 characters.

</details>
<br>


<details>
<summary><i>InvalidUsername</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/invalid-username``
- **Title:** Username is too short
- **Details:** The given username is too short and cannot be used.

</details>
<br>


<details>
<summary><i>UsernameTaken</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/username-taken``
- **Title:** Username already exists
- **Details:** A user with the same username already exists.

</details>
<br>

<details>
<summary><i>InvalidCredentials</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/invalid-credentials``
- **Title:** Invalid credentials
- **Details:** The given username and password do not match for any user

</details>
<br>

<details>
<summary><i>InvalidUserInvite</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/invalid-user-invite``
- **Title:** Invite is not valid
- **Details:** The given invite no longer exists or has already been used

</details>
<br>

<details>
<summary><i>UserInviteNotFound</i></summary>

- **Type:** ``https://github.com/isel-leic-daw/2024-daw-leic51n-gn12/tree/main/docs/error/user-invite-not-found``
- **Title:** Invite not found
- **Details:** The given invite token does not match any record

</details>
<br>







