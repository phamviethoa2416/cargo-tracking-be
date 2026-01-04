package com.example.cargotracking.common.messaging.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID

data class ShipmentTrackingMessage(
    @JsonProperty("message_id")
    val messageId: String,
    
    @JsonProperty("timestamp")
    val timestamp: Instant,
    
    @JsonProperty("shipment_id")
    val shipmentId: UUID,
    
    @JsonProperty("device_id")
    val deviceId: UUID,
    
    @JsonProperty("status")
    val status: String?,
    
    @JsonProperty("location")
    val location: DeviceLocation?,
    
    @JsonProperty("eta_minutes")
    val etaMinutes: Int?,
    
    @JsonProperty("distance_to_destination")
    val distanceToDestination: Double?
)
