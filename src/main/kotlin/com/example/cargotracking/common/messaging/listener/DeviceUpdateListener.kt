package com.example.cargotracking.common.messaging.listener

import com.example.cargotracking.common.messaging.dto.DeviceUpdateMessage
import com.example.cargotracking.modules.device.model.entity.Device
import com.example.cargotracking.modules.device.repository.DeviceRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DeviceUpdateListener(
    private val deviceRepository: DeviceRepository
) {
    private val logger = LoggerFactory.getLogger(DeviceUpdateListener::class.java)

    @RabbitListener(queues = ["\${rabbitmq.queue.device-updates:backend.device.updates}"])
    @Transactional
    fun handleDeviceUpdate(message: DeviceUpdateMessage) {
        try {
            logger.debug(
                "Received device update: type={}, deviceId={}",
                message.updateType,
                message.deviceId
            )

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
            logger.error(
                "Error processing device update: messageId={}, deviceId={}, type={}",
                message.messageId,
                message.deviceId,
                message.updateType,
                e
            )
            throw e // Re-throw to trigger retry mechanism
        }
    }

    private fun handleHeartbeat(device: Device, message: DeviceUpdateMessage) {
        // Update battery level if provided
        message.batteryLevel?.let { level ->
            device.updateBatteryLevel(level)
            logger.debug("Updated battery level for device {}: {}", message.deviceId, level)
        }

        // Update last seen timestamp
        message.lastSeen?.let { lastSeen ->
            device.updateLastSeenAt(lastSeen)
            logger.debug("Updated last seen for device {}: {}", message.deviceId, lastSeen)
        }

        // Update signal strength if available
        message.signalStrength?.let { signal ->
            logger.debug("Device {} signal strength: {}", message.deviceId, signal)
            // Could store in a separate table or extend Device entity
        }
    }

    private fun handleLocation(device: Device, message: DeviceUpdateMessage) {
        message.location?.let { location ->
            logger.debug(
                "Device {} location update: lat={}, lon={}, accuracy={}",
                message.deviceId,
                location.latitude,
                location.longitude,
                location.accuracy
            )
            // Location data is stored in Ingestion service's TimescaleDB
            // Backend could store a cached version or just log it
        }
    }

    private fun handleStatus(device: Device, message: DeviceUpdateMessage) {
        message.isOnline?.let { isOnline ->
            logger.debug("Device {} status update: isOnline={}", message.deviceId, isOnline)
            // Status is derived from lastSeenAt in Device entity
            // Could update a separate status field if needed
        }
    }
}
