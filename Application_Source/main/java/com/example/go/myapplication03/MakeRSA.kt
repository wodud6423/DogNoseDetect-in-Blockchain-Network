import kotlin.random.Random
import java.math.BigInteger

class MakeRSA {
    fun generateRandomPrime(): Int {
        while (true) {
            val num = Random.nextInt(100, 1000)
            if (num < 2) {
                return -1
            }

            for (i in 2..(Math.sqrt(num.toDouble())).toInt() + 1) {
                if (num % i == 0) {
                    return -1
                }
            }
            return num
        }
    }

    fun generateKeypair(p: Int, q: Int): List<List<Int>> {
        val n = p * q
        val phi = (p - 1) * (q - 1)
        var e = Random.nextInt(1, phi)

        fun gcd(a: Int, b: Int): Int {
            return if (b == 0) a else gcd(b, a % b)
        }

        var g = gcd(e, phi)

        while (g != 1) {
            e = Random.nextInt(1, phi)
            g = gcd(e, phi)
        }

        var d = 0
        var x1 = 0
        var x2 = 1
        var y1 = 1
        var tempPhi = phi
        val e1 = e

        while (e > 0) {
            val temp1 = tempPhi / e
            val temp2 = tempPhi - temp1 * e
            tempPhi = e
            e = temp2
            val x = x2 - temp1 * x1
            val y = d - temp1 * y1
            x2 = x1
            x1 = x
            d = y1
            y1 = y
        }

        if (tempPhi == 1) {
            d += phi
        }

        return listOf(listOf(e1, n), listOf(d, n))
    }

    fun encrypt(publicKey: List<Int>, plaintext: String): List<Int> {
        val key = publicKey[0]
        val n = publicKey[1]
        val cipher = plaintext.map { BigInteger.valueOf(it.toLong()).modPow(BigInteger.valueOf(key.toLong()), BigInteger.valueOf(n.toLong())).toInt() }
        return cipher
    }

    fun decrypt(privateKey: List<Int>, ciphertext: List<Int>): String {
        val key = privateKey[0]
        val n = privateKey[1]
        val plain = ciphertext.map { BigInteger.valueOf(it.toLong()).modPow(BigInteger.valueOf(key.toLong()), BigInteger.valueOf(n.toLong())).toInt().toChar() }
        return plain.joinToString("")
    }
}
