package com.example.cargotracking.modules.shipment.model.dto.request

import com.example.cargotracking.modules.shipment.model.types.ShipmentStatus
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class AssignDeviceRequest(
    @field:NotNull(message = "Device ID is required")
    val deviceId: UUID
)

data class AssignShipperRequest(
    @field:NotNull(message = "Shipper ID is required")
    val shipperId: UUID
)

data class CancelShipmentRequest(
    @field:NotBlank(message = "Cancellation reason is required")
    @field:Size(min = 10, max = 500, message = "Reason must be 10-500 characters")
    val reason: String
)

data class CompleteShipmentRequest(
    val deliveredAt: Instant? = null
)

data class ShipmentFilterRequest(
    val status: ShipmentStatus? = null,
    val shipperId: UUID? = null,
    val deviceId: UUID? = null,

    val createdAfter: Instant? = null,
    val createdBefore: Instant? = null,

    val search: String? = null,

    @field:Min(value = 1, message = "Page must be at least 1")
    val page: Int = 1,

    @field:Min(value = 1, message = "Page size must be at least 1")
    @field:Max(value = 100, message = "Page size must not exceed 100")
    val pageSize: Int = 20,

    val sortBy: String? = "createdAt",
    val sortOrder: String? = "desc"
)

data class UpdateShipmentRequest(
    @field:Size(min = 10, max = 1000, message = "Goods description must be 10-1000 characters")
    val goodsDescription: String? = null,

    @field:Size(min = 10, max = 500, message = "Pickup address must be 10-500 characters")
    val pickupAddress: String? = null,

    @field:Size(min = 10, max = 500, message = "Delivery address must be 10-500 characters")
    val deliveryAddress: String? = null,

    val estimatedDeliveryAt: Instant? = null
)