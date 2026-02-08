package com.example.cargotracking.modules.device.service

import com.example.cargotracking.common.client.ingestion.IngestionClient
import com.example.cargotracking.modules.device.model.dto.response.*
import com.example.cargotracking.modules.device.repository.DeviceRepository
import com.example.cargotracking.modules.shipment.repository.ShipmentRepository
import com.example.cargotracking.modules.shipment.repository.ShipmentSpecification
import com.example.cargotracking.modules.user.model.types.UserRole
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class TelemetryService(
    private val ingestionClient: IngestionClient,
    private val deviceRepository: DeviceRepository,
    private val shipmentRepository: ShipmentRepository
) {
    private val logger = LoggerFactory.getLogger(TelemetryService::class.java)

    fun getLatestLocation(
        deviceId: UUID,
        userId: UUID,
        userRole: UserRole,
        authToken: String
    ): LocationResponse? {
        findDeviceWithAccess(deviceId, userId, userRole)
            ?: throw IllegalArgumentException("Device not found or access denied")
        return ingestionClient.getLatestLocation(deviceId, authToken)
    }

    fun getLocationHistory(
        deviceId: UUID,
        startTime: Instant?,
        endTime: Instant?,
        limit: Int?,
        offset: Int?,
        userId: UUID,
        userRole: UserRole,
        authToken: String
    ): LocationHistoryResponse? {
        findDeviceWithAccess(deviceId, userId, userRole)
            ?: throw IllegalArgumentException("Device not found or access denied")
        return ingestionClient.getLocationHistory(deviceId, startTime, endTime, limit, offset, authToken)
    }

    fun getLatestTelemetry(
        deviceId: UUID,
        userId: UUID,
        userRole: UserRole,
        authToken: String
    ): TelemetryResponse? {
        val device = findDeviceWithAccess(deviceId, userId, userRole)
            ?: throw IllegalArgumentException("Device not found or access denied")

        val telemetry = ingestionClient.getLatestTelemetry(deviceId, authToken)

        if (telemetry == null) {
            logger.warn("Ingestion returned null telemetry for device {}, using fallback from device record", deviceId)
            return TelemetryResponse(
                deviceId = device.id,
                time = device.lastSeenAt ?: Instant.now(),
                temperature = null,
                humidity = null,
                co2 = null,
                light = null,
                latitude = null,
                longitude = null,
                speed = null,
                accuracy = null,
                lean = null,
                batteryLevel = device.batteryLevel,
                signalStrength = null,
                isMoving = null
            )
        }

        return telemetry.copy(
            batteryLevel = telemetry.batteryLevel ?: device.batteryLevel
        )
    }

    fun getDeviceEvents(
        deviceId: UUID,
        startTime: Instant?,
        endTime: Instant?,
        eventType: String?,
        limit: Int?,
        userId: UUID,
        userRole: UserRole,
        authToken: String
    ): DeviceEventListResponse? {
        findDeviceWithAccess(deviceId, userId, userRole)
            ?: throw IllegalArgumentException("Device not found or access denied")
        return ingestionClient.getDeviceEvents(deviceId, startTime, endTime, eventType, limit, authToken)
    }


    private fun findDeviceWithAccess(
        deviceId: UUID,
        userId: UUID,
        userRole: UserRole
    ) = when (userRole) {
        UserRole.ADMIN -> null
        UserRole.PROVIDER -> {
            val device = deviceRepository.findById(deviceId).orElse(null)
            if (device?.providerId == userId) device else null
        }
        UserRole.SHIPPER -> {
            val device = deviceRepository.findById(deviceId).orElse(null)
            if (device != null) {
                val spec = ShipmentSpecification.buildSpecification(deviceId = deviceId)
                val shipments = shipmentRepository.findAll(spec)
                if (shipments.any { it.shipperId == userId }) {
                    device
                } else {
                    null
                }
            } else {
                null
            }
        }
        UserRole.CUSTOMER -> {
            val device = deviceRepository.findById(deviceId).orElse(null)
            if (device != null) {
                val spec = ShipmentSpecification.buildSpecification(deviceId = deviceId)
                val shipments = shipmentRepository.findAll(spec)
                if (shipments.any { it.customerId == userId }) {
                    device
                } else {
                    null
                }
            } else {
                null
            }
        }
    }
}
