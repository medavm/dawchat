
import json
import pprint
import urllib3
import sseclient
import json
import string
import random

BASE_URL = "http://localhost:8080/api"

def with_urllib3(url, headers):
    """Get a streaming response for the given event feed using urllib3."""
    
    http = urllib3.PoolManager()
    return http.request('GET', url, preload_content=False, headers=headers)

class User:
    def __init__(self, userId, username, session) -> None:
        self.userId = userId
        self.username = username
        self.token = session

def rand(size=6, chars=string.ascii_uppercase + string.digits):
    return ''.join(random.choice(chars) for _ in range(size))

def createRequest(type, url, data={}, auth=None):
    http = urllib3.PoolManager()
    if data:
        headers = {"Content-Type": "application/json"}
    if auth:
        headers["Authorization"] = "Bearer "+auth
    return http.request(type, url, body=json.dumps(data) if data else None, headers=headers, preload_content=False)

def createUser(username: str, password: str, token: str):
    body = {"username": username, "password": password, "inviteToken": token}
    response = createRequest("POST", BASE_URL+"/user/create", body)
    if response.status == 201:
        result  = response.json()
        return User(result["userId"], result["username"])
    else:
        print(f"CreateUser failed, status {response.status}")


def createUserInvite(auth: str):
    response = createRequest("POST", BASE_URL+"/user/create", auth=auth)
    if response.status == 201:
        result  = response.json()
        return User(result["userId"], result["username"])
    else:
        print(f"CreateUser failed, status {response.status}")

startUser: User = None

def createUserAndLogin(username: str, passw: string):
    global startUser
    """
     if(startUser ==null){
            val startUserToken = login(client, STARTUSER_NAME, STARTUSER_PASSW)
                .expectResult(HttpStatus.OK, LoginOutputModel::class.java).token
            startUser = TestUser(STARTUSER_ID, STARTUSER_NAME, STARTUSER_PASSW, startUserToken)
        }

        val invite = createUserInvite(client, startUser!!.token)
            .expectResult(HttpStatus.CREATED, UserInviteOutputModel::class.java)
        val user1 = createUser(client, username, passw, invite.token)
            .expectResult(HttpStatus.CREATED, UserInfoOutputModel::class.java)
        val token = login(client, username, passw)
            .expectResult(HttpStatus.OK, LoginOutputModel::class.java).token
        return TestUser(user1.userId, username, passw, token)
    """

    if not startUser:
        startUserToken = login(startUser.userId)
    

def login(username: str, password: str) -> str: 
    body = {"username": username, "password": password}
    response = createRequest("POST", BASE_URL+"/user/login", body)
    if response.status == 200:
        return response.json()["token"]
    else:
        print(response.status,": ",response.data)

def createChannel(name: str, type: str, auth: str) -> str: 
    body = {"channelName": name, "type": type}
    response = createRequest("POST", BASE_URL+"/channel/create", body, auth)
    if response.status == 201:
        return response.json()
    else:
        print(response.status,": ",response.data)

def joinChannel(chId: int, auth: str) -> str: 
    body = {"channelId": chId}
    response = createRequest("POST", BASE_URL+"/channel/join", body, auth)
    if response.status == 200:
        return None
    else:
        print(response.status,": ",response.data)

def userChannels(auth: str) -> str: 
    response = createRequest("GET", BASE_URL+"/user/channels",auth=auth)
    if response.status == 200:
        return response.json()
    else:
        print(response.status,": ",response.data)


user1 = createUser("user-"+rand(), "password")
token1 = login(user1.username, "password")
user2 = createUser("user-"+rand(), "password")
token2 = login(user1.username, "password")
user3 = createUser("user-"+rand(), "password")
token3 = login(user1.username, "password")
createChannel()
#createChannel("channel1", "public", token1)

print(f"{user1.username}: {token1}")
print(f"{user2.username}: {token2}")
print(f"{user3.username}: {token3}")