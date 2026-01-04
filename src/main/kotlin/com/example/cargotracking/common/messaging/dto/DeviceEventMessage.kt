package com.example.cargotracking.common.messaging.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID

data class DeviceEventMessage(
    @JsonProperty("message_id")
    val messageId: String,
    
    @JsonProperty("timestamp")
    val timestamp: Instant,
    
    @JsonProperty("source")
    val source: String,
    
    @JsonProperty("event_type")
    val eventType: String,
    
    @JsonProperty("device_id")
    val deviceId: UUID,
    
    @JsonProperty("severity")
    val severity: String,  // INFO, WARNING, CRITICAL
    
    @JsonProperty("description")
    val description: String?,
    
    @JsonProperty("metadata")
    val metadata: Map<String, Any>?,
    
    @JsonProperty("event_time")
    val eventTime: Instant
)
