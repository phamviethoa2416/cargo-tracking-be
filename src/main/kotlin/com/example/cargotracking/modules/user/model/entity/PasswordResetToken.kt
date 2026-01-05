package com.example.cargotracking.modules.user.model.entity

import com.example.cargotracking.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "password_reset_tokens",
    indexes = [
        Index(name = "idx_password_reset_token_user_id", columnList = "user_id"),
        Index(name = "idx_password_reset_token_token", columnList = "token")
    ]
)
class PasswordResetToken private constructor (
    id: UUID,

    @Column(name = "user_id", nullable = false)
    var userId: UUID,

    @Column(name = "token", nullable = false, unique = true, length = 500)
    var token: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,

    @Column(name = "used", nullable = false)
    var used: Boolean = false,

    @Column(name = "used_at")
    var usedAt: Instant? = null

) : BaseEntity(id) {
    protected constructor() : this(
        id = UUID.randomUUID(),
        userId = UUID.randomUUID(),
        token = "",
        expiresAt = Instant.now()
    )

    override fun validateInvariants() {
        check(userId != UUID(0, 0)) {
            "User ID must be valid"
        }

        check(token.isNotBlank() && token.length >= 32) {
            "Token must be at least 32 characters"
        }

        if (used) {
            check(usedAt != null) {
                "Used token must have used timestamp"
            }
        }
    }

    companion object {
        private const val MIN_TOKEN_LENGTH = 32

        fun create(
            userId: UUID,
            token: String,
            expiresAt: Instant
        ): PasswordResetToken {
            require(userId != UUID(0, 0)) {
                "User ID must be valid"
            }
            require(token.isNotBlank() && token.length >= MIN_TOKEN_LENGTH) {
                "Token must be at least $MIN_TOKEN_LENGTH characters"
            }
            require(expiresAt.isAfter(Instant.now())) {
                "Expiration time must be in the future"
            }

            val resetToken = PasswordResetToken(
                id = UUID.randomUUID(),
                userId = userId,
                token = token,
                expiresAt = expiresAt,
                used = false,
                usedAt = null
            )

            resetToken.validateInvariants()
            return resetToken
        }
    }

    fun markAsUsed() {
        require(!used) {
            "Token has already been used"
        }
        require(!isExpired()) {
            "Cannot use expired token"
        }

        used = true
        usedAt = Instant.now()
    }

    fun isExpired(): Boolean = expiresAt.isBefore(Instant.now())

    fun isActive(): Boolean = !used && !isExpired()

    fun isValid(): Boolean = isActive()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PasswordResetToken) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "PasswordResetToken(id=$id, userId=$userId, expiresAt=$expiresAt, used=$used)"
    }
}