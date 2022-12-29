package ie.setu.config

import io.javalin.core.security.RouteRole

enum class Roles : RouteRole {
    UNAUTHENTICATED,
    ADMIN,
    MANAGER,
}