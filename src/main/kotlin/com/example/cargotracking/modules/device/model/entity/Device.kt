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
    private var _hardwareUID: String,

    @Column(name = "provider_id", nullable = false)
    private var _providerId: UUID,

    @Column(name = "device_name", length = 100)
    private var _deviceName: String? = null,

    @Column(name = "model", length = 50)
    private var _model: String? = null,

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
    protected constructor() : this(
        id = UUID.randomUUID(),
        _hardwareUID = "",
        _providerId = UUID.randomUUID()
    )

    val hardwareUID: String get() = _hardwareUID
    val providerId: UUID get() = _providerId
    val deviceName: String? get() = _deviceName
    val model: String? get() = _model
    val currentShipmentId: UUID? get() = _currentShipmentId
    val batteryLevel: Int? get() = _batteryLevel
    val status: DeviceStatus get() = _status
    val firmwareVersion: String? get() = _firmwareVersion
    val totalTrips: Int get() = _totalTrips
    val lastSeenAt: Instant? get() = _lastSeenAt

    override fun validateInvariants() {
        check(_hardwareUID.isNotBlank()) {
            "Hardware UID must not be blank"
        }

        check(_totalTrips >= 0) {
            "Total trips must be non-negative, got: $_totalTrips"
        }

        _batteryLevel?.let { level ->
            check(level in 0..100) {
                "Battery level must be 0-100, got: $level"
            }
        }

        if (_status == DeviceStatus.IN_TRANSIT) {
            check(_currentShipmentId != null) {
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
                _hardwareUID = hardwareUID,
                _providerId = providerId,
                _deviceName = deviceName,
                _model = model,
                _status = DeviceStatus.AVAILABLE,
                _totalTrips = 0
            )

            device.validateInvariants()
            return device
        }
    }

    fun assignToShipment(shipmentId: UUID) {
        require(_status == DeviceStatus.AVAILABLE) {
            "Only AVAILABLE device can be assigned to shipment. Current status: $_status"
        }
        require(shipmentId != UUID(0, 0)) {
            "Shipment ID must be valid"
        }

        _currentShipmentId = shipmentId
        _status = DeviceStatus.IN_TRANSIT
    }

    fun releaseFromShipment() {
        require(_status == DeviceStatus.IN_TRANSIT) {
            "Only IN_TRANSIT device can be released. Current status: $_status"
        }
        _currentShipmentId = null
        _status = DeviceStatus.AVAILABLE
        _totalTrips += 1
    }

    fun retire() {
        require(_status != DeviceStatus.RETIRED) {
            "Device is already retired"
        }

        _currentShipmentId = null
        _status = DeviceStatus.RETIRED
    }

    fun updateBatteryLevel(level: Int?) {
        level?.let {
            require(it in 0..100) {
                "Battery level must be between 0 and 100, got: $it"
            }
        }
        _batteryLevel = level
    }

    fun updateFirmware(version: String?) {
        version?.let {
            require(it.isNotBlank() && it.length <= 50) {
                "Firmware version must be non-blank and at most 50 characters"
            }
        }
        _firmwareVersion = version
    }

    fun updateDeviceName(name: String?) {
        name?.let {
            require(it.isNotBlank() && it.length <= 100) {
                "Device name must be non-blank and at most 100 characters"
            }
        }
        _deviceName = name
    }

    fun updateModel(model: String?) {
        model?.let {
            require(it.isNotBlank() && it.length <= 50) {
                "Model must be non-blank and at most 50 characters"
            }
        }
        _model = model
    }

    fun isOnline(thresholdSeconds: Long = 300): Boolean {
        return _lastSeenAt?.let {
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
                require(_status == DeviceStatus.AVAILABLE) {
                    "Only AVAILABLE device can be set to IN_TRANSIT. Current status: $_status"
                }
                _currentShipmentId = shipmentId
                _status = newStatus
            }
            DeviceStatus.AVAILABLE -> {
                require(_status == DeviceStatus.IN_TRANSIT) {
                    "Only IN_TRANSIT device can be set to AVAILABLE. Current status: $_status"
                }
                _currentShipmentId = null
                _status = newStatus
                _totalTrips += 1
            }
            DeviceStatus.MAINTENANCE -> {
                require(_status != DeviceStatus.IN_TRANSIT) {
                    "Cannot set IN_TRANSIT device to MAINTENANCE. Release from shipment first."
                }
                _status = newStatus
            }
            DeviceStatus.RETIRED -> {
                require(_status != DeviceStatus.IN_TRANSIT) {
                    "Cannot set IN_TRANSIT device to RETIRED. Release from shipment first."
                }
                _currentShipmentId = null
                _status = newStatus
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
        return "Device(id=$id, hardwareUID='$_hardwareUID', status=$_status, provider=$_providerId)"
    }
}