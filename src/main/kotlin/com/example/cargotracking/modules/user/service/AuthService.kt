package com.example.cargotracking.modules.user.service

import com.example.cargotracking.common.jwt.JwtManager
import com.example.cargotracking.common.security.RSAKeyProperties
import com.example.cargotracking.modules.user.model.dto.request.LoginRequest
import com.example.cargotracking.modules.user.model.dto.request.RegisterRequest
import com.example.cargotracking.modules.user.model.dto.response.AuthResponse
import com.example.cargotracking.modules.user.model.dto.response.toResponse
import com.example.cargotracking.modules.user.model.entity.RefreshToken
import com.example.cargotracking.modules.user.model.entity.User
import com.example.cargotracking.modules.user.repository.RefreshTokenRepository
import com.example.cargotracking.modules.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
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

        refreshTokenRepository.revokeAllUserTokens(savedUser.id, Instant.now())

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
            expiresAt = decodedRefresh.expiresAtAsInstant(),
            revoked = false,

        )
        refreshTokenRepository.save(refreshEntity)

        val expiresAt = Instant.now()
            .plusMillis(rsaKeyProperties.accessTokenExpiration)
            .toEpochMilli()

        return AuthResponse(
            user = user.toResponse(),
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt
        )
    }
}


