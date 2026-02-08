package com.example.cargotracking.modules.shipment.model.dto.response

import com.example.cargotracking.modules.shipment.model.entity.Shipment
import com.example.cargotracking.modules.shipment.model.types.ShipmentStatus
import java.time.Instant
import java.util.UUID

data class ShipmentResponse(
    val id: UUID,
    val status: ShipmentStatus,

    val customerId: UUID,
    val providerId: UUID,
    val shipperId: UUID?,
    val deviceId: UUID?,

    val goodsDescription: String,

    val pickupAddress: String,
    val deliveryAddress: String,

    val estimatedDeliveryAt: Instant?,
    val actualDeliveryAt: Instant?,

    val requireTemperatureTracking: Boolean,
    val minTemperature: Double?,
    val maxTemperature: Double?,
    val requireHumidityTracking: Boolean,
    val minHumidity: Double?,
    val maxHumidity: Double?,
    val requireLocationTracking: Boolean,
    val specialRequirements: String?,

    val createdAt: Instant?,
    val updatedAt: Instant?
) {
    companion object {
        fun from(shipment: Shipment): ShipmentResponse {
            return ShipmentResponse(
                id = shipment.id,
                status = shipment.status,
                customerId = shipment.customerId,
                providerId = shipment.providerId,
                shipperId = shipment.shipperId,
                deviceId = shipment.deviceId,
                goodsDescription = shipment.goodsDescription,
                pickupAddress = shipment.pickupAddress,
                deliveryAddress = shipment.deliveryAddress,
                estimatedDeliveryAt = shipment.estimatedDeliveryAt,
                actualDeliveryAt = shipment.actualDeliveryAt,
                requireTemperatureTracking = shipment.requireTemperatureTracking,
                minTemperature = shipment.minTemperature,
                maxTemperature = shipment.maxTemperature,
                requireHumidityTracking = shipment.requireHumidityTracking,
                minHumidity = shipment.minHumidity,
                maxHumidity = shipment.maxHumidity,
                requireLocationTracking = shipment.requireLocationTracking,
                specialRequirements = shipment.specialRequirements,
                createdAt = shipment.createdAt,
                updatedAt = shipment.updatedAt
            )
        }
    }
}

data class ShipmentListResponse(
    val shipments: List<ShipmentResponse>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)