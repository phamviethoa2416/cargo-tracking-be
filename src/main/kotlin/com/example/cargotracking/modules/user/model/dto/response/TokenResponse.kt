package com.example.cargotracking.modules.user.model.dto.response

import java.time.Instant

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val expiresAt: Instant
)