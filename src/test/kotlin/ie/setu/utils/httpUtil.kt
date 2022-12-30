package ie.setu.utils

import ie.setu.domain.Admin
import ie.setu.domain.AdminAuthParams
import kong.unirest.HttpResponse
import kong.unirest.JsonNode
import kong.unirest.Unirest
import org.joda.time.DateTime

class HttpUtil(port: Int) {
    private val origin = "http://localhost:$port"

    inner class AdminRequest {
        fun createAdmin(admin: Admin, token: String): HttpResponse<JsonNode> {
            return Unirest.post("$origin/api/admins")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $token")
                .body(admin)
                .asJson()
        }

        fun adminLogin(adminUser: AdminAuthParams): HttpResponse<JsonNode> {
            return Unirest.post("$origin/api/admins/login")
                .header("Content-Type", "application/json")
                .body(adminUser)
                .asJson()
        }

        fun deleteAdmin(id: Int, token: String) : HttpResponse<JsonNode> {
            return Unirest.delete("$origin/api/admins/$id")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $token")
                .asJson()
        }

        fun getAdmins(token: String) : HttpResponse<JsonNode> {
            return Unirest.get("$origin/api/admins")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $token")
                .asJson()
        }

        fun getAdminById(id: Int, token: String) : HttpResponse<JsonNode> {
            return Unirest.get("$origin/api/admins/$id")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $token")
                .asJson()
        }

        fun updateAdmin(admin: Admin, id: Int, token: String) : HttpResponse<JsonNode> {
            return Unirest.patch("$origin/api/admins/$id")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $token")
                .body(admin)
                .asJson()
        }

        fun refresh(token: String) : HttpResponse<JsonNode> {
            return Unirest.get("$origin/api/admins/login/refresh")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $token")
                .asJson()
        }
    }

    inner class UserRequest {
        fun retrieveAllUsers(token: String): HttpResponse<JsonNode> {
            return Unirest.get("$origin/api/users/")
                .header("accept", "application/json")
                .header("Authorization", "Bearer $token")
                .asJson()
        }

        //helper function to add a test user to the database
        fun addUser (name: String, email: String ,token: String): HttpResponse<JsonNode> {
            return Unirest.post("$origin/api/users")
                .body("{\"name\":\"$name\", \"email\":\"$email\"}")
                .header("Authorization", "Bearer $token")
                .asJson()
        }

        //helper function to delete a test user from the database
        fun deleteUser (token: String, id: Int): HttpResponse<String> {
            return Unirest.delete("$origin/api/users/$id")
                .header("Authorization", "Bearer $token")
                .asString()
        }

        //helper function to retrieve a test user from the database by email
        fun retrieveUserByEmail(email : String, token: String) : HttpResponse<String> {
            return Unirest.get(origin + "/api/users/email/${email}")
                .header("Authorization", "Bearer $token")
                .asString()
        }

        //helper function to retrieve a test user from the database by id
        fun retrieveUserById(id: Int, token: String) : HttpResponse<String> {
            return Unirest.get(origin + "/api/users/${id}")
                .header("Authorization", "Bearer $token")
                .asString()
        }

        //helper function to add a test user to the database
        fun updateUser (token: String, id: Int, name: String, email: String): HttpResponse<JsonNode> {
            return Unirest.patch("$origin/api/users/$id")
                .body("{\"name\":\"$name\", \"email\":\"$email\"}")
                .header("Authorization", "Bearer $token")
                .asJson()
        }

    }

    inner class ActivityRequest {
        //helper function to add a test activity to the database
        fun addActivity (token : String, userId: Int, description: String, duration: Double, calories: Int, started: DateTime): HttpResponse<JsonNode> {
            return Unirest.post("$origin/api/activities")
                .body("{\"userId\":\"$userId\", \"description\":\"$description\", \"duration\":\"$duration\", \"calories\":\"$calories\", \"started\":\"$started\"}")
                .header("Authorization", "Bearer $token")
                .asJson()
        }

        //helper function to retrieve a test activity from the database by id
        fun retrieveActivityById(token: String, activityId: Int) : HttpResponse<String> {
            return Unirest.get(origin + "/api/activities/${activityId}")
                .header("Authorization", "Bearer $token")
                .asString()
        }

        //helper function to retrieve all activities for a user from the database by user id
        fun retrieveActivitiesByUserId(token: String, userId: Int) : HttpResponse<String> {
            return Unirest.get(origin + "/api/users/${userId}/activities")
                .header("Authorization", "Bearer $token")
                .asString() // ###
        }

        //helper function to retrieve all activities from the database
        fun retrieveAllActivities(token: String) : HttpResponse<String> {
            return Unirest.get("$origin/api/activities")
                .header("Authorization", "Bearer $token")
                .asString()
        }

        //helper function to update a test activity in the database
        fun updateActivity (token: String, userId: Int, activityId: Int, description: String, duration: Double, calories: Int, started: DateTime): HttpResponse<JsonNode> {
            return Unirest.patch("$origin/api/activities/$activityId")
                .body("{\"userId\":\"$userId\", \"description\":\"$description\", \"duration\":\"$duration\", \"calories\":\"$calories\", \"started\":\"$started\"}")
                .header("Authorization", "Bearer $token")
                .asJson()
        }

        //helper function to delete a test activity from the database by activity id
        fun deleteActivityByActivityId (token: String, activityId: Int): HttpResponse<String> {
            return Unirest.delete("$origin/api/activities/$activityId")
                .header("Authorization", "Bearer $token")
                .asString()
        }

        //helper function to delete activities from the database by user id
        fun deleteActivitiesByUserId (token: String, userId: Int): HttpResponse<String> {
            return Unirest.delete("$origin/api/users/$userId/activities")
                .header("Authorization", "Bearer $token")
                .asString()
        }
    }
}