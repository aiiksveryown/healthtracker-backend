package ie.setu.domain

import ie.setu.config.Roles

import java.io.Serializable

data class AdminAuthParams(
    val email: String,
    val password: String
)

data class Admin (var id: Int,
                  var nickname: String,
                  var email: String,
                  var role: Roles,
                  var token: String? = null,
//                      @JsonIgnore
                  var password: String) : Serializable // must be serializable to store in session file/db
