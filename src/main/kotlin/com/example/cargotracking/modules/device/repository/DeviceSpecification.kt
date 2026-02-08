package com.example.cargotracking.modules.device.repository

import com.example.cargotracking.modules.device.model.entity.Device
import com.example.cargotracking.modules.device.model.types.DeviceStatus
import org.springframework.data.jpa.domain.Specification
import java.time.Instant
import java.util.UUID

object DeviceSpecification {

    fun withProviderId(providerId: UUID?): Specification<Device> = Specification { root, _, cb ->
        providerId?.let { cb.equal(root.get<UUID>("providerId"), it) } ?: cb.conjunction()
    }

    fun withStatus(status: DeviceStatus?): Specification<Device> = Specification { root, _, cb ->
        status?.let { cb.equal(root.get<DeviceStatus>("status"), it) } ?: cb.conjunction()
    }

    fun withCurrentShipmentId(shipmentId: UUID?): Specification<Device> = Specification { root, _, cb ->
        shipmentId?.let { cb.equal(root.get<UUID>("currentShipmentId"), it) } ?: cb.conjunction()
    }

    fun lastSeenBefore(threshold: Instant?): Specification<Device> = Specification { root, _, cb ->
        threshold?.let {
            val lastSeenPath = root.get<Instant>("lastSeenAt")
            cb.or(
                cb.isNull(lastSeenPath),
                cb.lessThan(lastSeenPath, it)
            )
        } ?: cb.conjunction()
    }

    fun lastSeenAfter(threshold: Instant?): Specification<Device> = Specification { root, _, cb ->
        threshold?.let {
            val lastSeenPath = root.get<Instant>("lastSeenAt")
            cb.and(
                cb.isNotNull(lastSeenPath),
                cb.greaterThanOrEqualTo(lastSeenPath, it)
            )
        } ?: cb.conjunction()
    }

    fun minBattery(min: Int?): Specification<Device> = Specification { root, _, cb ->
        min?.let {
            val batteryPath = root.get<Int>("batteryLevel")
            cb.or(
                cb.isNull(batteryPath),
                cb.greaterThanOrEqualTo(batteryPath, it)
            )
        } ?: cb.conjunction()
    }

    fun maxBattery(max: Int?): Specification<Device> = Specification { root, _, cb ->
        max?.let {
            val batteryPath = root.get<Int>("batteryLevel")
            cb.or(
                cb.isNull(batteryPath),
                cb.lessThanOrEqualTo(batteryPath, it)
            )
        } ?: cb.conjunction()
    }

    fun withSearchText(search: String?): Specification<Device> = Specification { root, _, cb ->
        if (search.isNullOrBlank()) {
            cb.conjunction()
        } else {
            val pattern = "%${search.lowercase()}%"
            val deviceNamePath = root.get<String>("deviceName")
            val hardwareUidPath = root.get<String>("hardwareUID")
            cb.or(
                cb.like(cb.lower(deviceNamePath), pattern),
                cb.like(cb.lower(hardwareUidPath), pattern)
            )
        }
    }

    fun buildSpecification(
        providerId: UUID? = null,
        status: DeviceStatus? = null,
        currentShipmentId: UUID? = null,
        lastSeenBefore: Instant? = null,
        lastSeenAfter: Instant? = null,
        minBattery: Int? = null,
        maxBattery: Int? = null,
        search: String? = null
    ): Specification<Device> {
        return withProviderId(providerId)
            .and(withStatus(status))
            .and(withCurrentShipmentId(currentShipmentId))
            .and(lastSeenBefore(lastSeenBefore))
            .and(lastSeenAfter(lastSeenAfter))
            .and(minBattery(minBattery))
            .and(maxBattery(maxBattery))
            .and(withSearchText(search))
    }
}