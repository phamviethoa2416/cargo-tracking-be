package com.example.cargotracking.modules.device.messaging.listener

import com.example.cargotracking.modules.device.messaging.dto.DeviceUpdate
import com.example.cargotracking.modules.device.messaging.service.EventService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DeviceUpdateListener(
    private val eventService: EventService
) {

    @RabbitListener(queues = ["\${rabbitmq.queue.device-updates:backend.device.updates}"])
    @Transactional
    fun handleDeviceUpdate(message: DeviceUpdate) {
        eventService.handleDeviceUpdate(message)
    }
}