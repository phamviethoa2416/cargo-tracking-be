package com.example.cargotracking.modules.shipment.messaging.service

import com.example.cargotracking.modules.shipment.messaging.dto.ShipmentTracking
import com.example.cargotracking.modules.shipment.repository.ShipmentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TrackingService(
    private val shipmentRepository: ShipmentRepository
) {
    private val logger = LoggerFactory.getLogger(TrackingService::class.java)

    @Transactional(readOnly = true)
    fun handleShipmentTracking(message: ShipmentTracking) {
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

            // Log only; do NOT persist (full time-series in Ingestion; frontend via Backend proxy).
            message.location?.let { location ->
                logger.debug(
                    "Shipment {} location: lat={}, lon={}, distance={}km, eta={}min",
                    message.shipmentId,
                    location.latitude,
                    location.longitude,
                    message.distanceToDestination,
                    message.etaMinutes
                )
            }

            message.etaMinutes?.let { eta ->
                if (eta <= 30) {
                    logger.info("Shipment {} is arriving soon (ETA: {} minutes)", message.shipmentId, eta)
                }
            }

        } catch (e: Exception) {
            logger.error(
                "Error processing shipment tracking update: messageId={}, shipmentId={}",
                message.messageId,
                message.shipmentId,
                e
            )
            throw e
        }
    }
}
