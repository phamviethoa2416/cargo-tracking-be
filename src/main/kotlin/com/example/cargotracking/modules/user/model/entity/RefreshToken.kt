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
    name = "refresh_tokens",
    indexes = [
        Index(name = "idx_refresh_token_user_id", columnList = "user_id"),
        Index(name = "idx_refresh_token_token", columnList = "token")
    ]
)
class RefreshToken private constructor(
    id: UUID,

    @Column(name = "user_id", nullable = false)
    var userId: UUID,

    @Column(name = "token", nullable = false, unique = true, length = 1000)
    var token: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,

    @Column(name = "revoked", nullable = false)
    var revoked: Boolean = false,

    @Column(name = "revoked_at")
    var revokedAt: Instant? = null

) : BaseEntity(id) {
    protected constructor() : this(
        id = UUID(0, 0),
        userId = UUID(0, 0),
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

        if (revoked) {
            check(revokedAt != null) {
                "Revoked token must have revoked timestamp"
            }
        }
    }

    companion object {
        private const val MIN_TOKEN_LENGTH = 32

        fun create(
            userId: UUID,
            token: String,
            expiresAt: Instant
        ): RefreshToken {
            require(userId != UUID(0, 0)) {
                "User ID must be valid"
            }
            require(token.isNotBlank() && token.length >= MIN_TOKEN_LENGTH) {
                "Token must be at least $MIN_TOKEN_LENGTH characters"
            }
            require(expiresAt.isAfter(Instant.now())) {
                "Expiration time must be in the future"
            }

            val refreshToken = RefreshToken(
                id = UUID.randomUUID(),
                userId = userId,
                token = token,
                expiresAt = expiresAt,
                revoked = false,
                revokedAt = null
            )
            return refreshToken
        }
    }

    fun revoke() {
        require(!revoked) {
            "Token is already revoked"
        }

        revoked = true
        revokedAt = Instant.now()
    }

    fun isExpired(): Boolean = expiresAt.isBefore(Instant.now())

    fun isActive(): Boolean = !revoked && !isExpired()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RefreshToken) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
        "RefreshToken(id=$id, userId=$userId, expiresAt=$expiresAt, revoked=$revoked)"
}