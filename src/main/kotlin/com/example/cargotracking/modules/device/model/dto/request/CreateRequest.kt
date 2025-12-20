package com.example.cargotracking.modules.device.model.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateDeviceRequest(
    @field:NotBlank
    @field:Size(min = 5, max = 255)
    val hardwareUID: String,

    @field:NotBlank
    @field:Size(min = 2, max = 100)
    val deviceName: String? = null,

    @field:Size(max = 50)
    val model: String? = null,

    @field:NotBlank
    val providerId: UUID,

    @field:Size(max = 50)
    val firmwareVersion: String? = null
)