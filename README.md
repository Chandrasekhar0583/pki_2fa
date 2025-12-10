🔐 PKI-based 2FA System (Spring Boot + Docker + Cron)

This project implements a Public Key Infrastructure (PKI) based Two-Factor Authentication (2FA) system using:

Spring Boot

RSA Encryption (OAEP-SHA256)

HMAC-based TOTP generation

Docker + Cron (automatic code generation every minute)

📁 Project Structure
pki2fa/
│
├── src/
│   ├── main/
│   │   ├── java/com/pki2fa/
│   │   │   ├── controller/TotpController.java
│   │   │   ├── service/CryptoService.java
│   │   │   ├── service/TotpService.java
│   │   │   └── Pki2faApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data/
│   │           ├── student_public.pem
│   │           ├── student_private.pem
│   │           ├── encrypted_seed.txt
│   │           └── (auto-generated: seed.txt)
│
├── data/                 # copied into Docker image
│   ├── student_private.pem
│   ├── student_public.pem
│   ├── encrypted_seed.txt
│
├── cron/
│   └── 2fa-cron          # cron job definition
│
├── scripts/
│   ├── log_2fa_cron.py   # cron python script
│   └── generate_commit_proof.py
│
├── Dockerfile
├── pom.xml
└── README.md

⚙️ Features
✔ RSA-OAEP Decryption

Decrypts encrypted 64-byte seed using your student_private.pem.

✔ TOTP Generation

Generates 6-digit codes valid for 30 seconds.

✔ Verification API

Verify a submitted 2FA code within a ±30 second window.

✔ Dockerized Runtime

Spring Boot + Cron job automatically logs a 2FA code every minute.

🐳 Docker Build & Run
1️⃣ Build the Docker image
docker build -t pki2fa .

2️⃣ Run container
docker run -d -p 8080:8080 --name pki2fa pki2fa

3️⃣ Verify /data inside container
docker exec pki2fa ls -l /data


Expected:

encrypted_seed.txt
student_private.pem
student_public.pem
seed.txt (after decryption)

🔑 1. Decrypt Seed (POST /decrypt-seed)
Example Request
curl -X POST http://localhost:8080/decrypt-seed \
  -H "Content-Type: application/json" \
  -d '{ "encrypted_seed": "<YOUR_BASE64_ENCRYPTED_SEED>" }'


Expected Response:

{ "status": "ok" }


This generates:

/data/seed.txt

⏱ 2. Generate TOTP (GET /totp)
curl http://localhost:8080/totp


Returns:

123456

🛂 3. Verify TOTP (POST /verify-2fa)
curl -X POST http://localhost:8080/verify-2fa \
  -H "Content-Type: application/json" \
  -d '{ "code": "123456" }'


Response:

{ "valid": true }

🕒 Cron: Automatic 2FA Logging

Cron runs every minute and executes:

/scripts/log_2fa_cron.py


Output logged in:

/cron/last_code.txt


Example:

2025-12-10 08:23:01 - 2FA Code: 508958

🔏 Generate Commit Proof (Python Script)

Script: scripts/generate_commit_proof.py

Run:
python scripts/generate_commit_proof.py

It asks:
Enter commit hash:

Output example:
Commit Hash: 82a2d158debb5ace11d32587bd51ad8ef4c271f7
Encrypted Signature: <VERY_LONG_BASE64_STRING>


Send the encrypted signature for verification.

📄 .gitignore
encrypted_seed.txt
__pycache__/
*.pyc
.env
.vscode/
.idea/
target/
.DS_Store
Thumbs.db
