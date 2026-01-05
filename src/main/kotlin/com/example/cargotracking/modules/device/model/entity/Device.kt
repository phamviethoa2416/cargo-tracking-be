package com.example.cargotracking.modules.device.model.entity

import com.example.cargotracking.common.entity.BaseEntity
import com.example.cargotracking.modules.device.model.types.DeviceStatus
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
    name = "devices",
    indexes = [
        Index(name = "idx_device_hardware_uid", columnList = "hardware_uid"),
        Index(name = "idx_device_provider_id", columnList = "provider_id"),
        Index(name = "idx_device_status", columnList = "status")
    ]
)
class Device private constructor(
    id: UUID,

    @Column(name = "hardware_uid", nullable = false, unique = true, length = 255)
    var hardwareUID: String,

    @Column(name = "provider_id", nullable = false)
    var providerId: UUID,

    @Column(name = "device_name", length = 100)
    var deviceName: String? = null,

    @Column(name = "model", length = 50)
    var model: String? = null,

    @Column(name = "current_shipment_id")
    var currentShipmentId: UUID? = null,

    @Column(name = "battery_level")
    var batteryLevel: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: DeviceStatus = DeviceStatus.AVAILABLE,

    @Column(name = "firmware_version", length = 50)
    var firmwareVersion: String? = null,

    @Column(name = "total_trips", nullable = false)
    var totalTrips: Int = 0,

    @Column(name = "last_seen_at")
    var lastSeenAt: Instant? = null

) : BaseEntity(id) {
    protected constructor() : this(
        id = UUID.randomUUID(),
        hardwareUID = "",
        providerId = UUID.randomUUID()
    )

    override fun validateInvariants() {
        check(hardwareUID.isNotBlank()) {
            "Hardware UID must not be blank"
        }

        check(totalTrips >= 0) {
            "Total trips must be non-negative, got: $totalTrips"
        }

        batteryLevel?.let { level ->
            check(level in 0..100) {
                "Battery level must be 0-100, got: $level"
            }
        }

        if (status == DeviceStatus.IN_TRANSIT) {
            check(currentShipmentId != null) {
                "IN_TRANSIT device must have shipment ID"
            }
        }
    }

    companion object {

        fun create(
            hardwareUID: String,
            providerId: UUID,
            deviceName: String? = null,
            model: String? = null
        ): Device {
            require(hardwareUID.isNotBlank()) {
                "Hardware UID must not be blank"
            }

            val device = Device(
                id = UUID.randomUUID(),
                hardwareUID = hardwareUID,
                providerId = providerId,
                deviceName = deviceName,
                model = model,
                status = DeviceStatus.AVAILABLE,
                totalTrips = 0
            )

            device.validateInvariants()
            return device
        }
    }

    fun assignToShipment(shipmentId: UUID) {
        require(status == DeviceStatus.AVAILABLE) {
            "Only AVAILABLE device can be assigned to shipment. Current status: $status"
        }
        require(shipmentId != UUID(0, 0)) {
            "Shipment ID must be valid"
        }

        currentShipmentId = shipmentId
        status = DeviceStatus.IN_TRANSIT
    }

    fun releaseFromShipment() {
        require(status == DeviceStatus.IN_TRANSIT) {
            "Only IN_TRANSIT device can be released. Current status: $status"
        }
        currentShipmentId = null
        status = DeviceStatus.AVAILABLE
        totalTrips += 1
    }

    fun retire() {
        require(status != DeviceStatus.RETIRED) {
            "Device is already retired"
        }

        currentShipmentId = null
        status = DeviceStatus.RETIRED
    }

    fun updateBatteryLevel(level: Int?) {
        level?.let {
            require(it in 0..100) {
                "Battery level must be between 0 and 100, got: $it"
            }
        }
        batteryLevel = level
    }

    fun updateFirmware(version: String?) {
        version?.let {
            require(it.isNotBlank() && it.length <= 50) {
                "Firmware version must be non-blank and at most 50 characters"
            }
        }
        firmwareVersion = version
    }

    fun updateDeviceName(name: String?) {
        name?.let {
            require(it.isNotBlank() && it.length <= 100) {
                "Device name must be non-blank and at most 100 characters"
            }
        }
        deviceName = name
    }

    fun updateModel(newModel: String?) {
        newModel?.let {
            require(it.isNotBlank() && it.length <= 50) {
                "Model must be non-blank and at most 50 characters"
            }
        }
        model = newModel
    }

    fun updateLastSeenAt(timestamp: Instant) {
        lastSeenAt = timestamp
    }

    fun isOnline(thresholdSeconds: Long = 300): Boolean {
        return lastSeenAt?.let {
            Instant.now().minusSeconds(thresholdSeconds).isBefore(it)
        } == true
    }

    fun updateStatus(newStatus: DeviceStatus, shipmentId: UUID? = null) {
        when (newStatus) {
            DeviceStatus.IN_TRANSIT -> {
                require(shipmentId != null) {
                    "Shipment ID is required when setting status to IN_TRANSIT"
                }
                require(shipmentId != UUID(0, 0)) {
                    "Shipment ID must be valid"
                }
                require(status == DeviceStatus.AVAILABLE) {
                    "Only AVAILABLE device can be set to IN_TRANSIT. Current status: $status"
                }
                currentShipmentId = shipmentId
                status = newStatus
            }
            DeviceStatus.AVAILABLE -> {
                require(status == DeviceStatus.IN_TRANSIT) {
                    "Only IN_TRANSIT device can be set to AVAILABLE. Current status: $status"
                }
                currentShipmentId = null
                status = newStatus
                totalTrips += 1
            }
            DeviceStatus.MAINTENANCE -> {
                require(status != DeviceStatus.IN_TRANSIT) {
                    "Cannot set IN_TRANSIT device to MAINTENANCE. Release from shipment first."
                }
                status = newStatus
            }
            DeviceStatus.RETIRED -> {
                require(status != DeviceStatus.IN_TRANSIT) {
                    "Cannot set IN_TRANSIT device to RETIRED. Release from shipment first."
                }
                currentShipmentId = null
                status = newStatus
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Device) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Device(id=$id, hardwareUID='$hardwareUID', status=$status, provider=$providerId)"
    }
}