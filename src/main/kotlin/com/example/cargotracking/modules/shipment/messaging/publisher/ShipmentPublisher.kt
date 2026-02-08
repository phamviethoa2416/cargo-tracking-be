package com.example.cargotracking.modules.shipment.messaging.publisher

import com.example.cargotracking.modules.shipment.messaging.dto.ShipmentAssignment
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ShipmentPublisher(
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${rabbitmq.exchange:cargo_tracking}")
    private val exchangeName: String
) {
    private val logger = LoggerFactory.getLogger(ShipmentPublisher::class.java)

    fun publishShipmentAssignment(
        deviceId: UUID,
        shipmentId: UUID,
        action: String
    ) {
        try {
            val message = ShipmentAssignment(
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