package com.example.cargotracking.modules.shipment.messaging.listener

import com.example.cargotracking.modules.shipment.messaging.dto.ShipmentTracking
import com.example.cargotracking.modules.shipment.messaging.service.TrackingService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ShipmentTrackingListener(
    private val trackingService: TrackingService
) {

    @RabbitListener(queues = ["\${rabbitmq.queue.shipment-tracking:backend.shipment.tracking}"])
    @Transactional
    fun handleShipmentTracking(message: ShipmentTracking) {
        trackingService.handleShipmentTracking(message)
    }
}
