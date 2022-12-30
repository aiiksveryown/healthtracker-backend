package ie.setu.controllers

import ie.setu.config.DbConfig
import ie.setu.config.Roles
import ie.setu.domain.Admin
import ie.setu.domain.AdminAuthParams
import ie.setu.helpers.*
import ie.setu.utils.HttpUtil
import ie.setu.utils.jsonToObject

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class AdminControllerTest {

    val db = DbConfig().getDbConnection()
    private val app = ServerContainer.instance
    private val req = (HttpUtil(this.app.port())::AdminRequest)()

    private val testAdmin = Admin(email = "test_login@test.ie.setu", password = "password", role = Roles.ADMIN, nickname = "test", id = 1)
    private val managerAdminAuthParams = AdminAuthParams(email = "admin@test.ie.setu", password = "admin")

    @Nested
    inner class Login {
        @Test
        fun `invalid login without email returns 422`() {
            val adminUser = AdminAuthParams(email = "", password = "password")
            val response = req.adminLogin(adminUser)
            assert(response.status == 422)
        }

        @Test
        fun `invalid login with invalid password returns 422`() {
            val adminUser = AdminAuthParams(email = "test_login@test.ie.setu", password = "")
            val response = req.adminLogin(adminUser)
            assert(response.status == 422)
        }

        @Test
        fun `invalid login with invalid email returns 422`() {
            val adminUser = AdminAuthParams(email = "test_login", password = "password")
            val response = req.adminLogin(adminUser)
            assert(response.status == 422)
        }

        @Test
        fun `invalid login with invalid credentials returns 401`() {
            val adminUser = AdminAuthParams(email = "not_auth_admin@test.ie.setu", password = "password")
            val response = req.adminLogin(adminUser)
            assert(response.status == 401)
        }

        @Test
        fun `valid login returns 200`() {
            val adminManager = managerAdminAuthParams
            val response = req.adminLogin(adminManager)

            println(response.status)

            assert(response.status == 200)

            val adminResponse = jsonToObject<Admin>(response.body.toString())

            assert(adminResponse.email == adminManager.email)
            assert(adminResponse.token != null)
        }

        @Test
        fun `refresh user returns 200`() {
            val adminManager = managerAdminAuthParams
            val response = req.adminLogin(adminManager)

            val adminResponse = jsonToObject<Admin>(response.body.toString())

            val responseRefresh = req.refresh(adminResponse.token!!)

            assert(responseRefresh.status == 200)

            val adminRefreshResponse = jsonToObject<Admin>(responseRefresh.body.toString())
            assert(adminRefreshResponse.email == adminManager.email)
            assert(adminRefreshResponse.token != null)
        }
    }

    @Nested
    inner class ReadAdmins {
        @Test
        fun `get all admins returns 200`() {
            val login = req.adminLogin(managerAdminAuthParams)
            val token = jsonToObject<Admin>(login.body.toString()).token!!

            val testAdminCreate = req.createAdmin(testAdmin, token)
            assert(testAdminCreate.status == 200)
            val testAdminCreate2 = req.createAdmin(testAdmin.copy(email = "test_login2@test.ie.setu"), token)
            assert(testAdminCreate2.status == 200)

            val response = req.getAdmins(token)
            assert(response.status == 200)

            val admins = jsonToObject<List<Admin>>(response.body.toString())

            assert(admins.size >= 3)

            val testAdmin = admins.find { it.email == jsonToObject<Admin>(testAdminCreate.body.toString()).email }

            assert(testAdmin != null)

            val testAdmin2 = admins.find { it.email == "test_login2@test.ie.setu" }

            assert(testAdmin2 != null)

            val deleteAdmin = req.deleteAdmin(jsonToObject<Admin>(testAdminCreate.body.toString()).id, token)
            assert(deleteAdmin.status == 200)

            val deleteAdmin2 = req.deleteAdmin(jsonToObject<Admin>(testAdminCreate2.body.toString()).id, token)
            assert(deleteAdmin2.status == 200)
        }

        @Test
        fun `get admin with id returns 200`() {
            val login = req.adminLogin(managerAdminAuthParams)
            val token = jsonToObject<Admin>(login.body.toString()).token!!

            val testAdminCreate = req.createAdmin(testAdmin, token)

            assert(testAdminCreate.status == 200)

            try {
                val adminId = jsonToObject<Admin>(testAdminCreate.body.toString()).id
                val response = req.getAdminById(adminId, token)
                assert(response.status == 200)

                val adminResponse = jsonToObject<Admin>(response.body.toString())
                assert(adminResponse.email == testAdmin.email)
                assert(adminResponse.token == null)
            }
            catch (e: Exception) {
                println("Error: ${e.message}")
                req.deleteAdmin(jsonToObject<Admin>(testAdminCreate.body.toString()).id, token)
            }
            finally {
                req.deleteAdmin(jsonToObject<Admin>(testAdminCreate.body.toString()).id, token)
            }
        }

        @Test
        fun `get admin with invalid id returns 404`() {
            val login = req.adminLogin(managerAdminAuthParams)
            val token = jsonToObject<Admin>(login.body.toString()).token!!

            val response = req.getAdminById(999999999, token)

            assert(response.status == 404)
        }
    }

    @Nested
    inner class CreateAdmin {
        @Test
        fun `invalid create without email returns 422`() {
            val login = req.adminLogin(managerAdminAuthParams)
            val token = jsonToObject<Admin>(login.body.toString()).token!!

            val adminUser = testAdmin.copy(email = "")

            val response = req.createAdmin(adminUser, token)

            assert(response.status == 422)
        }

        @Test
        fun `invalid create with invalid password returns 422`() {
            val login = req.adminLogin(managerAdminAuthParams)
            val token = jsonToObject<Admin>(login.body.toString()).token!!

            val adminUser = testAdmin.copy(password = "")

            val response = req.createAdmin(adminUser, token)

            if (response.status == 200) {
                req.deleteAdmin(jsonToObject<Admin>(response.body.toString()).id, token)
            }

            assert(response.status == 422)
        }

        @Test
        fun `create when email already exists returns 422`() {
            val login = req.adminLogin(managerAdminAuthParams)
            val token = jsonToObject<Admin>(login.body.toString()).token!!

            val adminUser = testAdmin.copy(id = 2)

            val response = req.createAdmin(adminUser, token)
            assert(response.status == 200)

            val duplicateAdmin = req.createAdmin(adminUser, token)

            try {
                assert(duplicateAdmin.status == 422)
            }
            catch (e: Exception) {
                println("Error: ${e.message}")
                if (duplicateAdmin.status == 200) {
                    req.deleteAdmin(jsonToObject<Admin>(duplicateAdmin.body.toString()).id, token)
                }
            }
            finally {
                req.deleteAdmin(jsonToObject<Admin>(response.body.toString()).id, token)
            }
        }

        @Test
        fun `valid create returns 200`() {
            val login = req.adminLogin(managerAdminAuthParams)
            val token = jsonToObject<Admin>(login.body.toString()).token!!

            val response = req.createAdmin(testAdmin, token)

            assert(response.status == 200)

            try {
                assert(jsonToObject<Admin>(response.body.toString()).email == testAdmin.email)
                assert(jsonToObject<Admin>(response.body.toString()).token != null)
            }
            catch (e: Exception) {
                println("Error: ${e.message}")
                assert(false)
            }
            finally {
                req.deleteAdmin(jsonToObject<Admin>(response.body.toString()).id, token)
            }
        }
    }

    @Nested
    inner class UpdateAdmin {
        @Test
        fun `invalid update with invalid id returns 404`() {
            val login = req.adminLogin(managerAdminAuthParams)
            val token = jsonToObject<Admin>(login.body.toString()).token!!

            val response = req.updateAdmin(testAdmin, 999999999, token)

            assert(response.status == 404)
        }

        @Test
        fun `invalid update with invalid email returns 422`() {
            val login = req.adminLogin(managerAdminAuthParams)
            val token = jsonToObject<Admin>(login.body.toString()).token!!

            val testAdminCreate = req.createAdmin(testAdmin, token)
            assert(testAdminCreate.status == 200)

            try {
                val adminId = jsonToObject<Admin>(testAdminCreate.body.toString()).id
                val adminUser = jsonToObject<Admin>(testAdminCreate.body.toString()).copy(email = "")

                val response = req.updateAdmin(adminUser, adminId, token)
                assert(response.status == 422)
            }
            catch (e: Exception) {
                println("Error: ${e.message}")
                assert(false)
            }
            finally {
                req.deleteAdmin(jsonToObject<Admin>(testAdminCreate.body.toString()).id, token)
            }
        }

        @Test
        fun `valid update returns 200`() {
            val login = req.adminLogin(managerAdminAuthParams)
            val token = jsonToObject<Admin>(login.body.toString()).token!!

            val testAdminCreate = req.createAdmin(testAdmin, token)
            assert(testAdminCreate.status == 200)

            val testAdmin = jsonToObject<Admin>(testAdminCreate.body.toString())
            val testAdminId = testAdmin.id

            val updatedAdmin = testAdmin.copy(email = "test_login_update@test.ie.setu", password = "test_pw_update")

            val updateResponse = req.updateAdmin(updatedAdmin, testAdminId, token)

            try {
                assert(updateResponse.status == 200)

                val testLogin = req.adminLogin(AdminAuthParams(updatedAdmin.email, updatedAdmin.password))
                assert(testLogin.status == 200)
            }
            catch (e: Exception) {
                println("Error: ${e.message}")
                assert(false)
            }
            finally {
                req.deleteAdmin(testAdminId, token)
            }

        }
    }

    @Nested
    inner class DeleteAdmin {
        @Test
        fun `invalid delete with invalid id returns 422`() {
            val login = req.adminLogin(managerAdminAuthParams)
            val token = jsonToObject<Admin>(login.body.toString()).token!!

            val response = req.deleteAdmin(999999999, token)

            assert(response.status == 404)
        }

        @Test
        fun `valid delete returns 200`() {
            val login = req.adminLogin(managerAdminAuthParams)
            val token = jsonToObject<Admin>(login.body.toString()).token!!

            val testAdminResponse = req.createAdmin(testAdmin, token)

            if (testAdminResponse.status == 200) {
                val testAdminId = jsonToObject<Admin>(testAdminResponse.body.toString()).id

                val response = req.deleteAdmin(testAdminId, token)
                assert(response.status == 200)

                // Check if user is deleted
                val adminUser = req.getAdminById(testAdmin.id, token)
                assert(adminUser.status == 404)
            }
            else {
                println("Error creating admin user for deletion: "+ jsonToObject(testAdminResponse.body.toString()))
                assert(false)
            }
        }
    }
}