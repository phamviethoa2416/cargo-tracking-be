package com.example.cargotracking.modules.user.model.dto.response

data class SuccessResponse(
    val message: String,
    val data: Any? = null
) {
    companion object {
        fun of(message: String): SuccessResponse {
            return SuccessResponse(message)
        }

        fun of(message: String, data: Any): SuccessResponse {
            return SuccessResponse(message, data)
        }
    }
}

