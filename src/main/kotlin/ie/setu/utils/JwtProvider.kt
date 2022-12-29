package ie.setu.utils
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import io.javalin.core.security.RouteRole
import ie.setu.domain.AdminUser
import java.util.*

object JwtProvider {
    fun decodeJWT(token: String): DecodedJWT = JWT.require(Cipher.algorithm).build().verify(token)
    fun createJWT(user: AdminUser, role: String): String? =
        JWT.create()
            .withIssuedAt(Date())
            .withSubject(user.email)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + 1 * 24 * 60 * 60 * 1000))
            .sign(Cipher.algorithm)
}