package com.example.cargotracking.modules.device.model.dto.request

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

