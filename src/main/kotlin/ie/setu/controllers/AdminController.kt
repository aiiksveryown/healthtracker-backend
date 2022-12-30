package ie.setu.controllers

import ie.setu.domain.Activity
import ie.setu.domain.Admin
import ie.setu.ext.isEmailValid
import ie.setu.domain.AdminAuthParams
import ie.setu.domain.repository.AdminDAO
import ie.setu.utils.*
import ie.setu.utils.Cipher.encodePassword
import ie.setu.utils.JwtProvider

import java.util.*
import io.javalin.http.BadRequestResponse
import io.javalin.http.HttpResponseException
import io.javalin.http.NotFoundResponse
import io.javalin.http.UnauthorizedResponse
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*

object AdminController {
    private val adminDAO = AdminDAO()

    @OpenApi(
        summary = "Get auth token",
        operationId = "login",
        tags = ["Admin"],
        path = "/api/admins/login",
        requestBody = OpenApiRequestBody([OpenApiContent(AdminAuthParams::class)]),
        method = HttpMethod.POST,
        responses = [OpenApiResponse("200", [OpenApiContent(Array<Admin>::class)])]
    )
    fun login(ctx: Context) {
        val adminUser : AdminAuthParams = jsonToObject(ctx.body())
        // validate input
        if (adminUser.email.isEmailValid() && adminUser.password.isNotEmpty()) {

            val userFound : Admin? = adminDAO.findByEmail(adminUser.email)
            if (userFound?.password == encodePassword(adminUser.password)) {
                ctx.json(userFound.copy(token = generateJwtToken(userFound), password = ""))
            } else {
                throw UnauthorizedResponse("Invalid credentials")
            }
        } else {
            throw BadRequestResponse("Invalid credentials")
        }
    }

    @OpenApi(
        summary = "Create new admin",
        operationId = "createAdmin",
        tags = ["Admin"],
        path = "/api/admins",
        requestBody = OpenApiRequestBody([OpenApiContent(Admin::class)]),
        method = HttpMethod.POST,
        responses = [OpenApiResponse("200", [OpenApiContent(Array<Admin>::class)])]
    )
    fun createAdmin(ctx: Context) {
        val admin : Admin = jsonToObject(ctx.body())
        // validate input
        if (!admin.email.isEmailValid() || !admin.password.isNotEmpty()) {
            throw BadRequestResponse("Invalid email or password")
        }
        val userFound : Admin? = adminDAO.findByEmail(admin.email)
        if (userFound == null) {
            val userCreated : Int = adminDAO.create(admin.copy(password = admin.password))
            ctx.json(admin.copy(id = userCreated, token = generateJwtToken(admin), password = ""))
        } else {
            throw BadRequestResponse("User already exists")
        }
    }

    @OpenApi(
        summary = "Update admin by ID",
        operationId = "updateAdminById",
        tags = ["Admin"],
        path = "/api/admins/{admin-id}",
        headers = [
            OpenApiParam(name = "Authorization", description = "Bearer token", required = true, type = String::class)
        ],
        method = HttpMethod.PATCH,
        pathParams = [OpenApiParam("admin-id", Int::class, "The admin ID")],
        requestBody = OpenApiRequestBody([OpenApiContent(Admin::class)]),
        responses  = [OpenApiResponse("200", [OpenApiContent(Int::class)])]
    )
    fun updateAdminById(ctx: Context) {
        val admin : Admin = jsonToObject(ctx.body())
        val adminUserId : Int = ctx.pathParam("admin-id").toInt()
        // validate input
        if (!admin.email.isEmailValid()) {
            throw BadRequestResponse("Invalid email or password")
        }

        val userFound = adminDAO.findById(adminUserId)
        println(userFound)
        println(adminUserId)
        if (userFound != null) {
            val userUpdated : Int = adminDAO.update(admin)
            ctx.json(userUpdated)
        } else {
            throw NotFoundResponse("User not found")
        }
    }

    @OpenApi(
        summary = "Delete admin by ID",
        operationId = "deleteAdminById",
        tags = ["Admin"],
        path = "/api/admins/{admin-id}",
        headers = [
            OpenApiParam(name = "Authorization", description = "Bearer token", required = true, type = String::class)
        ],
        method = HttpMethod.DELETE,
        pathParams = [OpenApiParam("admin-id", Int::class, "The admin ID")],
        responses  = [OpenApiResponse("200", [OpenApiContent(String::class)])]
    )
    fun deleteAdminById(ctx: Context) {
        val adminUserId : Int = ctx.pathParam("admin-id").toInt()
        val userFound : Admin? = adminDAO.findById(adminUserId)
        if (userFound != null) {
            val userDeleted : Int = adminDAO.deleteById(adminUserId)
            if (userDeleted != 0) {
                ctx.json("User deleted")
            } else {
                throw HttpResponseException(500, "Error deleting user")
            }
        } else {
            throw NotFoundResponse("User not found")
        }
    }

    @OpenApi(
        summary = "Get all admins",
        operationId = "getAllAdmins",
        tags = ["Admin"],
        path = "/api/admins",
        headers = [
            OpenApiParam(name = "Authorization", description = "Bearer token", required = true, type = String::class)
        ],
        method = HttpMethod.GET,
        responses = [OpenApiResponse("200", [OpenApiContent(Array<Admin>::class)])]
    )
    fun getAllAdmins(ctx: Context) {
        val usersFound : List<Admin> = adminDAO.getAll()
        if (usersFound.isNotEmpty()) {
            ctx.json(usersFound)
        } else {
            throw NotFoundResponse("No users found")
        }
    }

    @OpenApi(
        summary = "Get admin by ID",
        operationId = "getAdminById",
        tags = ["Admin"],
        path = "/api/admins/{admin-id}",
        headers = [
            OpenApiParam(name = "Authorization", description = "Bearer token", required = true, type = String::class)
        ],
        method = HttpMethod.GET,
        pathParams = [OpenApiParam("admin-id", Int::class, "The admin ID")],
        responses  = [OpenApiResponse("200", [OpenApiContent(Admin::class)])]
    )
    fun getAdminById(ctx: Context) {
        val adminUserId : Int = ctx.pathParam("admin-id").toInt()
        val userFound : Admin? = adminDAO.findById(adminUserId)
        if (userFound != null) {
            ctx.json(userFound)
        } else {
            throw NotFoundResponse("User not found")
        }
    }


    private fun generateJwtToken(admin: Admin): String? {
        return admin.role.let { JwtProvider.createJWT(admin, it.name) }
    }
}