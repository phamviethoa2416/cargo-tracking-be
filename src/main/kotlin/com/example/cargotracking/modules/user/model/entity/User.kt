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
    private var _username: String,

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private var _email: String,

    @Column(name = "password_hash", nullable = false)
    private var _passwordHash: String,

    @Column(name = "full_name", nullable = false, length = 255)
    private var _fullName: String,

    @Column(name = "phone_number", length = 20)
    private var _phoneNumber: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private var _role: UserRole,

    @Column(name = "address", length = 500)
    private var _address: String? = null,

    @Column(name = "is_active", nullable = false)
    private var _isActive: Boolean = true

) : BaseEntity(id) {
    protected constructor() : this(
        id = UUID.randomUUID(),
        _username = "",
        _email = "",
        _passwordHash = "",
        _fullName = "",
        _role = UserRole.CUSTOMER
    )

    val username: String get() = _username
    val email: String get() = _email
    val passwordHash: String get() = _passwordHash
    val fullName: String get() = _fullName
    val phoneNumber: String? get() = _phoneNumber
    val role: UserRole get() = _role
    val address: String? get() = _address
    val isActive: Boolean get() = _isActive

    override fun validateInvariants() {
        check(_username.isNotBlank() && _username.length in 3..100) {
            "Username must be 3-100 characters, got: '${_username}'"
        }

        check(_email.isNotBlank() && EMAIL_REGEX.matches(_email)) {
            "Invalid email format: '${_email}'"
        }

        check(_passwordHash.isNotBlank()) {
            "Password hash must not be blank"
        }

        check(_fullName.isNotBlank() && _fullName.length in 2..255) {
            "Full name must be 2-255 characters, got: '${_fullName}'"
        }

        _phoneNumber?.let { phone ->
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
                _username = username.trim(),
                _email = email.trim().lowercase(),
                _passwordHash = passwordHash,
                _fullName = fullName.trim(),
                _phoneNumber = phoneNumber?.trim(),
                _role = role,
                _address = address?.trim(),
                _isActive = true
            )

            user.validateInvariants()
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

        _email = newEmail.trim().lowercase()
    }

    fun updatePassword(newPasswordHash: String) {
        require(newPasswordHash.isNotBlank()) {
            "Password hash must not be blank"
        }

        _passwordHash = newPasswordHash
    }

    fun updateFullName(newFullName: String) {
        require(newFullName.isNotBlank() && newFullName.length in 2..255) {
            "Full name must be 2-255 characters long"
        }

        _fullName = newFullName.trim()
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

        _phoneNumber = newPhoneNumber?.trim()
    }

    fun updateAddress(newAddress: String?) {
        newAddress?.let { address ->
            require(address.isNotBlank() && address.length <= 500) {
                "Address must not be blank and at most 500 characters"
            }
        }

        _address = newAddress?.trim()
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
        require(_isActive) {
            "User is already deactivated"
        }

        _isActive = false
    }

    fun activate() {
        require(!_isActive) {
            "User is already active"
        }

        _isActive = true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "User(id=$id, username='$_username', email='$_email', role=$_role, active=$_isActive)"
    }
}
