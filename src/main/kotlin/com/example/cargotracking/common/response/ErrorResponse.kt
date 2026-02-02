package com.example.cargotracking.common.response

import java.time.Instant

data class ErrorResponse(
    val message: String,
    val error: String,
    val status: Int,
    val timestamp: Instant = Instant.now(),
    val path: String? = null
) {
    companion object {
        fun of(message: String, error: String, status: Int, path: String? = null): ErrorResponse {
            return ErrorResponse(message, error, status, Instant.now(), path)
        }
    }
}

