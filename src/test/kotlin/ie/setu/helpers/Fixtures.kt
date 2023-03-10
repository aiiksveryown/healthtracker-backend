package ie.setu.helpers

import ie.setu.config.Roles
import ie.setu.domain.*
import ie.setu.domain.db.*
import ie.setu.domain.repository.*
import ie.setu.utils.Cipher.encodePassword
import org.jetbrains.exposed.sql.SchemaUtils
import org.joda.time.DateTime

const val nonExistingId = Integer.MIN_VALUE
const val nonExistingEmail = "112233445566778testUser@xxxxx.xx"
const val validName = "Test User 1"
const val validEmail = "test_user1@test.com"
const val updatedName = "Updated Name"
const val updatedEmail = "test_user1_updated@test.com"

const val validActivityDescription = "Test Activity Description 1"
const val validActivityCalories = 100
val validActivityStarted: DateTime = DateTime.now()
const val validActivityDuration = 1.0
const val updatedActivityDescription = "Updated Activity Description"
val updatedActivityStarted: DateTime = DateTime.now()-1000
const val updatedActivityDuration = 2.0
const val updatedActivityCalories = 200

val adminAuthParams = AdminAuthParams(email = "admin@test.ie.setu", password = "admin")

val users = arrayListOf<User>(
    User(name = "Alice Wonderland", email = "alice@wonderland.com", id = 1),
    User(name = "Bob Cat", email = "bob@cat.ie", id = 2),
    User(name = "Mary Contrary", email = "mary@contrary.com", id = 3),
    User(name = "Carol Singer", email = "carol@singer.com", id = 4)
)

val activities = arrayListOf<Activity>(
    Activity(id = 1, description = "Running", duration = 22.0, calories = 230, started = DateTime.now(), userId = 1),
    Activity(id = 2, description = "Hopping", duration = 10.5, calories = 80, started = DateTime.now(), userId = 1),
    Activity(id = 3, description = "Walking", duration = 12.0, calories = 120, started = DateTime.now(), userId = 2)
)

val admins = arrayListOf<Admin>(
    Admin(email = "test_admin_manager@test.ie.setu", password = encodePassword("admin"), nickname = "manager", role = Roles.MANAGER, id = 1),
    Admin(email = "test_admin@test.ie.setu", password = encodePassword("password"), nickname = "admin", role = Roles.ADMIN, id = 2),
    Admin(email = "test_admin2@test.ie.setu", password = encodePassword("password"), nickname = "admin2", role = Roles.ADMIN, id = 3)
)

fun populateUserTable(): UserDAO {
    SchemaUtils.create(Users)
    val userDAO = UserDAO()
    userDAO.save(users[0])
    userDAO.save(users[1])
    userDAO.save(users[2])
    return userDAO
}
fun populateActivityTable(): ActivityDAO {
    SchemaUtils.create(Activities)
    val activityDAO = ActivityDAO()
    activityDAO.save(activities[0])
    activityDAO.save(activities[1])
    activityDAO.save(activities[2])
    return activityDAO
}

fun populateAdminTable(): AdminDAO {
    SchemaUtils.create(Admins)
    val adminDAO = AdminDAO()
    adminDAO.create(admins[0])
    adminDAO.create(admins[1])
    adminDAO.create(admins[2])
    return adminDAO
}