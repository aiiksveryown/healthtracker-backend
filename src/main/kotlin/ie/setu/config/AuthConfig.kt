package ie.setu.config

import ie.setu.utils.*
import io.javalin.core.security.RouteRole
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.ForbiddenResponse

object AuthConfig {
    fun manage(handler: Handler, ctx: Context, permittedRoles: Set<RouteRole>) {
        // Skip auth for swagger
        if (ctx.path().startsWith("/swagger")) {
            handler.handle(ctx)
            return
        }

        if (Roles.UNAUTHENTICATED in permittedRoles) {
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
}