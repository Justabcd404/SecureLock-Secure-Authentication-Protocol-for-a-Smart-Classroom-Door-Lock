import socket
import base64
import time
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad

key = b'1234567890123456'
current_ts = int(time.time())
plaintext = f'ID:12345|ROOM:CS428|TS:{current_ts}'.encode('utf-8')

cipher = AES.new(key, AES.MODE_ECB)
padded_data = pad(plaintext, AES.block_size)
encrypted_bytes = cipher.encrypt(padded_data)
encrypted_payload = base64.b64encode(encrypted_bytes).decode('utf-8')

print("Intercepted/Forged Payload:", encrypted_payload)

try:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect(('127.0.0.1', 8080))
        s.sendall(encrypted_payload.encode('utf-8'))
        print("Fresh access request sent to Authentication Server.")
except ConnectionRefusedError:
    print("Connection failed. Ensure the Java AuthServer is running.")