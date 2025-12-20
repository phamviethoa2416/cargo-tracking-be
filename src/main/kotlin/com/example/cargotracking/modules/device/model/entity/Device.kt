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
class Device(
    id: UUID = UUID.randomUUID(),

    @Column(name = "hardware_uid", unique = true, nullable = false, length = 255)
    val hardwareUID: String,

    @Column(name = "device_name", length = 100)
    val deviceName: String? = null,

    @Column(name = "model", length = 50)
    val model: String? = null,

    @Column(name = "provider_id", nullable = false)
    val providerId: UUID,

    @Column(name = "current_shipment_id")
    private var _currentShipmentId: UUID? = null,

    @Column(name = "battery_level")
    private var _batteryLevel: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private var _status: DeviceStatus = DeviceStatus.AVAILABLE,

    @Column(name = "firmware_version", length = 50)
    private var _firmwareVersion: String? = null,

    @Column(name = "total_trips", nullable = false)
    private var _totalTrips: Int = 0,

    @Column(name = "last_seen_at")
    private var _lastSeenAt: Instant? = null
) : BaseEntity(id) {
    val currentShipmentId: UUID? get() = _currentShipmentId
    val batteryLevel: Int? get() = _batteryLevel
    val status: DeviceStatus get() = _status
    val firmwareVersion: String? get() = _firmwareVersion
    val totalTrips: Int get() = _totalTrips
    val lastSeenAt: Instant? get() = _lastSeenAt

    protected constructor() : this(
        id = UUID.randomUUID(),
        hardwareUID = "",
        providerId = UUID.randomUUID()
    )

    init {
        validate()
    }

    override fun validate() {
        require(hardwareUID.isNotBlank()) {
            "Hardware UID must not be blank"
        }

        require(_totalTrips >= 0) {
            "Total trips must be a non-negative number."
        }

        _batteryLevel?.let {
            require(it in 0..100) {
                "Battery level must be between 0 and 100."
            }
        }

        if (_status == DeviceStatus.IN_TRANSIT) {
            require(_currentShipmentId != null) {
                "Device in transit must have a current shipment ID."
            }
        }
    }

    fun assignToShipment(shipmentId: UUID) {
        require(_status == DeviceStatus.AVAILABLE) {
            "Only AVAILABLE device can be assigned to shipment. Current status: $_status"
        }
        _currentShipmentId = shipmentId
        _status = DeviceStatus.IN_TRANSIT
        validate()
    }

    fun releaseFromShipment() {
        require(_status == DeviceStatus.IN_TRANSIT) {
            "Only IN_TRANSIT device can be released. Current status: $_status"
        }
        _currentShipmentId = null
        _status = DeviceStatus.AVAILABLE
        _totalTrips += 1
        validate()
    }

    fun updateStatus(newStatus: DeviceStatus, shipmentId: UUID? = null) {
        when (newStatus) {
            DeviceStatus.IN_TRANSIT -> {
                require(shipmentId != null) {
                    "Shipment ID is required when setting status to IN_TRANSIT"
                }
                _currentShipmentId = shipmentId
            }
            DeviceStatus.AVAILABLE -> {
                // If transitioning from IN_TRANSIT to AVAILABLE, clear shipment and increment trips
                if (_status == DeviceStatus.IN_TRANSIT) {
                    _currentShipmentId = null
                    _totalTrips += 1
                }
            }
            DeviceStatus.MAINTENANCE, DeviceStatus.RETIRED -> {
                // Clear shipment ID when moving to maintenance or retired
                if (_status == DeviceStatus.IN_TRANSIT) {
                    _currentShipmentId = null
                }
            }
        }
        _status = newStatus
        validate()
    }

    fun updateFirmware(version: String?) {
        version?.let {
            require(it.isNotBlank() && it.length <= 50) {
                "Firmware version must be non-blank and not exceed 50 characters."
            }
        }
        _firmwareVersion = version
    }

    fun updateBatteryLevel(level: Int?) {
        level?.let {
            require(it in 0..100) {
                "Battery level must be between 0 and 100."
            }
        }
        _batteryLevel = level
    }

    fun updateLastSeen(timestamp: Instant) {
        _lastSeenAt = timestamp
    }

    fun recordHeartbeat() {
        _lastSeenAt = Instant.now()
    }

    fun isOnline(): Boolean {
        return lastSeenAt?.let {
            Instant.now().minusSeconds(5 * 60).isBefore(it)
        } == true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Device) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Device(id=$id, hardwareId='$hardwareUID', deviceName=$deviceName, status=$status)"
    }
}