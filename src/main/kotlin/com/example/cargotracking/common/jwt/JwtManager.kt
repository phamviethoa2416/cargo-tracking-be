package com.example.cargotracking.common.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.cargotracking.common.security.RSAKeyProperties
import com.example.cargotracking.modules.user.model.types.UserRole
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtManager(
    private val props: RSAKeyProperties
) {
    private val algorithm =
        Algorithm.RSA512(props.publicKey, props.privateKey)

    private val verifier = JWT.require(algorithm)
        .acceptLeeway(3)
        .build()

    fun issueAccessToken(
        id: UUID,
        email: String,
        role: UserRole
    ): String =
        JWT.create()
            .withSubject(id.toString())
            .withClaim("email", email)
            .withClaim("role", role.name)
            .withClaim("type", "access")
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + props.accessTokenExpiration))
            .sign(algorithm)


    fun issueRefreshToken(id: UUID): String =
        JWT.create()
            .withSubject(id.toString())
            .withClaim("type", "refresh")
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + props.refreshTokenExpiration))
            .sign(algorithm)

    fun decodeAccessToken(token: String): DecodedJWT {
        val decoded = verifier.verify(token)
        require(decoded.getClaim("type").asString() == "access") {
            "Invalid token type"
        }
        return decoded
    }

    fun decodeRefreshToken(token: String): DecodedJWT {
        val decoded = verifier.verify(token)
        require(decoded.getClaim("type").asString() == "refresh") {
            "Invalid token type"
        }
        return decoded
    }
}