package ie.setu.domain.db

import ie.setu.config.Roles
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Admins : Table("admin_users") {
    val id: Column<Int> = integer("id").autoIncrement().primaryKey()
    val nickname: Column<String> = varchar("nickname", 50)
    val email: Column<String> = varchar("email", 50)
    val role: Column<Roles> = enumerationByName("role", 50, Roles::class)
    val password: Column<String> = varchar("password", 50)
}
