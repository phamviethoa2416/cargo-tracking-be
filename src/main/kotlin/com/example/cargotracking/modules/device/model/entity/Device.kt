package com.example.cargotracking.modules.device.model.entity

import com.example.cargotracking.common.entity.BaseEntity
import com.example.cargotracking.modules.device.model.types.DeviceStatus
import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.util.UUID

class Device(
    id: UUID = UUID.randomUUID(),

    @Column(name = "id", unique = true, nullable = false, length = 255)
    val hardwareId: String,

    @Column(name = "device_name", length = 100)
    var deviceName: String? = null,

    @Column(name = "model", length = 50)
    var model: String? = null,

    @Column(name = "owner_shipper_id")
    var ownerShipperId: UUID? = null,

    @Column(name = "current_shipment_id")
    var currentShipmentId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: DeviceStatus = DeviceStatus.AVAILABLE,

    @Column(name = "firmware_version", length = 50)
    var firmwareVersion: String? = null,

    @Column(name = "total_trips", nullable = false)
    var totalTrips: Int = 0,



    ) : BaseEntity(id) {
}