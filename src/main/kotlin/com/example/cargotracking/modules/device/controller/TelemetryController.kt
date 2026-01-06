package com.example.cargotracking.modules.device.controller

import com.example.cargotracking.modules.device.model.dto.response.*
import com.example.cargotracking.modules.device.service.TelemetryService
import com.example.cargotracking.modules.user.principal.UserPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

/**
 * Controller for telemetry-related endpoints.
 * Acts as a proxy to the Ingestion service for location, telemetry, and event data.
 */
@RestController
@RequestMapping("/api/devices")
class TelemetryController(
    private val telemetryService: TelemetryService
) {

    /**
     * Get the latest location for a device
     */
    @GetMapping("/{id}/location")
    fun getDeviceLocation(
        @PathVariable id: UUID,
        @RequestHeader("Authorization") authorization: String,
        authentication: Authentication
    ): ResponseEntity<LocationResponse> {
        val principal = authentication.principal as UserPrincipal
        val token = authorization.removePrefix("Bearer ").trim()

        val location = telemetryService.getLatestLocation(
            deviceId = id,
            userId = principal.userId,
            userRole = principal.role,
            authToken = token
        )

        return location?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    /**
     * Get location history for a device
     */
    @GetMapping("/{id}/location/history")
    fun getLocationHistory(
        @PathVariable id: UUID,
        @RequestParam(required = false) startTime: Instant?,
        @RequestParam(required = false) endTime: Instant?,
        @RequestParam(required = false, defaultValue = "100") limit: Int?,
        @RequestParam(required = false, defaultValue = "0") offset: Int?,
        @RequestHeader("Authorization") authorization: String,
        authentication: Authentication
    ): ResponseEntity<LocationHistoryResponse> {
        val principal = authentication.principal as UserPrincipal
        val token = authorization.removePrefix("Bearer ").trim()

        val history = telemetryService.getLocationHistory(
            deviceId = id,
            startTime = startTime,
            endTime = endTime,
            limit = limit,
            offset = offset,
            userId = principal.userId,
            userRole = principal.role,
            authToken = token
        )

        return history?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    /**
     * Get latest telemetry data for a device (temperature, humidity, battery, etc.)
     */
    @GetMapping("/{id}/telemetry")
    fun getDeviceTelemetry(
        @PathVariable id: UUID,
        @RequestHeader("Authorization") authorization: String,
        authentication: Authentication
    ): ResponseEntity<TelemetryResponse> {
        val principal = authentication.principal as UserPrincipal
        val token = authorization.removePrefix("Bearer ").trim()

        val telemetry = telemetryService.getLatestTelemetry(
            deviceId = id,
            userId = principal.userId,
            userRole = principal.role,
            authToken = token
        )

        return telemetry?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    /**
     * Get events/alerts for a device
     */
    @GetMapping("/{id}/events")
    fun getDeviceEvents(
        @PathVariable id: UUID,
        @RequestParam(required = false) startTime: Instant?,
        @RequestParam(required = false) endTime: Instant?,
        @RequestParam(required = false) eventType: String?,
        @RequestParam(required = false, defaultValue = "50") limit: Int?,
        @RequestHeader("Authorization") authorization: String,
        authentication: Authentication
    ): ResponseEntity<DeviceEventListResponse> {
        val principal = authentication.principal as UserPrincipal
        val token = authorization.removePrefix("Bearer ").trim()

        val events = telemetryService.getDeviceEvents(
            deviceId = id,
            startTime = startTime,
            endTime = endTime,
            eventType = eventType,
            limit = limit,
            userId = principal.userId,
            userRole = principal.role,
            authToken = token
        )

        return events?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }
}
