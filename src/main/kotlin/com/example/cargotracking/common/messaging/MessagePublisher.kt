package com.example.cargotracking.common.messaging

import com.example.cargotracking.common.messaging.dto.DeviceConfigUpdateMessage
import com.example.cargotracking.common.messaging.dto.ShipmentAssignmentMessage
import com.example.cargotracking.modules.device.model.entity.Device
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class MessagePublisher(
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${rabbitmq.exchange:cargo_tracking}")
    private val exchangeName: String
) {
    private val logger = LoggerFactory.getLogger(MessagePublisher::class.java)


    fun publishDeviceConfigUpdate(device: Device) {
        try {
            val hardwareUid = try {
                UUID.fromString(device.hardwareUID)
            } catch (_: IllegalArgumentException) {
                UUID.nameUUIDFromBytes(device.hardwareUID.toByteArray())
            }

            val message = DeviceConfigUpdateMessage(
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

    fun publishShipmentAssignment(
        deviceId: UUID,
        shipmentId: UUID,
        action: String
    ) {
        try {
            val message = ShipmentAssignmentMessage(
                messageId = UUID.randomUUID().toString(),
                shipmentId = shipmentId,
                deviceId = deviceId,
                action = action
            )

            rabbitTemplate.convertAndSend(
                exchangeName,
                "device.shipment.assignment",
                message
            )

            logger.debug("Published shipment assignment: {} for device: {}, shipment: {}", action, deviceId, shipmentId)
        } catch (e: Exception) {
            logger.error("Failed to publish shipment assignment for device: $deviceId, shipment: $shipmentId", e)
        }
    }
}
