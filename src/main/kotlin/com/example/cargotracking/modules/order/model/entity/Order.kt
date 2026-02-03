package com.example.cargotracking.modules.order.model.entity

import com.example.cargotracking.common.entity.BaseEntity
import com.example.cargotracking.modules.order.model.types.OrderStatus
import com.example.cargotracking.modules.user.model.types.UserRole
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
        id = UUID(0, 0),
        customerId = UUID(0, 0),
        providerId = UUID(0, 0),
        goodsDescription = "",
        pickupAddress = "",
        deliveryAddress = ""
    )

    override fun validateInvariants() {
        if (requireTemperatureTracking) {
            check(minTemperature != null && maxTemperature != null) {
                "Temperature tracking requires min and max temperature"
            }
            check(minTemperature!! <= maxTemperature!!) {
                "Min temperature must be <= max temperature"
            }
        }

        if (requireHumidityTracking) {
            check(minHumidity != null && maxHumidity != null) {
                "Humidity tracking requires min and max humidity"
            }
            check(minHumidity!! in 0.0..100.0 && maxHumidity!! in 0.0..100.0) {
                "Humidity must be between 0 and 100"
            }
            check(minHumidity!! <= maxHumidity!!) {
                "Min humidity must be <= max humidity"
            }
        }

        when (status) {
            OrderStatus.ACCEPTED -> {
                check(shipmentId != null) {
                    "Accepted order must have shipmentId"
                }
                check(processedAt != null) {
                    "Accepted order must have processedAt"
                }
            }

            OrderStatus.REJECTED -> {
                check(!rejectionReason.isNullOrBlank()) {
                    "Rejected order must have rejection reason"
                }
                check(processedAt != null) {
                    "Rejected order must have processedAt"
                }
            }

            OrderStatus.IN_PROGRESS -> {
                check(shipmentId != null) {
                    "In progress order must have shipmentId"
                }
            }

            OrderStatus.COMPLETED -> {
                check(shipmentId != null) {
                    "Completed order must have shipmentId"
                }
            }

            OrderStatus.CANCELLED -> {
                check(!rejectionReason.isNullOrBlank()) {
                    "Cancelled order must have cancellation reason"
                }
            }

            OrderStatus.PENDING -> Unit
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
        validateInvariants()
    }

    fun reject(reason: String) {
        require(status == OrderStatus.PENDING) {
            "Only PENDING orders can be rejected. Current status: $status"
        }
        require(reason.isNotBlank()) {
            "Rejection reason is required"
        }
        require(reason.length in 5..500) {
            "Rejection reason must be 5-500 characters"
        }

        this.rejectionReason = reason.trim()
        this.status = OrderStatus.REJECTED
        this.processedAt = Instant.now()
        validateInvariants()
    }
    
    fun canBeAcceptedBy(providerId: UUID): Boolean {
        return status == OrderStatus.PENDING && this.providerId == providerId
    }
    
    fun canBeRejectedBy(providerId: UUID): Boolean {
        return status == OrderStatus.PENDING && this.providerId == providerId
    }
    
    fun canBeCancelledBy(userId: UUID, userRole: UserRole): Boolean {
        if (status in listOf(OrderStatus.REJECTED, OrderStatus.COMPLETED, OrderStatus.CANCELLED)) {
            return false
        }
        return when (userRole) {
            UserRole.CUSTOMER -> customerId == userId
            UserRole.PROVIDER -> providerId == userId
            else -> false
        }
    }
    
    fun markInProgress() {
        require(status == OrderStatus.ACCEPTED) {
            "Only ACCEPTED orders can be marked as IN_PROGRESS. Current status: $status"
        }
        require(shipmentId != null) {
            "Order must have shipmentId to be marked as IN_PROGRESS"
        }
        
        this.status = OrderStatus.IN_PROGRESS
        validateInvariants()
    }
    
    fun markCompleted() {
        require(status == OrderStatus.IN_PROGRESS) {
            "Only IN_PROGRESS orders can be marked as COMPLETED. Current status: $status"
        }
        require(shipmentId != null) {
            "Order must have shipmentId to be marked as COMPLETED"
        }
        
        this.status = OrderStatus.COMPLETED
        validateInvariants()
    }
    
    fun cancel(reason: String) {
        require(status in listOf(OrderStatus.PENDING, OrderStatus.ACCEPTED, OrderStatus.IN_PROGRESS)) {
            "Cannot cancel order in status: $status"
        }
        require(reason.isNotBlank()) {
            "Cancellation reason is required"
        }
        require(reason.length in 10..500) {
            "Cancellation reason must be 10-500 characters"
        }
        
        this.rejectionReason = reason.trim()
        this.status = OrderStatus.CANCELLED
        this.processedAt = Instant.now()
        validateInvariants()
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
