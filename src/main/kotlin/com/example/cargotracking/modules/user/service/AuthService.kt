package com.example.cargotracking.modules.user.service

import com.example.cargotracking.common.jwt.JwtManager
import com.example.cargotracking.common.security.RSAKeyProperties
import com.example.cargotracking.modules.user.model.dto.request.ForgotPasswordRequest
import com.example.cargotracking.modules.user.model.dto.request.LoginRequest
import com.example.cargotracking.modules.user.model.dto.request.RegisterRequest
import com.example.cargotracking.modules.user.model.dto.request.ResetPasswordRequest
import com.example.cargotracking.modules.user.model.dto.response.AuthResponse
import com.example.cargotracking.modules.user.model.dto.response.UserResponse
import com.example.cargotracking.modules.user.model.entity.PasswordResetToken
import com.example.cargotracking.modules.user.model.entity.RefreshToken
import com.example.cargotracking.modules.user.model.entity.User
import com.example.cargotracking.modules.user.repository.PasswordResetTokenRepository
import com.example.cargotracking.modules.user.repository.RefreshTokenRepository
import com.example.cargotracking.modules.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val jwtManager: JwtManager,
    private val passwordEncoder: PasswordEncoder,
    private val rsaKeyProperties: RSAKeyProperties
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalStateException("Email is already in use")
        }
        if (userRepository.existsByUsername(request.username)) {
            throw IllegalStateException("Username is already taken")
        }

        val encodedPassword = passwordEncoder.encode(request.password)
            ?: throw IllegalStateException("Password encoding failed")

        val user = User(
            username = request.username,
            email = request.email,
            passwordHash = encodedPassword,
            fullName = request.fullName,
            phoneNumber = request.phoneNumber,
            role = request.role,
            address = request.address
        )

        val savedUser = userRepository.save(user)

        return generateAuthResponse(savedUser)
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("Invalid email or password") }

        if (!user.isActive) {
            throw IllegalStateException("User account is disabled")
        }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        refreshTokenRepository.revokeAllUserTokens(user.id, Instant.now())

        return generateAuthResponse(user)
    }

    @Transactional
    fun refreshToken(refreshToken: String): AuthResponse {
        val tokenEntity = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow { IllegalArgumentException("Invalid refresh token") }

        if (!tokenEntity.isActive()) {
            throw IllegalStateException("Refresh token is expired or revoked")
        }

        val decoded = jwtManager.decodeRefreshToken(refreshToken)
        val subject = decoded.subject ?: throw IllegalStateException("Invalid token subject")

        val userId = UUID.fromString(subject)

        if (tokenEntity.userId != userId) {
            throw IllegalStateException("Refresh token does not belong to this user")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        if (!user.isActive) {
            throw IllegalStateException("User account is disabled")
        }

        refreshTokenRepository.revokeAllUserTokens(user.id, Instant.now())

        return generateAuthResponse(user)
    }

    private fun generateAuthResponse(user: User): AuthResponse {
        val accessToken = jwtManager.issueAccessToken(user.id, user.email, user.role)
        val refreshToken = jwtManager.issueRefreshToken(user.id)

        val decodedRefresh = jwtManager.decodeRefreshToken(refreshToken)

        val refreshEntity = RefreshToken(
            userId = user.id,
            token = refreshToken,
            expiresAt = decodedRefresh.expiresAt.toInstant(),
            revoked = false,

        )
        refreshTokenRepository.save(refreshEntity)

        val expiresAt = Instant.now()
            .plusMillis(rsaKeyProperties.accessTokenExpiration)
            .toEpochMilli()

        return AuthResponse(
            user = UserResponse.from(user),
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt
        )
    }

    @Transactional
    fun forgotPassword(request: ForgotPasswordRequest) {
        val user = userRepository.findByEmail(request.email)
            .orElse(null)

        if (user != null && user.isActive) {
            val randomBytes = ByteArray(32)
            SecureRandom().nextBytes(randomBytes)
            val token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
            
            val expiresAt = Instant.now().plusSeconds(3600)

            val resetToken = PasswordResetToken(
                userId = user.id,
                token = token,
                expiresAt = expiresAt,
                used = false
            )
            
            passwordResetTokenRepository.save(resetToken)
            
            // TODO: Send email with reset token link
            // emailService.sendPasswordResetEmail(user.email, token)
        }
    }

    @Transactional
    fun resetPassword(request: ResetPasswordRequest) {
        val now = Instant.now()
        val tokenEntity = passwordResetTokenRepository
            .findByTokenAndUsedFalseAndExpiresAtAfter(request.token, now)
            .orElseThrow { IllegalArgumentException("Invalid or expired reset token") }

        if (!tokenEntity.isActive()) {
            throw IllegalStateException("Reset token is expired or already used")
        }

        val user = userRepository.findById(tokenEntity.userId)
            .orElseThrow { NoSuchElementException("User not found") }

        if (!user.isActive) {
            throw IllegalStateException("User account is disabled")
        }

        val encodedPassword = passwordEncoder.encode(request.newPassword)
            ?: throw IllegalStateException("Password encoding failed")

        userRepository.updatePasswordHash(user.id, encodedPassword)
        passwordResetTokenRepository.markTokenAsUsed(tokenEntity.id)

        refreshTokenRepository.revokeAllUserTokens(user.id, Instant.now())
    }
}


