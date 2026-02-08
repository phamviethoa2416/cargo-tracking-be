package com.example.cargotracking.modules.device.model.dto.response

import com.example.cargotracking.modules.device.model.entity.Device
import com.example.cargotracking.modules.device.model.types.DeviceStatus
import java.time.Instant
import java.util.UUID

data class DeviceResponse(
    val id: UUID,
    val hardwareUid: String,
    val deviceName: String?,
    val model: String?,
    val providerId: UUID,
    val currentShipmentId: UUID?,
    val status: DeviceStatus,
    val firmwareVersion: String?,
    val batteryLevel: Int?,
    val totalTrips: Int,
    val lastSeenAt: Instant?,
    val isOnline: Boolean,
    val createdAt: Instant?,
    val updatedAt: Instant?
) {
    companion object {
        fun from(device: Device): DeviceResponse {
            return DeviceResponse(
                id = device.id,
                hardwareUid = device.hardwareUID,
                deviceName = device.deviceName,
                model = device.model,
                providerId = device.providerId,
                currentShipmentId = device.currentShipmentId,
                status = device.status,
                firmwareVersion = device.firmwareVersion,
                batteryLevel = device.batteryLevel,
                totalTrips = device.totalTrips,
                lastSeenAt = device.lastSeenAt,
                isOnline = device.isOnline(),
                createdAt = device.createdAt,
                updatedAt = device.updatedAt
            )
        }
    }
}

data class DeviceListResponse(
    val devices: List<DeviceResponse>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)
