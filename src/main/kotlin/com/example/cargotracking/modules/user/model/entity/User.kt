package com.example.cargotracking.modules.user.model.entity

import com.example.cargotracking.common.entity.BaseEntity
import com.example.cargotracking.modules.user.model.types.UserRole
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "users")
class User(
    id: UUID = UUID.randomUUID(),

    @Column(name = "username", unique = true, nullable = false, length = 100)
    val username: String,

    @Column(name = "email", unique = true, nullable = false, length = 255)
    val email: String,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,

    @Column(name = "full_name", nullable = false, length = 255)
    val fullName: String,

    @Column(name = "phone_number", length = 20)
    val phoneNumber: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    val role: UserRole,

    @Column(name = "address", length = 500)
    val address: String? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true

) : BaseEntity(id) {
    init {
        validate()
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", RegexOption.IGNORE_CASE)
        private val PHONE_REGEX = Regex("^[0-9+()\\s-]{8,20}$")
    }

    override fun validate() {
        require(username.isNotBlank() && username.length in 3..100) {
            "Username must be between 3 and 100 characters long and cannot be blank."
        }
        require(email.isNotBlank() && EMAIL_REGEX.matches(email)) {
            "Email must be a valid email address."
        }
        require(passwordHash.isNotBlank()) {
            "Password cannot be blank"
        }
        require(fullName.isNotBlank() && fullName.length in 2..255) {
            "Full name must be between 2 and 255 characters long and cannot be blank."
        }
        phoneNumber?.let {
            require(PHONE_REGEX.matches(it)) {
                "Invalid phone number format"
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "User(id=$id, username='$username', email='$email', role=$role)"
    }
}
