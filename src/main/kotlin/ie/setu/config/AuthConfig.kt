package ie.setu.config

import ie.setu.utils.JwtProvider

import com.auth0.jwt.interfaces.DecodedJWT
import io.javalin.core.security.RouteRole
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.ForbiddenResponse

private const val headerTokenName = "Authorization"

object AuthConfig {
    fun manage(handler: Handler, ctx: Context, permittedRoles: Set<RouteRole>) {
        // Skip auth for swagger
        if (ctx.path().startsWith("/swagger")) {
            handler.handle(ctx)
            return
        }
        val jwtToken = getJwtTokenHeader(ctx)
        val userRole = getUserRole(jwtToken) ?: Roles.UNAUTHENTICATED
        if (userRole !in permittedRoles) {
            throw ForbiddenResponse()
        }
        ctx.attribute("email", getEmail(jwtToken))
        handler.handle(ctx)
    }

    private fun getJwtTokenHeader(ctx: Context): DecodedJWT? {
        val tokenHeader = ctx.header(headerTokenName)?.substringAfter("Bearer")?.trim()
            ?: return null

        return JwtProvider.decodeJWT(tokenHeader)
    }

    private fun getEmail(jwtToken: DecodedJWT?): String? {
        return jwtToken?.subject
    }

    private fun getUserRole(jwtToken: DecodedJWT?): RouteRole? {
        val adminUserRole = jwtToken?.getClaim("role")?.asString() ?: return null
        return Roles.valueOf(adminUserRole)
    }
}