package ie.setu.utils

import com.auth0.jwt.interfaces.DecodedJWT
import ie.setu.config.Roles
import io.javalin.core.security.RouteRole
import io.javalin.http.Context

fun getJwtTokenHeader(ctx: Context): DecodedJWT? {
    val tokenHeader = ctx.header("Authorization")?.substringAfter("Bearer")?.trim()
        ?: return null

    return JwtProvider.decodeJWT(tokenHeader)
}

fun getEmail(jwtToken: DecodedJWT?): String? {
    return jwtToken?.subject
}

fun getUserRole(jwtToken: DecodedJWT?): RouteRole? {
    val adminUserRole = jwtToken?.getClaim("role")?.asString() ?: return null
    return Roles.valueOf(adminUserRole)
}