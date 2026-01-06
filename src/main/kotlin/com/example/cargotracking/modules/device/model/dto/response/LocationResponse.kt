package com.example.cargotracking.modules.device.model.dto.response

import java.time.Instant
import java.util.UUID

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
