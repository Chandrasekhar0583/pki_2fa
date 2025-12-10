

import base64
import hmac
import hashlib
import time
import os

DATA_PATH = "/data/seed.txt"

def hex_to_bytes(hex_str):
    return bytes.fromhex(hex_str)

def generate_totp(secret_hex, interval=30):
    key = hex_to_bytes(secret_hex)
    timestep = int(time.time() // interval)

    msg = timestep.to_bytes(8, 'big')
    h = hmac.new(key, msg, hashlib.sha1).digest()

    o = h[-1] & 0x0F
    binary = ((h[o] & 0x7F) << 24) | ((h[o+1] & 0xFF) << 16) | ((h[o+2] & 0xFF) << 8) | (h[o+3] & 0xFF)

    return f"{binary % 1_000_000:06d}"

try:
    if not os.path.exists(DATA_PATH):
        print("Seed not found")
        exit(0)

    with open(DATA_PATH, "r") as f:
        seed = f.read().strip()

    code = generate_totp(seed)

    ts = time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime())
    print(f"{ts} - 2FA Code: {code}")

except Exception as e:
    print(f"Error: {e}")
