package ie.setu.controllers

import ie.setu.config.DbConfig
import ie.setu.domain.Activity
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
open class ActivityControllerTest {
    val db = DbConfig().getDbConnection()

    private val app = ServerContainer.instance

    private val httpUtil = HttpUtil(app.port())
    private val req = httpUtil.ActivityRequest()
    private val managerAdminAuthParams = AdminAuthParams(email = "admin@test.ie.setu", password = "admin")

    private var token = httpUtil.AdminRequest().adminLogin(managerAdminAuthParams).body.getObject().getString("token")

    @Nested
    inner class CreateActivities {
        //   post(  "/api/activities", HealthTrackerController::addActivity)
        @Test
        fun `add an activity with correct details returns a 201 response`() {

            //Arrange - add the user that we plan to do an add activity on
            val addedResponse = httpUtil.UserRequest().addUser(validName, validEmail, token)
            val addedUser : User = jsonToObject(addedResponse.body.toString())

            //Act & Assert - add the activity and verify return code (using fixture data)
            val addResponse = req.addActivity(token, addedUser.id, validActivityDescription, validActivityDuration, validActivityCalories, validActivityStarted )
            val addedActivity : Activity = jsonToObject(addResponse.body.toString())

            assertEquals(201, addResponse.status)

            //Assert - retrieve the added activity from the database and verify return code
            val retrieveResponse= req.retrieveActivityById(token, addedActivity.id)

            assertEquals(200, retrieveResponse.status)

            //Assert - verify the contents of the retrieved activity
            val retrievedActivity : Activity = jsonToObject(addResponse.body.toString())
            assertEquals(validActivityDescription, retrievedActivity.description)
            assertEquals(validActivityCalories, retrievedActivity.calories)
            assertEquals(validActivityDuration, retrievedActivity.duration)
            assertEquals(addedUser.id, retrievedActivity.userId)

            //After - restore the db to previous state by deleting the added user
            val deleteResponse = httpUtil.UserRequest().deleteUser(token, addedUser.id)
            assertEquals(204, deleteResponse.status)
        }
    }

    @Nested
    inner class ReadActivities {
        //   get(   "/api/users/:user-id/activities", HealthTrackerController::getActivitiesByUserId)
        @Test
        fun `getting activities by user id when user id exists, returns a 200 response`() {

            //Arrange - add the user
            val addResponse = httpUtil.UserRequest().addUser(validName, validEmail, token)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Act & Assert - add the activity and verify return code (using fixture data)
            req.addActivity(token, addedUser.id, validActivityDescription, validActivityDuration, validActivityCalories, validActivityStarted )

            //Assert - retrieve the added user's activities from the database and verify return code
            val retrieveResponse= req.retrieveActivitiesByUserId(token, addedUser.id)

            assertEquals(200, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            httpUtil.UserRequest().deleteUser(token, addedUser.id)
        }

        @Test
        fun `getting activities by user id when user id doesn't exist, returns a 404 response`() {

            //Assert - retrieve the added user's activities from the database and verify return code
            val retrieveResponse= req.retrieveActivitiesByUserId(token, nonExistingId)

            assertEquals(404, retrieveResponse.status)
        }

        @Test
        fun `getting activities by user id when there are no activities for the user, returns a 204 response`() {

            //Arrange - add the user
            val addResponse = httpUtil.UserRequest().addUser(validName, validEmail, token)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Assert - retrieve the added user's activities from the database and verify return code
            val retrieveResponse= req.retrieveActivitiesByUserId(token, addedUser.id)

            assertEquals(204, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            httpUtil.UserRequest().deleteUser(token, addedUser.id)
        }

        //   get(   "/api/activities", HealthTrackerController::getAllActivities)
        @Test
        fun `getting all activities when there are activities, returns a 200 response`() {

            //Arrange - add the user
            val addResponse = httpUtil.UserRequest().addUser(validName, validEmail, token)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Act & Assert - add the activity and verify return code (using fixture data)
            req.addActivity(token, addedUser.id, validActivityDescription, validActivityDuration, validActivityCalories, validActivityStarted )

            //Assert - retrieve the added user's activities from the database and verify return code
            val retrieveResponse= req.retrieveAllActivities(token)

            assertEquals(200, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            httpUtil.UserRequest().deleteUser(token, addedUser.id)
        }

        //   get(   "/api/activities/:activity-id", HealthTrackerController::getActivitiesByActivityId)
        @Test
        fun `getting activities by activity id when activity id exists, returns a 200 response`() {
            //Arrange - add the user
            val addResponse = httpUtil.UserRequest().addUser(validName, validEmail, token)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Act & Assert - add the activity and verify return code (using fixture data)
            val addActivityResponse = req.addActivity(token, addedUser.id, validActivityDescription, validActivityDuration, validActivityCalories, validActivityStarted )
            val addedActivity : Activity = jsonToObject(addActivityResponse.body.toString())

            //Assert - retrieve the added user's activities from the database and verify return code
            val retrieveResponse= req.retrieveActivityById(token, addedActivity.id)

            assertEquals(200, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            httpUtil.UserRequest().deleteUser(token, addedUser.id)
        }

        @Test
        fun `getting activities by activity id when activity id doesn't exist, returns a 404 response`() {
            //Assert - retrieve the added user's activities from the database and verify return code
            val retrieveResponse= req.retrieveActivityById(token, nonExistingId)

            assertEquals(404, retrieveResponse.status)
        }
    }

    @Nested
    inner class UpdateActivities {
        //  patch( "/api/activities/:activity-id", HealthTrackerController::updateActivity)
        @Test
        fun `updating an activity with correct details returns a 204 response`() {
            //Arrange - add the user
            val addResponse = httpUtil.UserRequest().addUser(validName, validEmail, token)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Act & Assert - add the activity and verify return code (using fixture data)
            val addActivityResponse = req.addActivity(token, addedUser.id, validActivityDescription, validActivityDuration, validActivityCalories, validActivityStarted )
            val addedActivity : Activity = jsonToObject(addActivityResponse.body.toString())

            //Act - update the activity
            val updateResponse = req.updateActivity(token, addedUser.id, addedActivity.id, updatedActivityDescription, updatedActivityDuration, updatedActivityCalories, validActivityStarted )

            assertEquals(204, updateResponse.status)

            //After - restore the db to previous state by deleting the added user
            httpUtil.UserRequest().deleteUser(token, addedUser.id)
        }

        @Test
        fun `updating an activity that doesn't exist returns a 404 response`() {

            //Act - update the activity
            val updateResponse = req.updateActivity(token, nonExistingId, nonExistingId, updatedActivityDescription, updatedActivityDuration, updatedActivityCalories, updatedActivityStarted )

            assertEquals(404, updateResponse.status)
        }
    }

    @Nested
    inner class DeleteActivities {
        //   delete("/api/activities/:activity-id", HealthTrackerController::deleteActivityByActivityId)
        @Test
        fun `deleting an activity with correct details returns a 204 response`() {
            //Arrange - add the user
            val addResponse = httpUtil.UserRequest().addUser(validName, validEmail, token)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Act & Assert - add the activity and verify return code (using fixture data)
            val addActivityResponse = req.addActivity(token, addedUser.id, validActivityDescription, validActivityDuration, validActivityCalories, validActivityStarted )
            val addedActivity : Activity = jsonToObject(addActivityResponse.body.toString())

            //Act - delete the activity
            val deleteResponse = req.deleteActivityByActivityId(token, addedActivity.id)

            assertEquals(204, deleteResponse.status)

            //After - restore the db to previous state by deleting the added user
            httpUtil.UserRequest().deleteUser(token, addedUser.id)
        }

        //   delete("/api/users/:user-id/activities", HealthTrackerController::deleteActivityByUserId)
        @Test
        fun `deleting all activities for a user with correct details returns a 204 response`() {
            //Arrange - add the user
            val addResponse = httpUtil.UserRequest().addUser(validName, validEmail, token)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Act & Assert - add the activity
            req.addActivity(token, addedUser.id, validActivityDescription, validActivityDuration, validActivityCalories, validActivityStarted )

            //Act - delete the activity
            val deleteResponse = req.deleteActivitiesByUserId(token, addedUser.id)

            // assert
            assertEquals(204, deleteResponse.status)

            //After - restore the db to previous state by deleting the added user
            httpUtil.UserRequest().deleteUser(token, addedUser.id)
        }
    }
}
