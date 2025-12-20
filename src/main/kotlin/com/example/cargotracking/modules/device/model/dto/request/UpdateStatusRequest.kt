package com.example.cargotracking.modules.device.model.dto.request

import com.example.cargotracking.modules.device.model.types.DeviceStatus
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class UpdateStatusRequest(
    @field:NotNull
    val status: DeviceStatus,
    
    val shipmentId: UUID? = null
)

