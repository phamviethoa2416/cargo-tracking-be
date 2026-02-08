package com.example.cargotracking.modules.device.model.dto.request

import com.example.cargotracking.modules.device.model.types.DeviceStatus
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateDeviceRequest(
    @field:NotBlank(message = "Hardware UID is required")
    @field:Size(min = 5, max = 255, message = "Hardware UID must be 5-255 characters")
    val hardwareUID: String,

    @field:Size(min = 2, max = 100, message = "Device name must be 2-100 characters")
    val deviceName: String? = null,

    @field:Size(max = 50, message = "Model must not exceed 50 characters")
    val model: String? = null,

    @field:Size(max = 50, message = "Firmware version must not exceed 50 characters")
    val firmwareVersion: String? = null
)

data class UpdateDeviceRequest(
    @field:Size(min = 2, max = 100, message = "Device name must be 2-100 characters")
    val deviceName: String? = null,

    @field:Size(max = 50, message = "Model must not exceed 50 characters")
    val model: String? = null,

    @field:Size(max = 50, message = "Firmware version must not exceed 50 characters")
    val firmwareVersion: String? = null,

    @field:Min(value = 0, message = "Battery level must be at least 0")
    @field:Max(value = 100, message = "Battery level must be at most 100")
    val batteryLevel: Int? = null,
)

data class DeviceFilterRequest(
    val status: DeviceStatus? = null,
    val minBattery: Int? = null,
    val maxBattery: Int? = null,
    val isOffline: Boolean? = null,
    val search: String? = null,

    val page: Int = 1,
    val pageSize: Int = 20,
    val sortBy: String = "createdAt",
    val sortOrder: String = "desc"
)