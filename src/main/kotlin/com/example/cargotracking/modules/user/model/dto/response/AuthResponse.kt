package com.example.cargotracking.modules.user.model.dto.response

data class AuthResponse(
    val user: UserResponse,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresAt: Long
)