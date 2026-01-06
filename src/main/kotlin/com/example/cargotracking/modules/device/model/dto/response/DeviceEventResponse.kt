package com.example.cargotracking.modules.device.model.dto.response

import java.time.Instant
import java.util.UUID

data class DeviceEventResponse(
    val deviceId: UUID,
    val time: Instant,
    val eventType: String,
    val severity: String,
    val description: String?,
    val metadata: Map<String, Any>?
)
