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
    private var _userId: UUID,

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private var _token: String,

    @Column(name = "expires_at", nullable = false)
    private var _expiresAt: Instant,

    @Column(name = "revoked", nullable = false)
    private var _revoked: Boolean = false,

    @Column(name = "revoked_at")
    private var _revokedAt: Instant? = null

) : BaseEntity(id) {
    protected constructor() : this(
        id = UUID.randomUUID(),
        _userId = UUID.randomUUID(),
        _token = "",
        _expiresAt = Instant.now()
    )

    val userId: UUID get() = _userId
    val token: String get() = _token
    val expiresAt: Instant get() = _expiresAt
    val revoked: Boolean get() = _revoked
    val revokedAt: Instant? get() = _revokedAt

    override fun validateInvariants() {
        check(_userId != UUID(0, 0)) {
            "User ID must be valid"
        }

        check(_token.isNotBlank() && _token.length >= 32) {
            "Token must be at least 32 characters"
        }

        if (_revoked) {
            check(_revokedAt != null) {
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
                _userId = userId,
                _token = token,
                _expiresAt = expiresAt,
                _revoked = false,
                _revokedAt = null
            )

            refreshToken.validateInvariants()
            return refreshToken
        }
    }

    fun revoke() {
        require(!_revoked) {
            "Token is already revoked"
        }

        _revoked = true
        _revokedAt = Instant.now()
    }

    fun isExpired(): Boolean = _expiresAt.isBefore(Instant.now())

    fun isActive(): Boolean = !_revoked && !isExpired()

    fun isValid(): Boolean = isActive()

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