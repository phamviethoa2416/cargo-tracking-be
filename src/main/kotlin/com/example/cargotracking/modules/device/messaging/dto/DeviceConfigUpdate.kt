package com.example.cargotracking.modules.device.messaging.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class DeviceConfigUpdate(
    @JsonProperty("message_id")
    val messageId: String,
    
    @JsonProperty("device_id")
    val deviceId: UUID,
    
    @JsonProperty("hardware_uid")
    val hardwareUid: UUID,
    
    @JsonProperty("name")
    val name: String?,
    
    @JsonProperty("type")
    val type: String?,
    
    @JsonProperty("shipment_id")
    val shipmentId: UUID?,
    
    @JsonProperty("is_active")
    val isActive: Boolean,
    
    @JsonProperty("configuration")
    val configuration: Map<String, Any>? = null
)
