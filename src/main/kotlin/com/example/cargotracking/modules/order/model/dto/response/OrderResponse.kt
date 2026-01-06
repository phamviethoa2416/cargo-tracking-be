package com.example.cargotracking.modules.order.model.dto.response

import com.example.cargotracking.modules.order.model.entity.Order
import com.example.cargotracking.modules.order.model.types.OrderStatus
import java.time.Instant
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val customerId: UUID,
    val providerId: UUID,
    val status: OrderStatus,
    val goodsDescription: String,
    val pickupAddress: String,
    val deliveryAddress: String,
    val estimatedDeliveryAt: Instant?,
    
    // Tracking Requirements
    val requireTemperatureTracking: Boolean,
    val minTemperature: Double?,
    val maxTemperature: Double?,
    val requireHumidityTracking: Boolean,
    val minHumidity: Double?,
    val maxHumidity: Double?,
    val requireLocationTracking: Boolean,
    val specialRequirements: String?,
    
    // Processing
    val shipmentId: UUID?,
    val rejectionReason: String?,
    val processedAt: Instant?,
    
    // Timestamps
    val createdAt: Instant?,
    val updatedAt: Instant?
) {
    companion object {
        fun from(order: Order): OrderResponse {
            return OrderResponse(
                id = order.id,
                customerId = order.customerId,
                providerId = order.providerId,
                status = order.status,
                goodsDescription = order.goodsDescription,
                pickupAddress = order.pickupAddress,
                deliveryAddress = order.deliveryAddress,
                estimatedDeliveryAt = order.estimatedDeliveryAt,
                requireTemperatureTracking = order.requireTemperatureTracking,
                minTemperature = order.minTemperature,
                maxTemperature = order.maxTemperature,
                requireHumidityTracking = order.requireHumidityTracking,
                minHumidity = order.minHumidity,
                maxHumidity = order.maxHumidity,
                requireLocationTracking = order.requireLocationTracking,
                specialRequirements = order.specialRequirements,
                shipmentId = order.shipmentId,
                rejectionReason = order.rejectionReason,
                processedAt = order.processedAt,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
        }
    }
}
