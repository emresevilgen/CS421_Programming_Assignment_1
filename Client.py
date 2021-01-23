import socket
import sys

HOST = '127.0.0.1'
PORT = 60000

try:
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    print ("Socket created")
except socket.error as error:
    print("Socket error:", error)
    sys.exit()

s.connect((HOST, PORT))

def send(message):
    s.send(message.encode('ascii'))
    data = s.recv(1024) 
    print('Received from the server : \"') 
    print(data, end="")
    print("\"")

send("USER bilkentstu\r\n")
send("PASS cs421s2020\r\n")
