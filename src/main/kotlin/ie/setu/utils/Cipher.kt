package ie.setu.utils

import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object Cipher {
    val algorithm: Algorithm = Algorithm.HMAC256("UroefwPqGJEjAuECLxfMjHztjRjrrPhCEbqQvAToyoHFVYYp")

    private fun encrypt(data: String?): ByteArray =
        algorithm.sign(data?.toByteArray())

    private fun encodeBase64(data: ByteArray): String =
        String(Base64.getEncoder().encode(data))

    fun encodePassword(password: String): String {
        return encodeBase64(encrypt(password))
    }
}