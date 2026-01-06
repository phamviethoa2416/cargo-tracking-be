package com.example.cargotracking.modules.order.model.entity

import com.example.cargotracking.common.entity.BaseEntity
import com.example.cargotracking.modules.order.model.types.OrderStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "orders",
    indexes = [
        Index(name = "idx_order_customer_id", columnList = "customer_id"),
        Index(name = "idx_order_provider_id", columnList = "provider_id"),
        Index(name = "idx_order_status", columnList = "status"),
        Index(name = "idx_order_shipment_id", columnList = "shipment_id")
    ]
)
class Order private constructor(
    id: UUID,

    @Column(name = "customer_id", nullable = false)
    var customerId: UUID,

    @Column(name = "provider_id", nullable = false)
    var providerId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(name = "goods_description", nullable = false, length = 1000)
    var goodsDescription: String,

    @Column(name = "pickup_address", nullable = false, length = 500)
    var pickupAddress: String,

    @Column(name = "delivery_address", nullable = false, length = 500)
    var deliveryAddress: String,

    @Column(name = "estimated_delivery_at")
    var estimatedDeliveryAt: Instant? = null,

    // Tracking Requirements
    @Column(name = "require_temperature_tracking", nullable = false)
    var requireTemperatureTracking: Boolean = false,

    @Column(name = "min_temperature")
    var minTemperature: Double? = null,

    @Column(name = "max_temperature")
    var maxTemperature: Double? = null,

    @Column(name = "require_humidity_tracking", nullable = false)
    var requireHumidityTracking: Boolean = false,

    @Column(name = "min_humidity")
    var minHumidity: Double? = null,

    @Column(name = "max_humidity")
    var maxHumidity: Double? = null,

    @Column(name = "require_location_tracking", nullable = false)
    var requireLocationTracking: Boolean = true,

    @Column(name = "special_requirements", length = 1000)
    var specialRequirements: String? = null,

    // Order Processing
    @Column(name = "shipment_id")
    var shipmentId: UUID? = null,

    @Column(name = "rejection_reason", length = 500)
    var rejectionReason: String? = null,

    @Column(name = "processed_at")
    var processedAt: Instant? = null

) : BaseEntity(id) {
    protected constructor() : this(
        id = UUID.randomUUID(),
        customerId = UUID.randomUUID(),
        providerId = UUID.randomUUID(),
        goodsDescription = "",
        pickupAddress = "",
        deliveryAddress = ""
    )

    override fun validateInvariants() {
        check(customerId != UUID(0, 0)) {
            "Customer ID must be valid"
        }

        check(providerId != UUID(0, 0)) {
            "Provider ID must be valid"
        }

        check(pickupAddress.isNotBlank() && pickupAddress.length <= 500) {
            "Pickup address must be 1-500 characters"
        }

        check(deliveryAddress.isNotBlank() && deliveryAddress.length <= 500) {
            "Delivery address must be 1-500 characters"
        }

        check(goodsDescription.isNotBlank() && goodsDescription.length <= 1000) {
            "Goods description must be 1-1000 characters"
        }

        if (requireTemperatureTracking) {
            minTemperature?.let { min ->
                maxTemperature?.let { max ->
                    check(min <= max) {
                        "Min temperature must be less than or equal to max temperature"
                    }
                }
            }
        }

        if (requireHumidityTracking) {
            minHumidity?.let { min ->
                check(min in 0.0..100.0) {
                    "Min humidity must be between 0 and 100"
                }
            }
            maxHumidity?.let { max ->
                check(max in 0.0..100.0) {
                    "Max humidity must be between 0 and 100"
                }
            }
            minHumidity?.let { min ->
                maxHumidity?.let { max ->
                    check(min <= max) {
                        "Min humidity must be less than or equal to max humidity"
                    }
                }
            }
        }

        when (status) {
            OrderStatus.ACCEPTED -> {
                check(shipmentId != null) {
                    "ACCEPTED order must have a shipment ID"
                }
                check(processedAt != null) {
                    "ACCEPTED order must have processed timestamp"
                }
            }
            OrderStatus.REJECTED -> {
                check(rejectionReason != null && rejectionReason!!.isNotBlank()) {
                    "REJECTED order must have a rejection reason"
                }
                check(processedAt != null) {
                    "REJECTED order must have processed timestamp"
                }
            }
            else -> {}
        }
    }

    companion object {
        fun create(
            customerId: UUID,
            providerId: UUID,
            goodsDescription: String,
            pickupAddress: String,
            deliveryAddress: String,
            estimatedDeliveryAt: Instant? = null,
            requireTemperatureTracking: Boolean = false,
            minTemperature: Double? = null,
            maxTemperature: Double? = null,
            requireHumidityTracking: Boolean = false,
            minHumidity: Double? = null,
            maxHumidity: Double? = null,
            requireLocationTracking: Boolean = true,
            specialRequirements: String? = null
        ): Order {
            require(customerId != UUID(0, 0)) { "Customer ID must be valid" }
            require(providerId != UUID(0, 0)) { "Provider ID must be valid" }
            require(goodsDescription.isNotBlank()) { "Goods description is required" }
            require(goodsDescription.length <= 1000) { "Goods description must be at most 1000 characters" }
            require(pickupAddress.isNotBlank()) { "Pickup address is required" }
            require(deliveryAddress.isNotBlank()) { "Delivery address is required" }

            val order = Order(
                id = UUID.randomUUID(),
                customerId = customerId,
                providerId = providerId,
                goodsDescription = goodsDescription.trim(),
                pickupAddress = pickupAddress.trim(),
                deliveryAddress = deliveryAddress.trim(),
                estimatedDeliveryAt = estimatedDeliveryAt,
                requireTemperatureTracking = requireTemperatureTracking,
                minTemperature = minTemperature,
                maxTemperature = maxTemperature,
                requireHumidityTracking = requireHumidityTracking,
                minHumidity = minHumidity,
                maxHumidity = maxHumidity,
                requireLocationTracking = requireLocationTracking,
                specialRequirements = specialRequirements?.trim(),
                status = OrderStatus.PENDING
            )

            order.validateInvariants()
            return order
        }
    }

    fun accept(shipmentId: UUID) {
        require(status == OrderStatus.PENDING) {
            "Only PENDING orders can be accepted. Current status: $status"
        }
        require(shipmentId != UUID(0, 0)) {
            "Shipment ID must be valid"
        }

        this.shipmentId = shipmentId
        this.status = OrderStatus.ACCEPTED
        this.processedAt = Instant.now()
    }

    fun reject(reason: String) {
        require(status == OrderStatus.PENDING) {
            "Only PENDING orders can be rejected. Current status: $status"
        }
        require(reason.isNotBlank()) {
            "Rejection reason is required"
        }
        require(reason.length <= 500) {
            "Rejection reason must be at most 500 characters"
        }

        this.rejectionReason = reason.trim()
        this.status = OrderStatus.REJECTED
        this.processedAt = Instant.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Order) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Order(id=$id, status=$status, customer=$customerId, provider=$providerId)"
    }
}
