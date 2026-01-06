package com.example.cargotracking.modules.device.model.dto.response

import java.util.UUID

data class DeviceEventListResponse(
    val deviceId: UUID,
    val events: List<DeviceEventResponse>,
    val total: Int
)
