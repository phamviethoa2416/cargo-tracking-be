package com.example.cargotracking.modules.device.validation

import com.example.cargotracking.modules.device.model.types.DeviceStatus
import org.springframework.stereotype.Component

@Component
object DeviceStatusValidator {
    private val validTransitions = mapOf(
        DeviceStatus.AVAILABLE to setOf(
            DeviceStatus.IN_TRANSIT,
            DeviceStatus.MAINTENANCE,
            DeviceStatus.RETIRED
        ),
        DeviceStatus.IN_TRANSIT to setOf(
            DeviceStatus.AVAILABLE,
            DeviceStatus.MAINTENANCE,
            DeviceStatus.RETIRED
        ),
        DeviceStatus.MAINTENANCE to setOf(
            DeviceStatus.AVAILABLE,
            DeviceStatus.RETIRED
        ),
        DeviceStatus.RETIRED to emptySet()
    )

    fun validateTransition(
        currentStatus: DeviceStatus,
        newStatus: DeviceStatus
    ) {
        val allowedStatuses = validTransitions[currentStatus]
            ?: throw IllegalStateException("Invalid current status: $currentStatus")

        if (newStatus !in allowedStatuses) {
            throw IllegalArgumentException(
                "Invalid status transition from $currentStatus to $newStatus"
            )
        }
    }
}