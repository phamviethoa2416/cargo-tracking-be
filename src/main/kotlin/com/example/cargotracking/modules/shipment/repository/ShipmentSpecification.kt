package com.example.cargotracking.modules.shipment.repository

import com.example.cargotracking.modules.shipment.model.entity.Shipment
import com.example.cargotracking.modules.shipment.model.types.ShipmentStatus
import org.springframework.data.jpa.domain.Specification
import java.time.Instant
import java.util.*

object ShipmentSpecification {

    fun withStatus(status: ShipmentStatus?): Specification<Shipment> = Specification { root, _, cb ->
        status?.let { cb.equal(root.get<ShipmentStatus>("status"), it) } ?: cb.conjunction()
    }

    fun withCustomerId(customerId: UUID?): Specification<Shipment> = Specification { root, _, cb ->
        customerId?.let { cb.equal(root.get<UUID>("customerId"), it) } ?: cb.conjunction()
    }

    fun withProviderId(providerId: UUID?): Specification<Shipment> = Specification { root, _, cb ->
        providerId?.let { cb.equal(root.get<UUID>("providerId"), it) } ?: cb.conjunction()
    }

    fun withShipperId(shipperId: UUID?): Specification<Shipment> = Specification { root, _, cb ->
        shipperId?.let { cb.equal(root.get<UUID>("shipperId"), it) } ?: cb.conjunction()
    }

    fun withDeviceId(deviceId: UUID?): Specification<Shipment> = Specification { root, _, cb ->
        deviceId?.let { cb.equal(root.get<UUID>("deviceId"), it) } ?: cb.conjunction()
    }

    fun createdAfter(createdAfter: Instant?): Specification<Shipment> = Specification { root, _, cb ->
        createdAfter?.let {
            val createdAtPath = root.get<Instant>("createdAt")
            cb.and(
                cb.isNotNull(createdAtPath),
                cb.greaterThanOrEqualTo(createdAtPath, it)
            )
        } ?: cb.conjunction()
    }

    fun createdBefore(createdBefore: Instant?): Specification<Shipment> = Specification { root, _, cb ->
        createdBefore?.let {
            val createdAtPath = root.get<Instant>("createdAt")
            cb.and(
                cb.isNotNull(createdAtPath),
                cb.lessThanOrEqualTo(createdAtPath, it)
            )
        } ?: cb.conjunction()
    }

    fun estimatedDeliveryAfter(deliveryAfter: Instant?): Specification<Shipment> = Specification { root, _, cb ->
        deliveryAfter?.let {
            val estimatedDeliveryPath = root.get<Instant>("estimatedDeliveryAt")
            cb.and(
                cb.isNotNull(estimatedDeliveryPath),
                cb.greaterThanOrEqualTo(estimatedDeliveryPath, it)
            )
        } ?: cb.conjunction()
    }

    fun estimatedDeliveryBefore(deliveryBefore: Instant?): Specification<Shipment> = Specification { root, _, cb ->
        deliveryBefore?.let {
            val estimatedDeliveryPath = root.get<Instant>("estimatedDeliveryAt")
            cb.and(
                cb.isNotNull(estimatedDeliveryPath),
                cb.lessThanOrEqualTo(estimatedDeliveryPath, it)
            )
        } ?: cb.conjunction()
    }

    fun withSearchText(search: String?): Specification<Shipment> = Specification { root, _, cb ->
        if (search.isNullOrBlank()) {
            cb.conjunction()
        } else {
            val searchPattern = "%${search.lowercase()}%"
            val goodsDescPath = root.get<String>("goodsDescription")
            val pickupAddrPath = root.get<String>("pickupAddress")
            val deliveryAddrPath = root.get<String>("deliveryAddress")

            cb.or(
                cb.like(cb.lower(goodsDescPath), searchPattern),
                cb.like(cb.lower(pickupAddrPath), searchPattern),
                cb.like(cb.lower(deliveryAddrPath), searchPattern)
            )
        }
    }

    fun buildSpecification(
        status: ShipmentStatus? = null,
        customerId: UUID? = null,
        providerId: UUID? = null,
        shipperId: UUID? = null,
        deviceId: UUID? = null,
        createdAfter: Instant? = null,
        createdBefore: Instant? = null,
        deliveryAfter: Instant? = null,
        deliveryBefore: Instant? = null,
        search: String? = null
    ): Specification<Shipment> {
        return withStatus(status)
            .and(withCustomerId(customerId))
            .and(withProviderId(providerId))
            .and(withShipperId(shipperId))
            .and(withDeviceId(deviceId))
            .and(createdAfter(createdAfter))
            .and(createdBefore(createdBefore))
            .and(estimatedDeliveryAfter(deliveryAfter))
            .and(estimatedDeliveryBefore(deliveryBefore))
            .and(withSearchText(search))
    }
}
