package com.example.cargotracking.modules.device.model.dto.response

import java.util.UUID

data class LocationHistoryResponse(
    val deviceId: UUID,
    val locations: List<LocationResponse>,
    val total: Int
)
