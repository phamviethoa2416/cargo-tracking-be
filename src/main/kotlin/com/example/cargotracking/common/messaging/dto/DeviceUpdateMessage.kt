package com.example.cargotracking.common.messaging.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID

data class DeviceUpdateMessage(
    @JsonProperty("message_id")
    val messageId: String,
    
    @JsonProperty("timestamp")
    val timestamp: Instant,
    
    @JsonProperty("update_type")
    val updateType: String,
    
    @JsonProperty("device_id")
    val deviceId: UUID,
    
    @JsonProperty("is_online")
    val isOnline: Boolean?,

    @JsonProperty("last_seen")
    val lastSeen: Instant?,
    
    @JsonProperty("battery_level")
    val batteryLevel: Int?,
    
    @JsonProperty("signal_strength")
    val signalStrength: Int?,
    
    @JsonProperty("location")
    val location: DeviceLocation?
)
