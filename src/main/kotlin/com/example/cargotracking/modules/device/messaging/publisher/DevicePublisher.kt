package com.example.cargotracking.modules.device.messaging.publisher

import com.example.cargotracking.modules.device.messaging.dto.DeviceConfigUpdate
import com.example.cargotracking.modules.device.model.entity.Device
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DevicePublisher(
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${rabbitmq.exchange:cargo_tracking}")
    private val exchangeName: String
) {
    private val logger = LoggerFactory.getLogger(DevicePublisher::class.java)

    fun publishDeviceConfigUpdate(device: Device) {
        try {
            val hardwareUid = try {
                UUID.fromString(device.hardwareUID)
            } catch (_: IllegalArgumentException) {
                UUID.nameUUIDFromBytes(device.hardwareUID.toByteArray())
            }

            val message = DeviceConfigUpdate(
                messageId = UUID.randomUUID().toString(),
                deviceId = device.id,
                hardwareUid = hardwareUid,
                name = device.deviceName ?: "",
                type = device.model ?: "",
                shipmentId = device.currentShipmentId,
                isActive = device.status.name != "RETIRED",
                configuration = null
            )

            rabbitTemplate.convertAndSend(
                exchangeName,
                "device.config.update",
                message
            )

            logger.debug("Published device config update for device: {}", device.id)
        } catch (e: Exception) {
            logger.error("Failed to publish device config update for device: ${device.id}", e)
        }
    }
}