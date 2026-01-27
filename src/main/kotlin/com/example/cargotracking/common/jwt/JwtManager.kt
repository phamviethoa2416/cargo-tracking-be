package com.example.cargotracking.common.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.cargotracking.common.security.RSAKeyProperties
import com.example.cargotracking.modules.user.model.types.UserRole
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class JwtManager(
    private val props: RSAKeyProperties
) {
    private val algorithm =
        Algorithm.RSA256(props.publicKey, props.privateKey)

    private val verifier: JWTVerifier =
        JWT.require(algorithm)
            .acceptLeeway(3)
            .build()

    private companion object {
        const val CLAIM_TYPE = "type"
        const val CLAIM_ROLE = "role"
        const val CLAIM_EMAIL = "email"

        const val TYPE_ACCESS = "access"
        const val TYPE_REFRESH = "refresh"
    }

    fun issueAccessToken(
        userId: UUID,
        email: String,
        role: UserRole
    ): String =
        JWT.create()
            .withSubject(userId.toString())
            .withClaim(CLAIM_EMAIL, email)
            .withClaim(CLAIM_ROLE, role.name)
            .withClaim(CLAIM_TYPE, TYPE_ACCESS)
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(
                Date.from(Instant.now().plusMillis(props.accessTokenExpiration))
            )
            .sign(algorithm)


    fun issueRefreshToken(userId: UUID): String =
        JWT.create()
            .withSubject(userId.toString())
            .withClaim(CLAIM_TYPE, TYPE_REFRESH)
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(
                Date.from(Instant.now().plusMillis(props.refreshTokenExpiration))
            )
            .sign(algorithm)

    fun decodeAccessToken(token: String): DecodedJWT =
        decode(token, TYPE_ACCESS)

    fun decodeRefreshToken(token: String): DecodedJWT =
        decode(token, TYPE_REFRESH)

    private fun decode(token: String, expectedType: String): DecodedJWT {
        val decoded = verifier.verify(token)

        val actualType = decoded.getClaim(CLAIM_TYPE).asString()
            ?: throw JWTVerificationException("Token missing type claim")

        if (actualType != expectedType) {
            throw JWTVerificationException("Invalid token type: $actualType")
        }

        return decoded
    }
}