package com.example.cargotracking.modules.user.repository

import com.example.cargotracking.modules.user.model.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional
import java.util.UUID

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByToken(token: String): Optional<RefreshToken>

    fun findAllByUserId(userId: UUID): List<RefreshToken>

    @Modifying
    @Query("UPDATE RefreshToken t SET t.revoked = true, t.revokedAt = :now WHERE t.userId = :userId AND t.revoked = false")
    fun revokeAllUserTokens(userId: UUID, now: Instant): Int

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expiresAt < :now")
    fun deleteExpiredTokens(now: Instant): Int
}