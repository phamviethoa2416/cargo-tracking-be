package com.example.cargotracking.common.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.cargotracking.common.security.RSAKeyProperties
import com.example.cargotracking.modules.user.model.types.UserRole
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class JwtManager(
    private val jwtProperties: RSAKeyProperties
) {
    private val algorithm: Algorithm =
        Algorithm.RSA512(jwtProperties.publicKey, jwtProperties.privateKey)

    fun issueAccessToken(
        id: UUID,
        email: String,
        role: UserRole
    ): String {
        return JWT.create()
            .withSubject(id.toString())
            .withClaim("email", email)
            .withClaim("role", role.name)
            .withClaim("type", "access")
            .withExpiresAt(Instant.now().plusMillis(jwtProperties.accessTokenExpiration))
            .sign(algorithm)
    }

    fun issueRefreshToken(id: UUID): String {
        return JWT.create()
            .withSubject(id.toString())
            .withClaim("type", "refresh")
            .withExpiresAt(Instant.now().plusMillis(jwtProperties.refreshTokenExpiration))
            .sign(algorithm)
    }

    fun decode(token: String): DecodedJWT =
        JWT.require(algorithm)
            .build()
            .verify(token)

    fun decodeAccessToken(token: String): DecodedJWT {
        val decoded = decode(token)
        val type = decoded.getClaim("type").asString()
        if (type != "access") {
            throw JWTVerificationException("Invalid token type: expected 'access' but was '$type'")
        }
        return decoded
    }

    fun decodeRefreshToken(token: String): DecodedJWT {
        val decoded = decode(token)
        val type = decoded.getClaim("type").asString()
        if (type != "refresh") {
            throw JWTVerificationException("Invalid token type: expected 'refresh' but was '$type'")
        }
        return decoded
    }
}