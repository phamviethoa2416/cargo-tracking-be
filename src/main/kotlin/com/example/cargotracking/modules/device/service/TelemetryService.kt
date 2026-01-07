package com.example.cargotracking.modules.device.service

import com.example.cargotracking.common.client.IngestionClient
import com.example.cargotracking.modules.device.model.dto.response.*
import com.example.cargotracking.modules.device.repository.DeviceRepository
import com.example.cargotracking.modules.shipment.repository.ShipmentRepository
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
        logger.info("[TELEMETRY_SERVICE] getLatestTelemetry called - deviceId: {}, userId: {}, role: {}", 
            deviceId, userId, userRole)
        
        val device = findDeviceWithAccess(deviceId, userId, userRole)
            ?: throw IllegalArgumentException("Device not found or access denied")

        logger.info("[TELEMETRY_SERVICE] Device found - id: {}, batteryLevel from DB: {}", 
            device.id, device.batteryLevel)
        
        // Get telemetry from ingestion, supplemented with device data from DB
        val telemetry = ingestionClient.getLatestTelemetry(deviceId, authToken)
        
        if (telemetry == null) {
            logger.warn("[TELEMETRY_SERVICE] Ingestion client returned null, creating fallback response")
            return device.let {
                TelemetryResponse(
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
        }
        
        logger.info("[TELEMETRY_SERVICE] Received telemetry from ingestion client:")
        logger.info("  - temperature: {} (null={})", telemetry.temperature, telemetry.temperature == null)
        logger.info("  - humidity: {} (null={})", telemetry.humidity, telemetry.humidity == null)
        logger.info("  - co2: {} (null={})", telemetry.co2, telemetry.co2 == null)
        logger.info("  - light: {} (null={})", telemetry.light, telemetry.light == null)
        logger.info("  - batteryLevel: {} (null={})", telemetry.batteryLevel, telemetry.batteryLevel == null)
        logger.info("  - signalStrength: {} (null={})", telemetry.signalStrength, telemetry.signalStrength == null)
        
        // Enrich with data from backend's device record
        val enriched = telemetry.copy(
            batteryLevel = telemetry.batteryLevel ?: device.batteryLevel
        )
        
        if (telemetry.batteryLevel == null && device.batteryLevel != null) {
            logger.info("[TELEMETRY_SERVICE] Enriched batteryLevel from device DB: {}", device.batteryLevel)
        }
        
        logger.info("[TELEMETRY_SERVICE] Returning enriched telemetry:")
        logger.info("  - temperature: {} (null={})", enriched.temperature, enriched.temperature == null)
        logger.info("  - humidity: {} (null={})", enriched.humidity, enriched.humidity == null)
        logger.info("  - co2: {} (null={})", enriched.co2, enriched.co2 == null)
        logger.info("  - light: {} (null={})", enriched.light, enriched.light == null)
        logger.info("  - batteryLevel: {} (null={})", enriched.batteryLevel, enriched.batteryLevel == null)
        
        return enriched
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
        UserRole.SHIPPER -> {
            // Shipper can only access devices assigned to their shipments
            val device = deviceRepository.findById(deviceId).orElse(null)
            if (device != null) {
                val shipments = shipmentRepository.findByDeviceId(deviceId)
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
            // Customer can only access devices assigned to their shipments
            val device = deviceRepository.findById(deviceId).orElse(null)
            if (device != null) {
                val shipments = shipmentRepository.findByDeviceId(deviceId)
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
