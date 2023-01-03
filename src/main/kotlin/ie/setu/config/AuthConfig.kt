package ie.setu.config

import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import ie.setu.utils.*
import io.javalin.core.security.RouteRole
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.ForbiddenResponse

object AuthConfig {
    fun manage(handler: Handler, ctx: Context, permittedRoles: Set<RouteRole>) {
        // Skip auth for swagger and public endpoints
        if (ctx.path().startsWith("/swagger") || Roles.UNAUTHENTICATED in permittedRoles) {
            handler.handle(ctx)
            return
        }

        val jwtToken : DecodedJWT?
        try {
            jwtToken = getJwtTokenHeader(ctx)
        }
        catch (e: JWTVerificationException) {
            throw ForbiddenResponse("Invalid token")
        }

        val userRole = getUserRole(jwtToken) ?: Roles.UNAUTHENTICATED
        if (userRole !in permittedRoles) {
            throw ForbiddenResponse()
        }
        ctx.attribute("email", getEmail(jwtToken))
        handler.handle(ctx)
    }
}