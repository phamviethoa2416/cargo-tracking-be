package com.example.cargotracking.modules.user.model.entity

import com.example.cargotracking.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "token", nullable = false, unique = true, length = 500)
    val token: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,

    @Column(name = "revoked", nullable = false)
    val revoked: Boolean = false,

    @Column(name = "revoked_at")
    val revokedAt: Instant? = null
) : BaseEntity(id) {
    init {
        validate()
    }

    override fun validate() {
        require(userId != UUID(0, 0)) {
            "User ID must not be the zero UUID."
        }
        require(token.isNotBlank() && token.length >= 32) {
            "Token must not be blank and must be at least 32 characters long."
        }
    }

    fun isExpired(): Boolean = expiresAt.isBefore(Instant.now())
    fun isActive(): Boolean = !revoked && !isExpired()

    fun revoke(): RefreshToken {
        if (revoked) return this
        return RefreshToken(
            id = this.id,
            userId = this.userId,
            token = this.token,
            expiresAt = this.expiresAt,
            revoked = true,
            revokedAt = Instant.now()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RefreshToken) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "RefreshToken(id=$id, userId=$userId, expiresAt=$expiresAt, revoked=$revoked)"
    }
}