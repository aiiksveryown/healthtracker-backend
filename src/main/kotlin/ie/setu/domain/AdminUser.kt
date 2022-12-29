package ie.setu.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import ie.setu.config.Roles

import java.io.Serializable

data class AdminUserAuthParams(
    val email: String,
    val password: String
)

data class AdminUser (var id: Int,
                      var nickname: String,
                      var email: String,
                      var role: Roles,
                      var token: String? = null,
                      @JsonIgnore
                      var password: String) : Serializable // must be serializable to store in session file/db
