import hashlib
import bcrypt


class HashManager:
    DEFAULT_ROUND=14
    DEFAULT_SALT_LENGTH=22

    def __init__(self):
        pass
    
    def compute_sha3hex(self, user_password, user_salt, round, challenge):
        bcrypt_init = f"$2b${round}${user_salt}"   
        bcrypt_hash = bcrypt.hashpw(user_password.encode(), bcrypt_init.encode())
        challenge_req = f"{challenge}{bcrypt_hash.decode()}"
        sha3 = hashlib.sha3_256()
        sha3.update(challenge_req.encode()) 
        return sha3.hexdigest()

    def compute_bcrypt2(self, user_password):
        bcrypt_salt = bcrypt.gensalt(HashManager.DEFAULT_ROUND)
        bcrypt_hash = bcrypt.hashpw(user_password.encode(), bcrypt_salt)
        return bcrypt_hash.decode()

    def salt_length(self):
        return HashManager.DEFAULT_SALT_LENGTH