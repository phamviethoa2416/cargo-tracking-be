package com.example.cargotracking.common.messaging.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID

data class DeviceLocation(
    @JsonProperty("latitude")
    val latitude: Double,

    @JsonProperty("longitude")
    val longitude: Double,

    @JsonProperty("altitude")
    val altitude: Double?,

    @JsonProperty("accuracy")
    val accuracy: Double?,

    @JsonProperty("timestamp")
    val timestamp: Instant
)
