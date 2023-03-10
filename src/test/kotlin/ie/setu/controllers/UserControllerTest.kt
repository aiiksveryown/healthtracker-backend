package ie.setu.controllers

import ie.setu.config.DbConfig
import ie.setu.domain.AdminAuthParams
import ie.setu.domain.User
import ie.setu.helpers.*
import ie.setu.utils.HttpUtil
import ie.setu.utils.jsonToObject

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class UserControllerTest {
    val db = DbConfig().getDbConnection()

    private val app = ServerContainer.instance

    private val httpUtil = HttpUtil(app.port())
    private val req = httpUtil.UserRequest()
    private val managerAdminAuthParams = AdminAuthParams(email = "admin@test.ie.setu", password = "admin")

    private var token = httpUtil.AdminRequest().adminLogin(managerAdminAuthParams).body.getObject().getString("token")

    @Nested
    inner class ReadUsers {
        @Test
        fun `get all users from the database returns 200 or 404 response`() {
            val response = req.retrieveAllUsers(token)
            if (response.status == 200) {
                val retrievedUsers: ArrayList<User> = jsonToObject(response.body.toString())
                assertNotEquals(0, retrievedUsers.size)
            }
            else {
                assertEquals(404, response.status)
            }
        }

        @Test
        fun `get user by id when user does not exist returns 404 response`() {

            //Arrange - test data for user id
            val id = nonExistingId

            // Act - attempt to retrieve the non-existent user from the database
            val  retrieveResponse = req.retrieveUserById(id, token)

            // Assert -  verify return code
            assertEquals(404, retrieveResponse.status)
        }

        @Test
        fun `get user by email when user does not exist returns 404 response`() {
            // Arrange & Act - attempt to retrieve the non-existent user from the database
            val retrieveResponse = req.retrieveUserByEmail(nonExistingEmail, token)
            // Assert -  verify return code
            assertEquals(404, retrieveResponse.status)
        }

        @Test
        fun `getting a user by id when id exists, returns a 200 response`() {

            //Arrange - add the user
            val addResponse = req.addUser(validName, validEmail, token)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Assert - retrieve the added user from the database and verify return code
            val retrieveResponse = req.retrieveUserById(addedUser.id, token)
            assertEquals(200, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            req.deleteUser(token, addedUser.id)
        }

        @Test
        fun `getting a user by email when email exists, returns a 200 response`() {

            //Arrange - add the user
            req.addUser(validName, validEmail, token)

            //Assert - retrieve the added user from the database and verify return code
            val retrieveResponse = req.retrieveUserByEmail(validEmail, token)
            assertEquals(200, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            val retrievedUser : User = jsonToObject(retrieveResponse.body.toString())
            req.deleteUser(token, retrievedUser.id)
        }
    }
    @Nested
    inner class CreateUsers {
        @Test
        fun `add a user with correct details returns a 201 response`() {

            //Arrange & Act & Assert
            //    add the user and verify return code (using fixture data)
            val addResponse = req.addUser(validName, validEmail, token)
            assertEquals(201, addResponse.status)

            //Assert - retrieve the added user from the database and verify return code
            val retrieveResponse= req.retrieveUserByEmail(validEmail, token)
            assertEquals(200, retrieveResponse.status)

            //Assert - verify the contents of the retrieved user
            val retrievedUser : User = jsonToObject(addResponse.body.toString())
            assertEquals(validEmail, retrievedUser.email)
            assertEquals(validName, retrievedUser.name)

            //After - restore the db to previous state by deleting the added user
            val deleteResponse = req.deleteUser(token, retrievedUser.id)
            assertEquals(204, deleteResponse.status)
        }
    }

    @Nested
    inner class UpdateUsers {
        @Test
        fun `updating a user when it exists, returns a 204 response`() {

            //Arrange - add the user that we plan to do an update on
            val addedResponse = req.addUser(validName, validEmail, token)
            val addedUser : User = jsonToObject(addedResponse.body.toString())

            //Act & Assert - update the email and name of the retrieved user and assert 204 is returned
            assertEquals(204, req.updateUser(token, addedUser.id, updatedName, updatedEmail).status)

            //Act & Assert - retrieve updated user and assert details are correct
            val updatedUserResponse = req.retrieveUserById(addedUser.id, token)
            val updatedUser : User = jsonToObject(updatedUserResponse.body.toString())
            assertEquals(updatedName, updatedUser.name)
            assertEquals(updatedEmail, updatedUser.email)

            //After - restore the db to previous state by deleting the added user
            req.deleteUser(token, addedUser.id)
        }

        @Test
        fun `updating a user when it doesn't exist, returns a 404 response`() {

            //Act & Assert - attempt to update the email and name of user that doesn't exist
            assertEquals(404, req.updateUser(token, -1, updatedName, updatedEmail).status)
        }
    }

    @Nested
    inner class DeleteUsers {
        @Test
        fun `deleting a user when it doesn't exist, returns a 404 response`() {
            //Act & Assert - attempt to delete a user that doesn't exist
            assertEquals(404, req.deleteUser(token, -1).status)
        }

        @Test
        fun `deleting a user when it exists, returns a 204 response`() {

            //Arrange - add the user that we plan to delete
            val addedResponse = req.addUser(validName, validEmail, token)
            val addedUser : User = jsonToObject(addedResponse.body.toString())

            //Act & Assert - delete the added user and assert a 204 is returned
            assertEquals(204, req.deleteUser(token, addedUser.id).status)

            //Act & Assert - attempt to retrieve the deleted user --> 404 response
            assertEquals(404, req.retrieveUserById(addedUser.id, token).status)
        }
    }
}
