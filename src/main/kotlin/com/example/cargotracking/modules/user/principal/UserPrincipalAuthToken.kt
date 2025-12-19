package com.example.cargotracking.modules.user.principal

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority

class UserPrincipalAuthToken(
    private val principal: UserPrincipal
): AbstractAuthenticationToken(
    listOf(SimpleGrantedAuthority("ROLE_${principal.role.name}"))
) {
    init { isAuthenticated = true; }
    override fun getCredentials(): Any? = null
    override fun getPrincipal(): UserPrincipal = principal
}