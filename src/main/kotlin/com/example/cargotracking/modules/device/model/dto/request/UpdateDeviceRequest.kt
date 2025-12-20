package com.example.cargotracking.modules.device.model.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class UpdateDeviceRequest(
    @field:Size(min = 2, max = 100)
    val deviceName: String? = null,

    @field:Size(max = 50)
    val model: String? = null,

    @field:Size(max = 50)
    val firmwareVersion: String? = null,

    @field:NotNull
    @field:Min(0)
    @field:Max(100)
    val batteryLevel: Int,

    val reason: String? = null,

    val providerId: UUID
)
