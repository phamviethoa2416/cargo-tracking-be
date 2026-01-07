package com.example.cargotracking.common.client.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID

data class IngestionTelemetryResponse(
    @JsonProperty("device_id")
    val deviceId: UUID,
    
    @JsonProperty("time")
    val time: Instant,
    
    @JsonProperty("hardware_uid")
    val hardwareUid: UUID?,
    
    @JsonProperty("temperature")
    val temperature: Double?,
    
    @JsonProperty("humidity")
    val humidity: Double?,
    
    @JsonProperty("co2")
    val co2: Double?,
    
    @JsonProperty("light")
    val light: Double?,
    
    @JsonProperty("latitude")
    val latitude: Double?,
    
    @JsonProperty("longitude")
    val longitude: Double?,
    
    @JsonProperty("speed")
    val speed: Double?,
    
    @JsonProperty("accuracy")
    val accuracy: Double?,
    
    @JsonProperty("lean")
    val lean: Double?,
    
    @JsonProperty("battery_level")
    val batteryLevel: Int?,
    
    @JsonProperty("signal_strength")
    val signalStrength: Int?,
    
    @JsonProperty("is_moving")
    val isMoving: Boolean?,
    
    @JsonProperty("event_type")
    val eventType: String?
)
