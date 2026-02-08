package com.example.cargotracking.modules.device.messaging.service

import com.example.cargotracking.modules.device.messaging.dto.DeviceEvent
import com.example.cargotracking.modules.device.messaging.dto.DeviceUpdate
import com.example.cargotracking.modules.device.messaging.type.EventType
import com.example.cargotracking.modules.device.model.entity.Device
import com.example.cargotracking.modules.device.repository.DeviceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EventService(
    private val deviceRepository: DeviceRepository
) {
    private val logger = LoggerFactory.getLogger(EventService::class.java)

    @Transactional
    fun handleDeviceEvent(message: DeviceEvent) {
        try {
            val device = deviceRepository.findById(message.deviceId).orElse(null)
            if (device == null) {
                logger.warn("Device not found for event: deviceId={}", message.deviceId)
                return
            }

            val eventType = runCatching { EventType.valueOf(message.eventType) }.getOrNull()
            when (eventType) {
                EventType.DEVICE_OFFLINE -> {
                    logger.info("Device {} went offline", message.deviceId)
                }
                EventType.DEVICE_ONLINE -> {
                    logger.info("Device {} came online", message.deviceId)
                }
                EventType.LOW_BATTERY -> {
                    val batteryLevel = (message.metadata?.get("battery_level") as? Number)?.toInt()
                    if (batteryLevel != null && batteryLevel in 0..100) {
                        device.updateBatteryLevel(batteryLevel)
                        deviceRepository.save(device)
                        logger.info("Updated battery level for device {}: {}", message.deviceId, batteryLevel)
                    }
                }
                EventType.TEMP_HIGH, EventType.TEMP_LOW -> {
                    logger.warn(
                        "Temperature alert for device {}: type={}, metadata={}",
                        message.deviceId,
                        message.eventType,
                        message.metadata
                    )
                }
                null -> {
                    logger.debug("Unhandled event type: {}", message.eventType)
                }
            }

            if (message.severity == "CRITICAL") {
                logger.error(
                    "CRITICAL event received: type={}, deviceId={}, description={}",
                    message.eventType,
                    message.deviceId,
                    message.description
                )
            }

        } catch (e: Exception) {
            logger.error("Error processing device event: messageId={}, deviceId={}", message.messageId, message.deviceId, e)
            throw e
        }
    }

    @Transactional
    fun handleDeviceUpdate(message: DeviceUpdate) {
        try {
            val device = deviceRepository.findById(message.deviceId).orElse(null)
            if (device == null) {
                logger.warn("Device not found for update: deviceId={}", message.deviceId)
                return
            }

            when (message.updateType) {
                "heartbeat" -> {
                    handleHeartbeat(device, message)
                }
                "location" -> {
                    handleLocation(device, message)
                }
                "status" -> {
                    handleStatus(device, message)
                }
                else -> {
                    logger.debug("Unhandled update type: {}", message.updateType)
                }
            }

            deviceRepository.save(device)

        } catch (e: Exception) {
            throw e
        }
    }

    private fun handleHeartbeat(device: Device, message: DeviceUpdate) {
        message.batteryLevel?.let { level ->
            device.updateBatteryLevel(level)
            logger.debug("Updated battery level for device {}: {}", message.deviceId, level)
        }

        message.lastSeen?.let { lastSeen ->
            device.updateLastSeenAt(lastSeen)
            logger.debug("Updated last seen for device {}: {}", message.deviceId, lastSeen)
        }

        message.signalStrength?.let { signal ->
            logger.debug("Device {} signal strength: {}", message.deviceId, signal)
            // Not persisted in Backend to avoid duplicating Ingestion telemetry.
        }
    }

    private fun handleLocation(device: Device, message: DeviceUpdate) {
        message.location?.let { location ->
            logger.debug(
                "Device {} location update: lat={}, lon={}, accuracy={}",
                message.deviceId,
                location.latitude,
                location.longitude,
                location.accuracy
            )
            // Do NOT persist: full time-series lives in Ingestion. Frontend gets via Backend proxy.
        }
    }

    private fun handleStatus(device: Device, message: DeviceUpdate) {
        message.isOnline?.let { isOnline ->
            logger.debug("Device {} status update: isOnline={}", message.deviceId, isOnline)
            // Status derived from lastSeenAt on Device; no separate persistence here.
        }
    }
}