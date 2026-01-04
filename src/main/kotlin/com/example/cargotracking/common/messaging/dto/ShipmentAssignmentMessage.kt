package com.example.cargotracking.common.messaging.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class ShipmentAssignmentMessage(
    @JsonProperty("message_id")
    val messageId: String,
    
    @JsonProperty("shipment_id")
    val shipmentId: UUID,
    
    @JsonProperty("device_id")
    val deviceId: UUID,
    
    @JsonProperty("action")
    val action: String
)
