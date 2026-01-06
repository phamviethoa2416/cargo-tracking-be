package com.example.cargotracking.modules.device.model.dto.response

import java.time.Instant
import java.util.UUID

data class TelemetryResponse(
    val deviceId: UUID,
    val time: Instant,
    val temperature: Double?,
    val humidity: Double?,
    val pressure: Double?,
    val batteryLevel: Int?,
    val signalStrength: Int?,
    val isMoving: Boolean?
)
