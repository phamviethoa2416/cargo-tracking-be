package com.example.cargotracking.modules.device.model.dto.request

import com.example.cargotracking.modules.device.model.types.DeviceStatus
import java.util.UUID

data class DeviceFilterRequest(
    val status: DeviceStatus? = null,
    val providerId: UUID? = null,
    val minBattery: Int? = null,
    val maxBattery: Int? = null,
    val isOffline: Boolean? = null,
    val search: String? = null,

    val page: Int = 1,
    val pageSize: Int = 20,
    val sortBy: String = "createdAt",
    val sortOrder: String = "desc"
)