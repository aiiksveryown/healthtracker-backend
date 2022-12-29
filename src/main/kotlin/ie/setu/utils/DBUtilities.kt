package ie.setu.utils

import ie.setu.domain.Activity
import ie.setu.domain.User
import ie.setu.domain.db.Activities
import ie.setu.domain.db.Users
import ie.setu.domain.AdminUser
import ie.setu.domain.db.Admins
import org.jetbrains.exposed.sql.ResultRow

fun mapToUser(it: ResultRow) = User(
    id = it[Users.id],
    name = it[Users.name],
    email = it[Users.email]
)

fun mapToActivity(it: ResultRow) = Activity(
    id = it[Activities.id],
    description = it[Activities.description],
    duration = it[Activities.duration],
    started = it[Activities.started],
    calories = it[Activities.calories],
    userId = it[Activities.userId]
)

fun mapToAccount(it: ResultRow) = AdminUser(
    id = it[Admins.id],
    nickname = it[Admins.nickname],
    email = it[Admins.email],
    password = it[Admins.password],
    role = it[Admins.role]
)