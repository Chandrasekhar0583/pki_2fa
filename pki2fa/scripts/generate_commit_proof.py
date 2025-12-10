import base64
import os
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives.serialization import load_pem_private_key, load_pem_public_key

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(BASE_DIR, "..", "data")

def sign_message(message: str, private_key_pem: str) -> bytes:
    private_key = load_pem_private_key(private_key_pem.encode(), password=None)

    signature = private_key.sign(
        message.encode("utf-8"),
        padding.PSS(
            mgf=padding.MGF1(hashes.SHA256()),
            salt_length=padding.PSS.MAX_LENGTH
        ),
        hashes.SHA256()
    )
    return signature

def encrypt_with_public_key(data: bytes, public_key_pem: str) -> bytes:
    public_key = load_pem_public_key(public_key_pem.encode())

    ciphertext = public_key.encrypt(
        data,
        padding.OAEP(
            mgf=padding.MGF1(algorithm=hashes.SHA256()),
            algorithm=hashes.SHA256(),
            label=None,
        )
    )
    return ciphertext


if __name__ == "__main__":
    # Ask user for commit hash
    commit_hash = input("Enter commit hash: ").strip()

    # Load student private key
    with open(os.path.join(DATA_DIR, "student_private.pem"), "r") as f:
        private_key_pem = f.read()

    # Sign commit hash
    signature = sign_message(commit_hash, private_key_pem)

    # Load instructor public key
    with open(os.path.join(DATA_DIR, "instructor_public.pem"), "r") as f:
        instructor_pub = f.read()

    # Encrypt signature
    encrypted = encrypt_with_public_key(signature, instructor_pub)

    # Output final encoded proof
    print("\nCommit Hash:", commit_hash)
    print("Encrypted Signature:", base64.b64encode(encrypted).decode())