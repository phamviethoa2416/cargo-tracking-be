package com.example.cargotracking.modules.user.service

import com.example.cargotracking.common.jwt.JwtManager
import com.example.cargotracking.modules.user.exception.UserException
import com.example.cargotracking.modules.user.model.dto.request.admin.AdminCreateUserRequest
import com.example.cargotracking.modules.user.model.dto.request.auth.*
import com.example.cargotracking.modules.user.model.dto.response.AuthResponse
import com.example.cargotracking.modules.user.model.dto.response.TokenResponse
import com.example.cargotracking.modules.user.model.dto.response.UserResponse
import com.example.cargotracking.modules.user.model.entity.PasswordResetToken
import com.example.cargotracking.modules.user.model.entity.User
import com.example.cargotracking.modules.user.model.types.UserRole
import com.example.cargotracking.modules.user.repository.PasswordResetTokenRepository
import com.example.cargotracking.modules.user.repository.UserRepository
import com.example.cargotracking.common.response.SuccessResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService,
    private val emailService: EmailService,
    private val jwtManager: JwtManager
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
        return tokenService.generateAuthResponse(savedUser)
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

        emailService.sendPasswordEmail(
            email = savedUser.email,
            username = savedUser.username,
            password = tempPassword
        )

        return UserResponse.from(savedUser)
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { UserException.InvalidCredentialsException() }

        if (!user.isActive) {
            throw UserException.UserAccountDisabledException()
        }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw UserException.InvalidCredentialsException()
        }

        tokenService.revokeAllUserTokens(user.id)

        return tokenService.generateAuthResponse(user)
    }

    @Transactional
    fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        val decoded = try {
            jwtManager.decodeRefreshToken(request.refreshToken)
        } catch (e: Exception) {
            throw UserException.InvalidTokenException("Invalid refresh token format")
        }

        val subject = decoded.subject ?: throw UserException.InvalidTokenException("Invalid token subject")
        val userId = try {
            UUID.fromString(subject)
        } catch (e: IllegalArgumentException) {
            throw UserException.InvalidTokenException("Invalid token subject format")
        }

        tokenService.validateAndRevokeRefreshToken(request.refreshToken, userId)

        val user = userRepository.findById(userId)
            .orElseThrow { UserException.UserNotFoundException("User not found") }

        if (!user.isActive) {
            throw UserException.UserAccountDisabledException()
        }

        return tokenService.generateTokenResponse(user)
    }

    @Transactional
    fun logout(refreshToken: String) {
        tokenService.revokeToken(refreshToken)
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
            emailService.sendPasswordResetEmail(user.email, token)
        }
    }

    @Transactional
    fun resetPassword(request: ResetPasswordRequest): SuccessResponse {
        val tokenEntity = passwordResetTokenRepository
            .findByTokenAndUsedFalseAndExpiresAtAfter(request.token, Instant.now())
            .orElseThrow { UserException.InvalidTokenException("Invalid or expired reset token") }

        if (!tokenEntity.isActive()) {
            throw UserException.TokenExpiredException("Reset token is expired or already used")
        }

        val user = userRepository.findById(tokenEntity.userId)
            .orElseThrow { UserException.UserNotFoundException("User not found") }

        if (!user.isActive) {
            throw UserException.UserAccountDisabledException()
        }

        user.updatePassword(encodePassword(request.newPassword))
        userRepository.save(user)

        tokenEntity.markAsUsed()
        passwordResetTokenRepository.save(tokenEntity)

        tokenService.revokeAllUserTokens(user.id)

        return SuccessResponse(
            message = "Password has been reset successfully. Please login with your new password."
        )
    }

    private fun validateUserDoesNotExist(username: String, email: String) {
        if (userRepository.existsByUsername(username)) {
            throw UserException.UserAlreadyExistsException("Username is already taken")
        }
        if (userRepository.existsByEmail(email)) {
            throw UserException.UserAlreadyExistsException("Email is already in use")
        }
    }

    private fun generateTemporaryPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%"
        val random = SecureRandom()
        return (1..12)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }

    private fun encodePassword(password: String): String {
        return passwordEncoder.encode(password)
            ?: throw IllegalStateException("Password encoding failed")
    }
}
