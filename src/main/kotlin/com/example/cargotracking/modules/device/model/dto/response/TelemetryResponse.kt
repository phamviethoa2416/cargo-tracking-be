package com.example.cargotracking.modules.device.model.dto.response

import java.time.Instant
import java.util.UUID

data class TelemetryResponse(
    val deviceId: UUID,
    val time: Instant,
    val temperature: Double?,
    val humidity: Double?,
    val co2: Double?,
    val light: Double?,
    val latitude: Double?,
    val longitude: Double?,
    val speed: Double?,
    val accuracy: Double?,
    val lean: Double?,
    val batteryLevel: Int?,
    val signalStrength: Int?,
    val isMoving: Boolean?
)
