package com.example.cargotracking.modules.device.service

import com.example.cargotracking.common.client.IngestionClient
import com.example.cargotracking.modules.device.model.dto.response.*
import com.example.cargotracking.modules.device.repository.DeviceRepository
import com.example.cargotracking.modules.user.model.types.UserRole
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class TelemetryService(
    private val ingestionClient: IngestionClient,
    private val deviceRepository: DeviceRepository
) {
    private val logger = LoggerFactory.getLogger(TelemetryService::class.java)

    /**
     * Get latest location for a device with authorization check
     */
    fun getLatestLocation(
        deviceId: UUID,
        userId: UUID,
        userRole: UserRole,
        authToken: String
    ): LocationResponse? {
        val device = findDeviceWithAccess(deviceId, userId, userRole)
            ?: throw IllegalArgumentException("Device not found or access denied")

        logger.debug("Fetching location for device {} by user {}", deviceId, userId)
        return ingestionClient.getLatestLocation(deviceId, authToken)
    }

    /**
     * Get location history for a device
     */
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
        // Verify device exists and user has access
        val device = findDeviceWithAccess(deviceId, userId, userRole)
            ?: throw IllegalArgumentException("Device not found or access denied")

        logger.debug("Fetching location history for device {} by user {}", deviceId, userId)
        return ingestionClient.getLocationHistory(deviceId, startTime, endTime, limit, offset, authToken)
    }

    /**
     * Get latest telemetry data for a device
     */
    fun getLatestTelemetry(
        deviceId: UUID,
        userId: UUID,
        userRole: UserRole,
        authToken: String
    ): TelemetryResponse? {
        val device = findDeviceWithAccess(deviceId, userId, userRole)
            ?: throw IllegalArgumentException("Device not found or access denied")

        logger.debug("Fetching telemetry for device {} by user {}", deviceId, userId)
        
        // Get telemetry from ingestion, supplemented with device data from DB
        val telemetry = ingestionClient.getLatestTelemetry(deviceId, authToken)
        
        // Enrich with data from backend's device record
        return telemetry?.copy(
            batteryLevel = telemetry.batteryLevel ?: device.batteryLevel
        ) ?: device.let {
            TelemetryResponse(
                deviceId = device.id,
                time = device.lastSeenAt ?: Instant.now(),
                temperature = null,
                humidity = null,
                pressure = null,
                batteryLevel = device.batteryLevel,
                signalStrength = null,
                isMoving = null
            )
        }
    }

    /**
     * Get events/alerts for a device
     */
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
        val device = findDeviceWithAccess(deviceId, userId, userRole)
            ?: throw IllegalArgumentException("Device not found or access denied")

        logger.debug("Fetching events for device {} by user {}", deviceId, userId)
        return ingestionClient.getDeviceEvents(deviceId, startTime, endTime, eventType, limit, authToken)
    }


    private fun findDeviceWithAccess(
        deviceId: UUID,
        userId: UUID,
        userRole: UserRole
    ) = when (userRole) {
        UserRole.ADMIN -> deviceRepository.findById(deviceId).orElse(null)
        UserRole.PROVIDER -> {
            val device = deviceRepository.findById(deviceId).orElse(null)
            if (device?.providerId == userId) device else null
        }
        UserRole.SHIPPER -> deviceRepository.findById(deviceId).orElse(null)
        UserRole.CUSTOMER -> null
    }
}
