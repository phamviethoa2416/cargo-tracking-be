package com.example.cargotracking.modules.device.messaging.listener

import com.example.cargotracking.modules.device.messaging.dto.DeviceEvent
import com.example.cargotracking.modules.device.messaging.service.EventService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DeviceEventListener(
    private val eventService: EventService
) {
    private val logger = LoggerFactory.getLogger(DeviceEventListener::class.java)

    @RabbitListener(queues = ["\${rabbitmq.queue.device-events:backend.device.events}"])
    @Transactional
    fun handle(message: DeviceEvent) {
        logger.info(
            "Received device event: type={}, deviceId={}, severity={}",
            message.eventType,
            message.deviceId,
            message.severity
        )
        eventService.handleDeviceEvent(message)
    }
}