import http.client
import json

conn = http.client.HTTPConnection("localhost", 8080)

headersList = {
 "Accept": "*/*",
 "User-Agent": "Thunder Client (https://www.thunderclient.com)",
 "Content-Type": "application/json" 
}

payload = json.dumps({
  "username": "user1",
  "password": "password1"
})

conn.request("POST", "/api/user/create", payload, headersList)
response = conn.getresponse()

result = response.read()
response.
print(result.decode("utf-8"))