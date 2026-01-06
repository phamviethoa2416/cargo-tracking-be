package com.example.cargotracking.modules.order.model.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RejectOrderRequest(
    @field:NotBlank(message = "Rejection reason is required")
    @field:Size(min = 5, max = 500, message = "Rejection reason must be 5-500 characters")
    val reason: String
)
