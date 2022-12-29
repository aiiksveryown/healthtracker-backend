package ie.setu.controllers

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

object AdminController {
    private val adminDAO = AdminDAO()

    fun login(ctx: Context) {
        val adminUser : AdminAuthParams = jsonToObject(ctx.body())
        // validate input
        if (adminUser.email.isEmailValid() && adminUser.password.isNotEmpty() == true) {

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

    fun createAdmin(ctx: Context) {
        val admin : Admin = jsonToObject(ctx.body())
        // validate input
        if (!admin.email.isEmailValid() || admin.password.isNotEmpty() != true) {
            throw BadRequestResponse("Invalid email or password")
        }
        val userFound : Admin? = adminDAO.findByEmail(admin.email)
        if (userFound == null) {
            val userCreated : Int? = adminDAO.create(admin.copy(password = admin.password))
            if (userCreated != null) {
                ctx.json(admin.copy(id = userCreated, token = generateJwtToken(admin), password = ""))
            } else {
                throw HttpResponseException(500, "Error creating user")
            }
        } else {
            throw BadRequestResponse("User already exists")
        }
    }

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
            val userUpdated : Int? = adminDAO.update(admin)
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

    fun getAllAdmins(ctx: Context) {
        val usersFound : List<Admin> = adminDAO.getAll()
        if (usersFound.isNotEmpty()) {
            ctx.json(usersFound)
        } else {
            throw NotFoundResponse("No users found")
        }
    }

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