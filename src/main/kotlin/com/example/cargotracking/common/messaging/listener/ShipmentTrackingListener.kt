package com.example.cargotracking.common.messaging.listener

import com.example.cargotracking.common.messaging.dto.ShipmentTrackingMessage
import com.example.cargotracking.modules.shipment.repository.ShipmentRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ShipmentTrackingListener(
    private val shipmentRepository: ShipmentRepository
) {
    private val logger = LoggerFactory.getLogger(ShipmentTrackingListener::class.java)

    @RabbitListener(queues = ["\${rabbitmq.queue.shipment-tracking:backend.shipment.tracking}"])
    @Transactional(readOnly = true)
    fun handleShipmentTracking(message: ShipmentTrackingMessage) {
        try {
            logger.debug(
                "Received shipment tracking update: shipmentId={}, deviceId={}, status={}, eta={}",
                message.shipmentId,
                message.deviceId,
                message.status,
                message.etaMinutes
            )

            val shipment = shipmentRepository.findById(message.shipmentId).orElse(null)
            if (shipment == null) {
                logger.warn("Shipment not found for tracking update: shipmentId={}", message.shipmentId)
                return
            }

            // Log tracking information
            message.location?.let { location ->
                logger.info(
                    "Shipment {} location: lat={}, lon={}, distance={}km, eta={}min",
                    message.shipmentId,
                    location.latitude,
                    location.longitude,
                    message.distanceToDestination,
                    message.etaMinutes
                )
            }

            // Note: Tracking data is stored in Ingestion service's TimescaleDB
            // Backend service could:
            // 1. Store a cached version in PostgreSQL (if needed for quick access)
            // 2. Trigger notifications to customers
            // 3. Update shipment status based on location (e.g., near destination)
            // 4. Just log and rely on Ingestion service for tracking queries

            // Example: Could trigger customer notification if shipment is near destination
            message.etaMinutes?.let { eta ->
                if (eta <= 30) {
                    logger.info("Shipment {} is arriving soon (ETA: {} minutes)", message.shipmentId, eta)
                    // Could trigger notification service here
                }
            }

        } catch (e: Exception) {
            logger.error(
                "Error processing shipment tracking update: messageId={}, shipmentId={}",
                message.messageId,
                message.shipmentId,
                e
            )
            throw e // Re-throw to trigger retry mechanism
        }
    }
}
