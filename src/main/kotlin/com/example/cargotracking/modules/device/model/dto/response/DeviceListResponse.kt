package com.example.cargotracking.modules.device.model.dto.response

data class DeviceListResponse(
    val devices: List<DeviceResponse>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)