package com.example.cargotracking.modules.user.repository

import com.example.cargotracking.modules.user.model.entity.PasswordResetToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional
import java.util.UUID

@Repository
interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, UUID> {
    fun findByTokenAndUsedFalseAndExpiresAtAfter(token: String, now: Instant): Optional<PasswordResetToken>

    fun findByToken(token: String): Optional<PasswordResetToken>
    fun findAllByUserId(userId: UUID): List<PasswordResetToken>

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t._expiresAt < :now")
    fun deleteExpiredTokens(now: Instant): Int
}