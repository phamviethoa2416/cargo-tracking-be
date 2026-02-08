package com.example.cargotracking.modules.device.controller

import com.example.cargotracking.modules.device.model.dto.response.*
import com.example.cargotracking.modules.device.service.TelemetryService
import com.example.cargotracking.modules.user.principal.UserPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/devices")
class TelemetryController(
    private val telemetryService: TelemetryService
) {

    @GetMapping("/{id}/location")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'SHIPPER', 'CUSTOMER')")
    fun getDeviceLocation(
        @PathVariable id: UUID,
        @RequestHeader("Authorization") authorization: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<LocationResponse> {
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

    @GetMapping("/{id}/location/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'SHIPPER', 'CUSTOMER')")
    fun getLocationHistory(
        @PathVariable id: UUID,
        @RequestParam(required = false) startTime: Instant?,
        @RequestParam(required = false) endTime: Instant?,
        @RequestParam(required = false, defaultValue = "100") limit: Int?,
        @RequestParam(required = false, defaultValue = "0") offset: Int?,
        @RequestHeader("Authorization") authorization: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<LocationHistoryResponse> {
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

    @GetMapping("/{id}/telemetry")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'SHIPPER', 'CUSTOMER')")
    fun getDeviceTelemetry(
        @PathVariable id: UUID,
        @RequestHeader("Authorization") authorization: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<TelemetryResponse> {
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

    @GetMapping("/{id}/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'SHIPPER', 'CUSTOMER')")
    fun getDeviceEvents(
        @PathVariable id: UUID,
        @RequestParam(required = false) startTime: Instant?,
        @RequestParam(required = false) endTime: Instant?,
        @RequestParam(required = false) eventType: String?,
        @RequestParam(required = false, defaultValue = "50") limit: Int?,
        @RequestHeader("Authorization") authorization: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<DeviceEventListResponse> {
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
