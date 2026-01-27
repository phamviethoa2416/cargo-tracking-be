package com.example.cargotracking.modules.user.principal

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority

class UserPrincipalAuthToken(
    private val principal: UserPrincipal
) : AbstractAuthenticationToken(
    listOf(SimpleGrantedAuthority("ROLE_${principal.role.name}"))
) {
    override fun getPrincipal(): UserPrincipal = principal
    override fun getCredentials(): Any? = null // JWT-based authentication
    override fun getName(): String = principal.email

    override fun isAuthenticated(): Boolean = true
}