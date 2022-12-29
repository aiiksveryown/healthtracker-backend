package ie.setu.utils

import com.auth0.jwt.algorithms.Algorithm

object Cipher {
    val algorithm = Algorithm.HMAC256("UroefwPqGJEjAuECLxfMjHztjRjrrPhCEbqQvAToyoHFVYYp")

    fun encrypt(data: String?): ByteArray =
        algorithm.sign(data?.toByteArray())

}