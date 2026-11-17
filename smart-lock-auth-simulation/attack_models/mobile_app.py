import tkinter as tk
from tkinter import messagebox
import requests
import base64
import time
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad

KEY = b'1234567890123456'

def send_payload(timestamp, is_attack):
    plaintext = f'ID:12345|ROOM:CS428|TS:{timestamp}'.encode('utf-8')
    cipher = AES.new(KEY, AES.MODE_ECB)
    padded_data = pad(plaintext, AES.block_size)
    encrypted_bytes = cipher.encrypt(padded_data)
    encrypted_payload = base64.b64encode(encrypted_bytes).decode('utf-8')

    try:
        response = requests.post(
            'http://localhost:8080/api/auth/verify',
            json={"data": encrypted_payload}
        )
        
        if is_attack:
            if response.status_code == 403:
                messagebox.showinfo("Attack Blocked", f"Server successfully blocked the attack: {response.text}")
            else:
                messagebox.showwarning("Attack Status", f"Unexpected response: {response.status_code}")
        else:
            if response.status_code == 200:
                messagebox.showinfo("Access Granted", "Valid request transmitted. Door unlocked!")
            else:
                messagebox.showerror("Error", f"Access Denied: {response.text}")
                
    except requests.exceptions.ConnectionError:
        messagebox.showerror("Error", "Connection failed. Ensure the Spring Boot API is running.")

def valid_request():
    current_ts = int(time.time())
    send_payload(current_ts, False)

def replay_attack():
    old_ts = 1700000000 
    send_payload(old_ts, True)

root = tk.Tk()
root.title("Smart Lock Mobile App")
root.geometry("350x250")
root.configure(bg="#2c3e50")

tk.Label(root, text="Classroom Access", font=("Helvetica", 16, "bold"), fg="white", bg="#2c3e50").pack(pady=20)

tk.Button(root, text="🔓 Unlock Door (Valid)", font=("Helvetica", 12), bg="#27ae60", fg="white", width=25, command=valid_request).pack(pady=10)
tk.Button(root, text="⚠️ Simulate Replay Attack", font=("Helvetica", 12), bg="#c0392b", fg="white", width=25, command=replay_attack).pack(pady=10)

root.mainloop()