package com.example.cargotracking.common.jwt

import com.auth0.jwt.exceptions.JWTVerificationException
import com.example.cargotracking.modules.user.model.types.UserRole
import com.example.cargotracking.modules.user.principal.UserPrincipal
import com.example.cargotracking.modules.user.principal.UserPrincipalAuthToken
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class JwtAuthFilter(
    private val jwtManager: JwtManager
) : OncePerRequestFilter() {

    private companion object {
        const val BEARER_PREFIX = "Bearer "
        const val CLAIM_EMAIL = "email"
        const val CLAIM_ROLE = "role"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractToken(request)

        if (token != null && SecurityContextHolder.getContext().authentication == null) {
            try {
                val decoded = jwtManager.decodeAccessToken(token)

                val principal = UserPrincipal(
                    userId = UUID.fromString(
                        decoded.subject ?: throw JWTVerificationException("Missing subject")
                    ),
                    email = decoded.getClaim(CLAIM_EMAIL).asString()
                        ?: throw JWTVerificationException("Token missing email claim"),
                    role = UserRole.valueOf(
                        decoded.getClaim(CLAIM_ROLE).asString()
                            ?: throw JWTVerificationException("Token missing role claim")
                    )
                )

                SecurityContextHolder.getContext().authentication =
                    UserPrincipalAuthToken(principal)

            } catch (ex: JWTVerificationException) {
                writeUnauthorized(response, ex.message)
                return
            } catch (ex: IllegalArgumentException) {
                writeUnauthorized(response, "Invalid token payload")
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun extractToken(req: HttpServletRequest): String? {
        val authHeader = req.getHeader(HttpHeaders.AUTHORIZATION)
        return authHeader?.takeIf { it.startsWith("Bearer ") }
            ?.substring(7)
    }

    private fun writeUnauthorized(response: HttpServletResponse, message: String?) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(
            """{"error":"invalid_token","message":"${message ?: "Invalid token"}"}"""
        )
    }
}