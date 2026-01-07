package com.example.cargotracking.modules.shipment.model.entity

import com.example.cargotracking.common.entity.BaseEntity
import com.example.cargotracking.modules.shipment.model.types.ShipmentStatus
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
    name = "shipments",
    indexes = [
        Index(name = "idx_shipment_customer_id", columnList = "customer_id"),
        Index(name = "idx_shipment_provider_id", columnList = "provider_id"),
        Index(name = "idx_shipment_shipper_id", columnList = "shipper_id"),
        Index(name = "idx_shipment_device_id", columnList = "device_id"),
        Index(name = "idx_shipment_status", columnList = "status")
    ]
)
class Shipment private constructor(
    id: UUID,

    @Column(name = "customer_id", nullable = false)
    var customerId: UUID,

    @Column(name = "provider_id", nullable = false)
    var providerId: UUID,

    @Column(name = "shipper_id")
    var shipperId: UUID? = null,

    @Column(name = "device_id")
    var deviceId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ShipmentStatus = ShipmentStatus.PENDING,

    @Column(name = "goods_description", nullable = false, length = 1000)
    var goodsDescription: String,

    @Column(name = "pickup_address", nullable = false, length = 500)
    var pickupAddress: String,

    @Column(name = "delivery_address", nullable = false, length = 500)
    var deliveryAddress: String,

    @Column(name = "estimated_delivery_at")
    var estimatedDeliveryAt: Instant? = null,

    @Column(name = "actual_delivery_at")
    var actualDeliveryAt: Instant? = null

): BaseEntity(id) {
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

        check(goodsDescription.isNotBlank() && goodsDescription.length <= 1000) {
            "Goods description must be 1-1000 characters"
        }

        check(pickupAddress.isNotBlank() && pickupAddress.length <= 500) {
            "Pickup address must be 1-500 characters"
        }

        check(deliveryAddress.isNotBlank() && deliveryAddress.length <= 500) {
            "Delivery address must be 1-500 characters"
        }

        when (status) {
            ShipmentStatus.ASSIGNED -> {
                check(shipperId != null) {
                    "ASSIGNED shipment must have shipper"
                }
            }
            ShipmentStatus.IN_TRANSIT -> {
                check(shipperId != null) {
                    "IN_TRANSIT shipment must have shipper"
                }
                check(deviceId != null) {
                    "IN_TRANSIT shipment must have device"
                }
            }
            ShipmentStatus.COMPLETED -> {
                check(actualDeliveryAt != null) {
                    "COMPLETED shipment must have actual delivery time"
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
        ): Shipment {
            require(customerId != UUID(0, 0)) { "Customer ID must be valid" }
            require(providerId != UUID(0, 0)) { "Provider ID must be valid" }
            require(goodsDescription.isNotBlank()) { "Goods description is required" }
            require(pickupAddress.isNotBlank()) { "Pickup address is required" }
            require(deliveryAddress.isNotBlank()) { "Delivery address is required" }

            val shipment = Shipment(
                id = UUID.randomUUID(),
                customerId = customerId,
                providerId = providerId,
                goodsDescription = goodsDescription.trim(),
                pickupAddress = pickupAddress.trim(),
                deliveryAddress = deliveryAddress.trim(),
                estimatedDeliveryAt = estimatedDeliveryAt,
                status = ShipmentStatus.PENDING
            )

            shipment.validateInvariants()
            return shipment
        }
    }

    fun assignShipper(newShipperId: UUID) {
        require(status == ShipmentStatus.PENDING) {
            "Only PENDING shipments can be assigned. Current status: $status"
        }
        require(newShipperId != UUID(0, 0)) { "Shipper ID must be valid" }

        shipperId = newShipperId
        status = ShipmentStatus.ASSIGNED
    }

    fun assignDevice(newDeviceId: UUID) {
        require(status == ShipmentStatus.ASSIGNED) {
            "Only ASSIGNED shipments can have device assigned. Current status: $status"
        }
        require(newDeviceId != UUID(0, 0)) {
            "Device ID must be valid"
        }
        deviceId = newDeviceId
    }

    fun startTransit() {
        require(status == ShipmentStatus.ASSIGNED) {
            "Only ASSIGNED shipments can start transit. Current status: $status"
        }
        require(deviceId != null) {
            "Shipment must have a device assigned before starting transit"
        }
        status = ShipmentStatus.IN_TRANSIT
    }

    fun unassignShipper() {
        require(status == ShipmentStatus.ASSIGNED) {
            "Only ASSIGNED shipments can be unassigned"
        }
        shipperId = null
        status = ShipmentStatus.PENDING
    }


    fun complete(deliveredAt: Instant = Instant.now()) {
        require(status == ShipmentStatus.IN_TRANSIT) {
            "Only IN_TRANSIT shipment can be completed"
        }
        actualDeliveryAt = deliveredAt
        status = ShipmentStatus.COMPLETED
    }

    fun cancel() {
        require(status in listOf(
            ShipmentStatus.PENDING,
            ShipmentStatus.ASSIGNED,
            ShipmentStatus.IN_TRANSIT
        )) {
            "Cannot cancel shipment in status: $status"
        }

        status = ShipmentStatus.CANCELLED
    }

    fun updateGoodsDescription(description: String) {
        require(description.isNotBlank() && description.length <= 1000) {
            "Goods description must be 1-1000 characters"
        }
        require(status == ShipmentStatus.PENDING) {
            "Only PENDING shipments can be updated. Current status: $status"
        }
        goodsDescription = description.trim()
    }

    fun updatePickupAddress(address: String) {
        require(address.isNotBlank() && address.length <= 500) {
            "Pickup address must be 1-500 characters"
        }
        require(status == ShipmentStatus.PENDING) {
            "Only PENDING shipments can be updated. Current status: $status"
        }
        pickupAddress = address.trim()
    }

    fun updateDeliveryAddress(address: String) {
        require(address.isNotBlank() && address.length <= 500) {
            "Delivery address must be 1-500 characters"
        }
        require(status == ShipmentStatus.PENDING) {
            "Only PENDING shipments can be updated. Current status: $status"
        }
        deliveryAddress = address.trim()
    }

    fun updateEstimatedDeliveryAt(newEstimatedDeliveryAt: Instant?) {
        require(status == ShipmentStatus.PENDING) {
            "Only PENDING shipments can be updated. Current status: $status"
        }
        estimatedDeliveryAt = newEstimatedDeliveryAt
    }

    fun updateDetails(
        goodsDescription: String? = null,
        pickupAddress: String? = null,
        deliveryAddress: String? = null,
        estimatedDeliveryAt: Instant? = null
    ) {
        goodsDescription?.let { updateGoodsDescription(it) }
        pickupAddress?.let { updatePickupAddress(it) }
        deliveryAddress?.let { updateDeliveryAddress(it) }
        estimatedDeliveryAt?.let { updateEstimatedDeliveryAt(it) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Shipment) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Shipment(id=$id, status=$status, customer=$customerId, provider=$providerId)"
    }
}