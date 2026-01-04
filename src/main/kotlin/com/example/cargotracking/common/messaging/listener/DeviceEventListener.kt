package com.example.cargotracking.common.messaging.listener

import com.example.cargotracking.common.messaging.dto.DeviceEventMessage
import com.example.cargotracking.modules.device.repository.DeviceRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DeviceEventListener(
    private val deviceRepository: DeviceRepository
) {
    private val logger = LoggerFactory.getLogger(DeviceEventListener::class.java)

    @RabbitListener(queues = ["\${rabbitmq.queue.device-events:backend.device.events}"])
    @Transactional
    fun handleDeviceEvent(message: DeviceEventMessage) {
        try {
            logger.info(
                "Received device event: type={}, deviceId={}, severity={}",
                message.eventType,
                message.deviceId,
                message.severity
            )

            // Find device by ID
            val device = deviceRepository.findById(message.deviceId).orElse(null)
            if (device == null) {
                logger.warn("Device not found for event: deviceId={}", message.deviceId)
                return
            }

            // Handle different event types
            when (message.eventType) {
                "DEVICE_OFFLINE" -> {
                    // Device went offline
                    logger.info("Device {} went offline", message.deviceId)
                }
                "DEVICE_ONLINE" -> {
                    // Device came online
                    logger.info("Device {} came online", message.deviceId)
                }
                "LOW_BATTERY" -> {
                    // Extract battery level from metadata if available
                    val batteryLevel = message.metadata?.get("battery_level") as? Int
                    if (batteryLevel != null) {
                        device.updateBatteryLevel(batteryLevel)
                        deviceRepository.save(device)
                        logger.info("Updated battery level for device {}: {}", message.deviceId, batteryLevel)
                    }
                }
                "TEMP_HIGH", "TEMP_LOW" -> {
                    logger.warn(
                        "Temperature alert for device {}: type={}, metadata={}",
                        message.deviceId,
                        message.eventType,
                        message.metadata
                    )
                    // Could trigger notifications or alerts here
                }
                else -> {
                    logger.debug("Unhandled event type: {}", message.eventType)
                }
            }

            // Log critical events
            if (message.severity == "CRITICAL") {
                logger.error(
                    "CRITICAL event received: type={}, deviceId={}, description={}",
                    message.eventType,
                    message.deviceId,
                    message.description
                )
                // Could trigger notifications, alerts, or webhooks here
            }

        } catch (e: Exception) {
            logger.error("Error processing device event: messageId={}, deviceId={}", message.messageId, message.deviceId, e)
            throw e // Re-throw to trigger retry mechanism
        }
    }
}
