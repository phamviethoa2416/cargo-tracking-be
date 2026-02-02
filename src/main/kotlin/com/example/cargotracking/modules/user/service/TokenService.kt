package com.example.cargotracking.modules.user.service

import com.example.cargotracking.common.jwt.JwtManager
import com.example.cargotracking.common.security.RSAKeyProperties
import com.example.cargotracking.modules.user.exception.UserException
import com.example.cargotracking.modules.user.model.dto.response.AuthResponse
import com.example.cargotracking.modules.user.model.dto.response.TokenResponse
import com.example.cargotracking.modules.user.model.dto.response.UserResponse
import com.example.cargotracking.modules.user.model.entity.RefreshToken
import com.example.cargotracking.modules.user.model.entity.User
import com.example.cargotracking.modules.user.repository.RefreshTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class TokenService(
    private val jwtManager: JwtManager,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val rsaKeyProperties: RSAKeyProperties
) {

    @Transactional
    fun generateAuthResponse(user: User): AuthResponse {
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

    @Transactional
    fun generateTokenResponse(user: User): TokenResponse {
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

    @Transactional
    fun validateAndRevokeRefreshToken(token: String, userId: UUID): RefreshToken {
        val tokenEntity = refreshTokenRepository.findByTokenWithLock(token)
            .orElseThrow { UserException.InvalidTokenException("Invalid refresh token") }

        if (tokenEntity.userId != userId) {
            throw UserException.InvalidTokenException("Refresh token does not belong to this user")
        }

        if (tokenEntity.isExpired()) {
            throw UserException.TokenExpiredException("Refresh token is expired")
        }

        if (tokenEntity.revoked) {
            throw UserException.TokenRevokedException("Refresh token has been revoked")
        }

        tokenEntity.revoke()
        refreshTokenRepository.save(tokenEntity)

        return tokenEntity
    }

    @Transactional
    fun revokeAllUserTokens(userId: UUID) {
        val activeTokens = refreshTokenRepository.findByUserIdAndRevoked(userId, false)
        activeTokens.forEach { it.revoke() }
        refreshTokenRepository.saveAll(activeTokens)
    }

    @Transactional
    fun revokeToken(token: String) {
        refreshTokenRepository.findByToken(token)
            .ifPresent { tokenEntity ->
                if (!tokenEntity.revoked) {
                    tokenEntity.revoke()
                    refreshTokenRepository.save(tokenEntity)
                }
            }
    }
}
