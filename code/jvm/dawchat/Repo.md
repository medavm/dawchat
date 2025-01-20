


### Repository

**Users**
    
| Field        |                 |  |
|--------------|-----------------|--|
| **Id**       | primary key     |  |
| **Username** | unique not null |  |
| **Secret**   | not null        |  |
<br>

**Sessions**

| Field         |         |                         |   |
|---------------|---------|-------------------------|---|
| **Token**     | varchar | unique primary key      |   |
| **userId**    | int     | not null ref *Users.id* |   |
| **createdAt** | bigint  | not null                |   |
| **lastUsed**  | bigint  | not null                |   |
<br>

**Channels**

| Field         |         |                         |   |
|---------------|---------|-------------------------|---|
| **Id**        | int     | unique primary key      |   |
| **name**      | varchar | unique not null         |   |
| **type**      | int     | not null                |   |
| **createdBy** | int     | not null ref *Users.id* |   |
| **createdAt** | bigint  | not null                |   |
<br>

**ChannelUsers**

| Field       |        |                            |   |
|-------------|--------|----------------------------|---|
| **userId**      | int    | not null ref *Users.id*    |   |
| **channelId**   | int    | not null ref *Channels.id* |   |
| **permissions** | int    | not null                   |   |
| **iniviteId**   | int    | null ref *Invites.id*      |   |
| **joinedAt**    | bigint | not null                   |   |
<br>

**Invites**

| Field       |         |                         |   |
|-------------|---------|-------------------------|---|
| **id**          | int     | primary key             |   |
| **senderId**    | int     | not null ref *Users.id* |   |
| **recipientId** | int     | not null red *Users.id* |   |
| **channelId**   | int     | null ref *Channels.id*  |   |
| **permissions** | int     | not null                |   |
| **message**     | varchar | null                    |   |
| **status**      | int     | not null                |   |
| **createdAt**   | bigint  | not null                |   |
| **resolvedAt**  | bigint  | null                    |   |
<br>

**Messages**

| Field      |         |                            |   |
|------------|---------|----------------------------|---|
| **id**         | int     | primary key                |   |
| **userId**     | int     | not null ref *Users.id*    |   |
| **channelId**  | int     | not null ref *Channels.id* |   |
| **type**       | varchar | not null                   |   |
| **content**    | text    | not null                   |   |
| **createdAt**  | bigint  | not null                   |   |
<br>



