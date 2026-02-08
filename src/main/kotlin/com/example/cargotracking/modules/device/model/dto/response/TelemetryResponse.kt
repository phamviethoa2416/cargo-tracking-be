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

data class DeviceEventResponse(
    val deviceId: UUID,
    val time: Instant,
    val eventType: String,
    val severity: String,
    val description: String?,
    val metadata: Map<String, Any>?
)

data class DeviceEventListResponse(
    val deviceId: UUID,
    val events: List<DeviceEventResponse>,
    val total: Int
)

data class LocationResponse(
    val deviceId: UUID,
    val time: Instant,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val speed: Double?,
    val heading: Double?,
    val accuracy: Double?
)

data class LocationHistoryResponse(
    val deviceId: UUID,
    val locations: List<LocationResponse>,
    val total: Int
)
