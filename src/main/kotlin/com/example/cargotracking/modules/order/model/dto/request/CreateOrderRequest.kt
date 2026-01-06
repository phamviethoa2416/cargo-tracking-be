package com.example.cargotracking.modules.order.model.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class CreateOrderRequest(
    @field:NotNull(message = "Provider ID is required")
    val providerId: UUID,

    @field:Size(max = 1000, message = "Goods description must be at most 1000 characters")
    val goodsDescription: String? = null,

    @field:NotBlank(message = "Pickup address is required")
    @field:Size(min = 10, max = 500, message = "Pickup address must be 10-500 characters")
    val pickupAddress: String,

    @field:NotBlank(message = "Delivery address is required")
    @field:Size(min = 10, max = 500, message = "Delivery address must be 10-500 characters")
    val deliveryAddress: String,

    val estimatedDeliveryAt: Instant? = null,

    // Tracking Requirements
    val requireTemperatureTracking: Boolean = false,

    @field:Min(value = -50, message = "Min temperature must be at least -50")
    @field:Max(value = 100, message = "Min temperature must be at most 100")
    val minTemperature: Double? = null,

    @field:Min(value = -50, message = "Max temperature must be at least -50")
    @field:Max(value = 100, message = "Max temperature must be at most 100")
    val maxTemperature: Double? = null,

    val requireHumidityTracking: Boolean = false,

    @field:Min(value = 0, message = "Min humidity must be at least 0")
    @field:Max(value = 100, message = "Min humidity must be at most 100")
    val minHumidity: Double? = null,

    @field:Min(value = 0, message = "Max humidity must be at least 0")
    @field:Max(value = 100, message = "Max humidity must be at most 100")
    val maxHumidity: Double? = null,

    val requireLocationTracking: Boolean = true,

    @field:Size(max = 1000, message = "Special requirements must be at most 1000 characters")
    val specialRequirements: String? = null
)
