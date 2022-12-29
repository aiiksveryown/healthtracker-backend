package ie.setu.domain.repository

import ie.setu.domain.db.Admins
import ie.setu.domain.AdminUser
import ie.setu.utils.mapToAccount
import ie.setu.config.Roles

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class AdminDAO {
    // Prop to create the table if not exists and insert the first admin user
    init {
        transaction {
            SchemaUtils.create(Admins)
            println("AdminUsers table created")
            if (Admins.selectAll().count() == 0) {
                Admins.insert {
                    it[nickname] = "admin"
                    it[email] = "admin@test.ie.setu"
                    it[password] = "DP+aA52RcR8bXc74yfl2jUYOR383/BoQaLbxg6YuaXU="
                    it[role] = Roles.ADMIN
                }
            }
        }
    }

    fun create(admin: AdminUser) : Int {
        return transaction {
            Admins.insert {
                it[nickname] = admin.nickname
                it[password] = admin.password
                it[email] = admin.email
                it[role] = admin.role
            } get Admins.id
        }
    }

    fun findById(userId: Int): AdminUser?{
        return transaction {
            Admins
                .select() { Admins.id eq userId}
                .map{mapToAccount(it)}
                .firstOrNull()
        }
    }

    fun deleteById(userId: Int):Int{
        return transaction {
            Admins.deleteWhere { Admins.id eq userId }
        }
    }

    fun update(admin: AdminUser):Int{
        return transaction {
            Admins.update({Admins.id eq admin.id}){
                it[nickname] = admin.nickname
                it[password] = admin.password
                it[email] = admin.email
                it[role] = admin.role
            }
        }
    }

    fun getAll(): ArrayList<AdminUser> {
        val accountsList: ArrayList<AdminUser> = arrayListOf()
        transaction {
            Admins.selectAll().map {
                accountsList.add(mapToAccount(it)) }
        }
        return accountsList
    }

    fun findByEmail(email: String): AdminUser?{
        return transaction {
            Admins
                .select() { Admins.email eq email}
                .map{mapToAccount(it)}
                .firstOrNull()
        }
    }
}
