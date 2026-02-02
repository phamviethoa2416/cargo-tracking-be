package com.example.cargotracking.modules.user.model.entity

import com.example.cargotracking.common.entity.BaseEntity
import com.example.cargotracking.modules.user.model.types.UserRole
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_user_username", columnList = "username"),
        Index(name = "idx_user_email", columnList = "email"),
        Index(name = "idx_user_role", columnList = "role"),
        Index(name = "idx_user_is_active", columnList = "is_active")
    ]
)
class User private constructor (
    id: UUID,

    @Column(name = "username", unique = true, nullable = false, length = 100)
    var username: String,

    @Column(name = "email", unique = true, nullable = false, length = 255)
    var email: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Column(name = "full_name", nullable = false, length = 255)
    var fullName: String,

    @Column(name = "phone_number", length = 20)
    var phoneNumber: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: UserRole,

    @Column(name = "address", length = 500)
    var address: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

) : BaseEntity(id) {
    protected constructor() : this(
        id = UUID(0, 0),
        username = "",
        email = "",
        passwordHash = "",
        fullName = "",
        role = UserRole.CUSTOMER
    )

    override fun validateInvariants() {
        check(username.isNotBlank() && username.length in 3..100) {
            "Username must be 3-100 characters, got: '$username'"
        }

        check(email.isNotBlank() && EMAIL_REGEX.matches(email)) {
            "Invalid email format: '$email'"
        }

        check(passwordHash.isNotBlank()) {
            "Password hash must not be blank"
        }

        check(fullName.isNotBlank() && fullName.length in 2..255) {
            "Full name must be 2-255 characters, got: '$fullName'"
        }

        phoneNumber?.let { phone ->
            check(PHONE_REGEX.matches(phone)) {
                "Invalid phone number format: '$phone'"
            }
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", RegexOption.IGNORE_CASE)
        private val PHONE_REGEX = Regex("^[0-9+()\\s-]{8,20}$")

        fun create(
            username: String,
            email: String,
            passwordHash: String,
            fullName: String,
            role: UserRole,
            phoneNumber: String? = null,
            address: String? = null
        ): User {
            require(username.isNotBlank()) {
                "Username must not be blank"
            }
            require(email.isNotBlank()) {
                "Email must not be blank"
            }
            require(passwordHash.isNotBlank()) {
                "Password hash must not be blank"
            }
            require(fullName.isNotBlank()) {
                "Full name must not be blank"
            }

            val user = User(
                id = UUID.randomUUID(),
                username = username.trim(),
                email = email.trim().lowercase(),
                passwordHash = passwordHash,
                fullName = fullName.trim(),
                phoneNumber = phoneNumber?.trim(),
                role = role,
                address = address?.trim(),
                isActive = true
            )
            return user
        }
    }

    fun updateEmail(newEmail: String) {
        require(newEmail.isNotBlank()) {
            "Email must not be blank"
        }
        require(EMAIL_REGEX.matches(newEmail)) {
            "Invalid email format: '$newEmail'"
        }

        email = newEmail.trim().lowercase()
    }

    fun updatePassword(newPasswordHash: String) {
        require(newPasswordHash.isNotBlank()) {
            "Password hash must not be blank"
        }

        passwordHash = newPasswordHash
    }

    fun updateFullName(newFullName: String) {
        require(newFullName.isNotBlank() && newFullName.length in 2..255) {
            "Full name must be 2-255 characters long"
        }

        fullName = newFullName.trim()
    }

    fun updatePhoneNumber(newPhoneNumber: String?) {
        newPhoneNumber?.let { phone ->
            require(phone.isNotBlank()) {
                "Phone number must not be blank if provided"
            }
            require(PHONE_REGEX.matches(phone)) {
                "Invalid phone number format: '$phone'"
            }
        }

        phoneNumber = newPhoneNumber?.trim()
    }

    fun updateAddress(newAddress: String?) {
        newAddress?.let { addr ->
            require(addr.isNotBlank() && addr.length <= 500) {
                "Address must not be blank and at most 500 characters"
            }
        }

        address = newAddress?.trim()
    }

    fun updateProfile(
        fullName: String? = null,
        phoneNumber: String? = null,
        address: String? = null
    ) {
        fullName?.let { updateFullName(it) }
        phoneNumber?.let { updatePhoneNumber(it) }
        address?.let { updateAddress(it) }
    }

    fun deactivate() {
        require(isActive) {
            "User is already deactivated"
        }

        isActive = false
    }

    fun activate() {
        require(!isActive) {
            "User is already active"
        }

        isActive = true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
        "User(id=$id, username='$username', email='$email', role=$role, active=$isActive)"
}
