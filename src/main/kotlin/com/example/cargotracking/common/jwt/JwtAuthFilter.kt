package com.example.cargotracking.common.jwt

import com.example.cargotracking.modules.user.model.types.UserRole
import com.example.cargotracking.modules.user.principal.UserPrincipal
import com.example.cargotracking.modules.user.principal.UserPrincipalAuthToken
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class JwtAuthFilter(
    private val jwtManager: JwtManager
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (SecurityContextHolder.getContext().authentication == null) {
            val token = extractToken(request)

            if (token != null) {
                try {
                    val decodedJWT = jwtManager.decodeAccessToken(token)

                    val email = decodedJWT.getClaim("email").asString()
                    val roleStr = decodedJWT.getClaim("role").asString()
                    val subject = decodedJWT.subject

                    if (!subject.isNullOrBlank() && !email.isNullOrBlank() && !roleStr.isNullOrBlank()) {
                        val principal = UserPrincipal(
                            userId = UUID.fromString(subject),
                            email = email,
                            role = UserRole.valueOf(roleStr)
                        )

                        val authentication = UserPrincipalAuthToken(principal)
                        SecurityContextHolder.getContext().authentication = authentication
                    }
                } catch (ex: Exception) {
                    throw (ex)
                }
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun extractToken(req: HttpServletRequest): String? {
        val authHeader = req.getHeader(HttpHeaders.AUTHORIZATION)
        return if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authHeader.substring(7)
        } else null
    }
}