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

    fun findByUserIdAndRevoked(userId: UUID, revoked: Boolean): List<RefreshToken>

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expiresAt < :now")
    fun deleteExpiredTokens(now: Instant): Int

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.userId = :userId")
    fun deleteByUserId(userId: UUID): Int
}