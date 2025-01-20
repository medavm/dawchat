
import json
import pprint
import urllib3
import sseclient
import json
import string
import time
import socket
import sys


auth = sys.argv[1]
request = f"GET /api/user/listener HTTP/1.1\r\nHost: localhost\r\nAuthorization: Bearer {auth}\r\n\r\n"

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(("localhost", 8080))
s.send(request.encode())
#s.settimeout(10)
result = s.recv(1024)

while result:
    try:
        print(result)
        print()
        result = s.recv(1024)
    except:
        break