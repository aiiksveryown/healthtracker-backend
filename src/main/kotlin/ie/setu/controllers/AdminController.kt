package ie.setu.controllers

import ie.setu.domain.AdminUser
import ie.setu.ext.isEmailValid
import ie.setu.domain.AdminUserAuthParams
import ie.setu.domain.repository.AdminDAO
import ie.setu.utils.*
import ie.setu.utils.Cipher
import ie.setu.utils.JwtProvider

import java.util.*
import io.javalin.http.BadRequestResponse
import io.javalin.http.HttpResponseException
import io.javalin.http.NotFoundResponse
import io.javalin.http.UnauthorizedResponse
import io.javalin.http.Context

object AdminController {
    private val adminDAO = AdminDAO()
    private val base64Encoder = Base64.getEncoder()

    fun login(ctx: Context) {
        val adminUser : AdminUserAuthParams = jsonToObject(ctx.body())
        // validate input
        if (adminUser.email.isEmailValid() && adminUser.password?.isNotEmpty() == true) {
            val userFound : AdminUser? = adminDAO.findByEmail(adminUser.email)
            if (userFound?.password == String(base64Encoder.encode(Cipher.encrypt(adminUser.password)))) {
                ctx.json(userFound.copy(token = generateJwtToken(userFound)))
            } else {
                throw UnauthorizedResponse("Invalid credentials")
            }
        } else {
            throw BadRequestResponse("Invalid credentials")
        }
    }

    fun logout(ctx: Context) {
        ctx.json("Logged out")
    }

    fun createAdmin(ctx: Context) {
        val adminUser : AdminUser = jsonToObject(ctx.body())
        val userFound : AdminUser? = adminDAO.findByEmail(adminUser.email)
        if (userFound == null) {
            val userCreated : Int? = adminDAO.create(adminUser)
            if (userCreated != null) {
                ctx.json(adminUser.copy(id = userCreated))
            } else {
                throw HttpResponseException(500, "Error creating user")
            }
        } else {
            throw BadRequestResponse("User already exists")
        }
    }

    fun updateAdminById(ctx: Context) {
        val adminUser : AdminUser = jsonToObject(ctx.body())
        val adminUserId : Int = ctx.pathParam("id").toInt()
        val userFound = adminDAO.findById(adminUserId)
        if (userFound != null) {
            val userUpdated : Int? = adminDAO.update(adminUser)
            if (userUpdated != null) {
                ctx.json(userUpdated)
            } else {
                throw HttpResponseException(500, "Error updating user")
            }
        } else {
            throw NotFoundResponse("User not found")
        }
    }

    fun deleteAdminById(ctx: Context) {
        val adminUser : AdminUser = jsonToObject(ctx.body())
        val userFound : AdminUser? = adminDAO.findByEmail(adminUser.email)
        if (userFound != null) {
            val userDeleted : Int = adminDAO.deleteById(adminUser.id)
            if (userDeleted != 0) {
                ctx.json("User deleted")
            } else {
                throw HttpResponseException(500, "Error deleting user")
            }
        } else {
            throw NotFoundResponse("User not found")
        }
    }

    fun getAllAdmins(ctx: Context) {
        val usersFound : List<AdminUser> = adminDAO.getAll()
        if (usersFound.isNotEmpty()) {
            ctx.json(usersFound)
        } else {
            throw NotFoundResponse("No users found")
        }
    }

    fun getAdminById(ctx: Context) {
        val adminUserId : Int = ctx.pathParam("id").toInt()
        val userFound : AdminUser? = adminDAO.findById(adminUserId)
        if (userFound != null) {
            ctx.json(userFound)
        } else {
            throw NotFoundResponse("User not found")
        }
    }

    private fun generateJwtToken(adminUser: AdminUser): String? {
        return adminUser.role?.let { JwtProvider.createJWT(adminUser, it.name) }
    }
}