package com.example.cargotracking.common.jwt

import com.example.cargotracking.modules.user.model.types.UserRole
import com.example.cargotracking.modules.user.principal.UserPrincipal
import com.example.cargotracking.modules.user.principal.UserPrincipalAuthToken
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class JwtAuthFilter(
    private val jwtManager: JwtManager
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractToken(request)

        if (token != null && SecurityContextHolder.getContext().authentication == null) {
            try {
                val decodedJWT = jwtManager.decodeAccessToken(token)

                val email = decodedJWT.getClaim("email").asString()
                    ?: throw IllegalArgumentException("Token missing email claim")
                val roleStr = decodedJWT.getClaim("role").asString()
                    ?: throw IllegalArgumentException("Token missing role claim")
                
                val principal = UserPrincipal(
                    userId = UUID.fromString(decodedJWT.subject ?: throw IllegalArgumentException("Token missing subject")),
                    email = email,
                    role = UserRole.valueOf(roleStr)
                )

                SecurityContextHolder.getContext().authentication =
                    UserPrincipalAuthToken(principal)

            } catch (ex: Exception) {
                // Catch all exceptions (JWTVerificationException, IllegalArgumentException, etc.)
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                response.contentType = "application/json"
                response.writer.write(
                    """{"error":"invalid_token","message":"${ex.message ?: "Invalid token"}"}"""
                )
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
}