import base64
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives.serialization import load_pem_private_key, load_pem_public_key


def sign_message(message: str, private_key_pem: str) -> bytes:
    private_key = load_pem_private_key(private_key_pem.encode(), password=None)

    signature = private_key.sign(
        message.encode("utf-8"),
        padding.PSS(
            mgf=padding.MGF1(hashes.SHA256()),
            salt_length=padding.PSS.MAX_LENGTH
        ),
        hashes.SHA256(),
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
        ),
    )

    return ciphertext


if __name__ == "__main__":

    commit_hash = input("Enter your 40-character commit hash: ").strip()

    if len(commit_hash) != 40:
        print("❌ ERROR: Commit hash must be exactly 40 characters!")
        exit(1)

    with open("student_private.pem", "r") as f:
        private_key_pem = f.read()

    signature = sign_message(commit_hash, private_key_pem)

    with open("instructor_public.pem", "r") as f:
        instructor_pub = f.read()

    encrypted = encrypt_with_public_key(signature, instructor_pub)

    encrypted_b64 = base64.b64encode(encrypted).decode()

    print("\n==============================")
    print("Commit Hash:", commit_hash)
    print("Encrypted Signature:", encrypted_b64)
    print("==============================")