package com.example.cargotracking.modules.user.service

import com.example.cargotracking.common.jwt.JwtManager
import com.example.cargotracking.common.security.RSAKeyProperties
import com.example.cargotracking.modules.user.model.dto.request.*
import com.example.cargotracking.modules.user.model.dto.response.AuthResponse
import com.example.cargotracking.modules.user.model.dto.response.SuccessResponse
import com.example.cargotracking.modules.user.model.dto.response.TokenResponse
import com.example.cargotracking.modules.user.model.dto.response.UserResponse
import com.example.cargotracking.modules.user.model.entity.PasswordResetToken
import com.example.cargotracking.modules.user.model.entity.RefreshToken
import com.example.cargotracking.modules.user.model.entity.User
import com.example.cargotracking.modules.user.model.types.UserRole
import com.example.cargotracking.modules.user.repository.PasswordResetTokenRepository
import com.example.cargotracking.modules.user.repository.RefreshTokenRepository
import com.example.cargotracking.modules.user.repository.UserRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant
import java.util.*

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
    fun registerCustomer(request: CustomerRegisterRequest): AuthResponse {
        validateUserDoesNotExist(request.username, request.email)

        val user = User.create(
            username = request.username,
            email = request.email,
            passwordHash = encodePassword(request.password),
            fullName = request.fullName,
            role = UserRole.CUSTOMER,
            phoneNumber = request.phoneNumber,
            address = request.address
        )

        val savedUser = userRepository.save(user)
        return generateAuthResponse(savedUser)
    }

    @Transactional
    fun createUserByAdmin(request: AdminCreateUserRequest): UserResponse {
        validateUserDoesNotExist(request.username, request.email)

        val tempPassword = generateTemporaryPassword()

        val user = User.create(
            username = request.username,
            email = request.email,
            passwordHash = encodePassword(tempPassword),
            fullName = request.fullName,
            role = request.role,
            phoneNumber = request.phoneNumber,
            address = request.address
        )

        val savedUser = userRepository.save(user)

        return UserResponse.from(savedUser)
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

        revokeAllUserTokens(user.id)

        return generateAuthResponse(user)
    }

    @Transactional
    fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        val tokenEntity = refreshTokenRepository.findByToken(request.refreshToken)
            .orElseThrow { RuntimeException("Invalid refresh token") }

        if (!tokenEntity.isValid()) {
            throw RuntimeException("Refresh token is expired or revoked")
        }

        val decoded = jwtManager.decodeRefreshToken(request.refreshToken)
        val subject = decoded.subject ?: throw RuntimeException("Invalid token subject")

        val userId = UUID.fromString(subject)

        if (tokenEntity.userId != userId) {
            throw RuntimeException("Refresh token does not belong to this user")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        if (!user.isActive) {
            throw RuntimeException("User account is disabled")
        }

        tokenEntity.revoke()
        refreshTokenRepository.save(tokenEntity)

        return generateTokenResponse(user)
    }

    @Transactional
    fun logout(refreshToken: String) {
        refreshTokenRepository.findByToken(refreshToken)
            .ifPresent { token ->
                token.revoke()
                refreshTokenRepository.save(token)
            }
    }

    @Transactional
    fun forgotPassword(request: ForgotPasswordRequest) {
        val user = userRepository.findByEmail(request.email).orElse(null)

        if (user != null && user.isActive) {
            val randomBytes = ByteArray(32)
            SecureRandom().nextBytes(randomBytes)
            val token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
            
            val expiresAt = Instant.now().plusSeconds(3600)

            val resetToken = PasswordResetToken.create(
                userId = user.id,
                token = token,
                expiresAt = expiresAt
            )
            
            passwordResetTokenRepository.save(resetToken)
        }
    }

    @Transactional
    fun resetPassword(request: ResetPasswordRequest): SuccessResponse {
        val tokenEntity = passwordResetTokenRepository
            .findByTokenAndUsedFalseAndExpiresAtAfter(request.token, Instant.now())
            .orElseThrow { RuntimeException("Invalid or expired reset token") }

        if (!tokenEntity.isActive()) {
            throw IllegalStateException("Reset token is expired or already used")
        }

        val user = userRepository.findById(tokenEntity.userId)
            .orElseThrow { NoSuchElementException("User not found") }

        if (!user.isActive) {
            throw RuntimeException("User account is disabled")
        }

        user.updatePassword(encodePassword(request.newPassword))
        userRepository.save(user)

        tokenEntity.markAsUsed()
        passwordResetTokenRepository.save(tokenEntity)

        revokeAllUserTokens(user.id)

        return SuccessResponse(
            message = "Password has been reset successfully. Please login with your new password."
        )
    }

    private fun validateUserDoesNotExist(username: String, email: String) {
        if (userRepository.existsByUsername(username)) {
            throw RuntimeException("Username is already taken")
        }
        if (userRepository.existsByEmail(email)) {
            throw RuntimeException("Email is already in use")
        }
    }

    private fun generateTemporaryPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%"
        val random = SecureRandom()
        return (1..12)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }

    private fun revokeAllUserTokens(userId: UUID) {
        val activeTokens = refreshTokenRepository.findByUserIdAndRevoked(userId, false)
        activeTokens.forEach { it.revoke() }
        refreshTokenRepository.saveAll(activeTokens)
    }

    private fun encodePassword(password: String): String {
        return passwordEncoder.encode(password)
            ?: throw IllegalStateException("Password encoding failed")
    }

    private fun generateAuthResponse(user: User): AuthResponse {
        val accessToken = jwtManager.issueAccessToken(user.id, user.email, user.role)
        val refreshToken = jwtManager.issueRefreshToken(user.id)

        val decodedRefresh = jwtManager.decodeRefreshToken(refreshToken)

        val refreshTokenExpiresAt = decodedRefresh.expiresAt?.toInstant()
            ?: throw IllegalStateException("Refresh token missing expiration")

        val refreshEntity = RefreshToken.create(
            userId = user.id,
            token = refreshToken,
            expiresAt = refreshTokenExpiresAt
        )
        refreshTokenRepository.save(refreshEntity)

        val expiresIn = rsaKeyProperties.accessTokenExpiration / 1000
        val expiresAt = Instant.now().plusSeconds(expiresIn)

        return AuthResponse(
            user = UserResponse.from(user),
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            expiresIn = expiresIn,
            expiresAt = expiresAt
        )
    }

    private fun generateTokenResponse(user: User): TokenResponse {
        val accessToken = jwtManager.issueAccessToken(user.id, user.email, user.role)
        val refreshToken = jwtManager.issueRefreshToken(user.id)

        val decodedRefresh = jwtManager.decodeRefreshToken(refreshToken)

        val refreshTokenExpiresAt = decodedRefresh.expiresAt?.toInstant()
            ?: throw IllegalStateException("Refresh token missing expiration")

        val refreshEntity = RefreshToken.create(
            userId = user.id,
            token = refreshToken,
            expiresAt = refreshTokenExpiresAt
        )
        refreshTokenRepository.save(refreshEntity)

        val expiresIn = rsaKeyProperties.accessTokenExpiration / 1000
        val expiresAt = Instant.now().plusSeconds(expiresIn)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            expiresIn = expiresIn,
            expiresAt = expiresAt
        )
    }
}


