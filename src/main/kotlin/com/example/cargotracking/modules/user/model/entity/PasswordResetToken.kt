package com.example.cargotracking.modules.user.model.entity

import com.example.cargotracking.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetToken(
    id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "token", nullable = false, unique = true, length = 500)
    val token: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,

    @Column(name = "used", nullable = false)
    val used: Boolean = false

) : BaseEntity(id) {
    init {
        validate()
    }

    override fun validate() {
        require(userId != UUID(0, 0)) { "User ID cannot be null or empty" }
        require(token.isNotBlank() && token.length >= 32) {
            "Token must be at least 32 characters long"
        }
    }

    fun isExpired(): Boolean = expiresAt.isBefore(Instant.now())
    fun isActive(): Boolean = !used && !isExpired()

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