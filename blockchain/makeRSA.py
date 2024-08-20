import random

class makeRSA:
    def generate_random_prime():
        while True:
            num = random.randint(100, 1000)
            if num < 2:
                return False

            for i in range(2, int(num ** 0.5) + 1):
                if num % i == 0:
                    return False
            return num

    def generate_keypair(p, q):
        n = p * q
        phi = (p - 1) * (q - 1)
        e = random.randrange(1, phi)
        def gcd(a, b):
            if b == 0:
                return a

            else:
                return gcd(b, a % b)
        g = gcd(e, phi)

        while g != 1:
            e = random.randrange(1, phi)
            g = gcd(e, phi)
        d = 0
        x1 = 0
        x2 = 1
        y1 = 1
        temp_phi = phi
        e1 = e

        while e > 0:
            temp1 = temp_phi // e
            temp2 = temp_phi - temp1 * e
            temp_phi = e
            e = temp2
            x = x2 - temp1 * x1
            y = d - temp1 * y1
            x2 = x1
            x1 = x
            d = y1
            y1 = y

        if temp_phi == 1:
            d = d + phi

        return ((e1, n), (d, n))

    def encrypt(public_key, plaintext):
        key, n = public_key
        cipher = [(ord(char) ** key) % n for char in plaintext]

        return cipher

    def decrypt(private_key, ciphertext):
        key, n = private_key
        plain = [chr((char ** key) % n) for char in ciphertext]

        return ''.join(plain)

