package ie.setu.config

import ie.setu.controllers.*
import ie.setu.utils.jsonObjectMapper
import ie.setu.config.Roles.*
import ie.setu.utils.ErrorExceptionMapping

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.plugin.json.JavalinJackson
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.swagger.v3.oas.models.info.Info
import okhttp3.internal.addHeaderLenient

class JavalinConfig {
    fun startJavalinService(): Javalin {
        val app = Javalin.create {
            it.registerPlugin(getConfiguredOpenApiPlugin())
            it.defaultContentType = "application/json"
            //added this jsonMapper for our integration tests - serialise objects to json
            it.jsonMapper(JavalinJackson(jsonObjectMapper()))
            it.accessManager(AuthConfig::manage)
            it.enableWebjars()
            it.enableCorsForAllOrigins()
        }.apply {
            exception(Exception::class.java) { e, _ -> e.printStackTrace() }
            error(404) { ctx -> ctx.json("404 - Not Found") }
        }.start(getRemoteAssignedPort())

        ErrorExceptionMapping.register(app)
        registerRoutes(app)
        return app
    }

    private fun getRemoteAssignedPort(): Int {
        val remotePort = System.getenv("PORT")
        return if (remotePort != null) {
            Integer.parseInt(remotePort)
        } else 7001
    }

    private fun registerRoutes(app: Javalin) {
        app.routes {
            path("/api/users") {
                get(UserController::getAllUsers, ADMIN, MANAGER)
                post(UserController::addUser, ADMIN, MANAGER)
                path("{user-id}"){
                    get(UserController::getUserByUserId, ADMIN, MANAGER)
                    patch(UserController::updateUser, ADMIN, MANAGER)
                    delete(UserController::deleteUser, ADMIN, MANAGER)
                    path("activities"){
                        get(ActivityController::getActivitiesByUserId, ADMIN, MANAGER)
                        delete(ActivityController::deleteActivitiesByUserId, ADMIN, MANAGER)
                    }
                }
                path("email/{email}"){
                    get(UserController::getUserByEmail, ADMIN, MANAGER)
                }
            }
            path("/api/activities") {
                get(ActivityController::getAllActivities, ADMIN, MANAGER)
                post(ActivityController::addActivity, ADMIN, MANAGER)
                path("{activity-id}") {
                    get(ActivityController::getActivitiesByActivityId, ADMIN, MANAGER)
                    delete(ActivityController::deleteActivityByActivityId, ADMIN, MANAGER)
                    patch(ActivityController::updateActivity, ADMIN, MANAGER)
                }
            }
            path("/api/admins") {
                get(AdminController::getAllAdmins, MANAGER)
                post(AdminController::createAdmin, MANAGER)
                path("{admin-id}") {
                    get(AdminController::getAdminById, MANAGER)
                    patch(AdminController::updateAdminById, MANAGER)
                    delete(AdminController::deleteAdminById, MANAGER)
                }
                path("login") {
                    post(AdminController::login, UNAUTHENTICATED)
                    path("/refresh") {
                        get(AdminController::adminRefresh, ADMIN, MANAGER)
                    }
                }
            }

        }
    }

    private fun getConfiguredOpenApiPlugin() = OpenApiPlugin(
        OpenApiOptions(
            Info().version("1.0").description("API for the health-tracker app").title("Health Tracker API")
        ).apply {
            ignorePath("/swagger-ui")
            activateAnnotationScanningFor("ie.setu.controllers")
            path("/swagger-docs")// endpoint for OpenAPI json
            swagger(SwaggerOptions("/swagger-ui")) // endpoint for swagger-ui
            reDoc(ReDocOptions("/redoc")) // endpoint for redoc
        }
    )
}