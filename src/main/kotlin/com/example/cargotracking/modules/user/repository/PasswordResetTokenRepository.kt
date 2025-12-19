package com.example.cargotracking.modules.user.repository

import com.example.cargotracking.modules.user.model.entity.PasswordResetToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.Optional
import java.util.UUID

interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, UUID> {
    fun findByTokenAndUsedFalseAndExpiresAtAfter(token: String, now: Instant): Optional<PasswordResetToken>

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.id = :tokenId")
    fun markTokenAsUsed(tokenId: UUID): Int
}