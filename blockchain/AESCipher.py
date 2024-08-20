import base64
import hashlib
from Crypto.Cipher import AES

class AESCipher(object):
    def __init__(self, key):
        self.key = hashlib.sha256(key.encode()).digest()
        
    def pad(self, message):
        block_size = AES.block_size
        padding_size = block_size - len(message) % block_size
        padding = chr(padding_size).encode() * padding_size
        padded_message = message + padding
        return padded_message
    
    def encrypt(self, message):
        raw = self.pad(message.encode())
        cipher = AES.new(self.key, AES.MODE_CBC, self.__iv().encode('utf8'))
        enc = cipher.encrypt(raw)
        return base64.b64encode(enc).decode('utf-8')
    
    def decrypt(self, enc):
        enc = base64.b64decode(enc)
        cipher = AES.new(self.key, AES.MODE_CBC, self.__iv().encode('utf8'))
        dec = cipher.decrypt(enc)
        return self.unpad(dec).decode('utf-8')
    
    def __iv(self):
        return chr(0) * 16
    
    def unpad(self, message):
        padding_size = message[-1]
        return message[:-padding_size]
