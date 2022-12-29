package ie.setu.domain.repository

import ie.setu.domain.db.Admins
import ie.setu.domain.Admin
import ie.setu.utils.mapToAccount
import ie.setu.config.Roles
import ie.setu.utils.Cipher.encodePassword

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class AdminDAO {
    // Prop to create the table if not exists and insert the first admin user
    init {
        transaction {
            SchemaUtils.create(Admins)
            // Sanitize db
            Admins.deleteWhere {
                Admins.email like "test_%"
            }
        }
    }

    fun create(admin: Admin) : Int {
        return transaction {
            Admins.insert {
                it[nickname] = admin.nickname
                it[password] = encodePassword(admin.password)
                it[email] = admin.email
                it[role] = admin.role
            } get Admins.id
        }
    }

    fun findById(userId: Int): Admin?{
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

    fun update(admin: Admin):Int{
        return transaction {
            Admins.update({Admins.id eq admin.id}){
                it[nickname] = admin.nickname
                if (admin.password.isNotEmpty()) {
                    it[password] = encodePassword(admin.password)
                }
                it[email] = admin.email
                it[role] = admin.role
            }
        }
    }

    fun getAll(): ArrayList<Admin> {
        val accountsList: ArrayList<Admin> = arrayListOf()
        transaction {
            Admins.selectAll().map {
                accountsList.add(mapToAccount(it)) }
        }
        return accountsList
    }

    fun findByEmail(email: String): Admin?{
        return transaction {
            Admins
                .select() { Admins.email eq email}
                .map{mapToAccount(it)}
                .firstOrNull()
        }
    }
}
