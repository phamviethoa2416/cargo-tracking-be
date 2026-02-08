package com.example.cargotracking.modules.device.controller

import com.example.cargotracking.modules.device.model.dto.request.*
import com.example.cargotracking.modules.device.model.dto.response.*
import com.example.cargotracking.modules.device.model.types.DeviceStatus
import com.example.cargotracking.modules.device.service.DeviceService
import com.example.cargotracking.modules.user.principal.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/devices")
class DeviceController(
    private val deviceService: DeviceService
) {

    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    fun createDevice(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: CreateDeviceRequest
    ): ResponseEntity<DeviceResponse> {
        val device = deviceService.createDevice(
            request = request,
            providerId = principal.userId
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(device)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    fun getDeviceById(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<DeviceResponse> {
        val device = deviceService.getDeviceById(
            id = id,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )
        return ResponseEntity.ok(device)
    }

    @GetMapping("/hardware/{hardwareUid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    fun getDeviceByHardwareUid(
        @PathVariable hardwareUid: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<DeviceResponse> {
        val device = deviceService.getDeviceByHardwareUID(
            hardwareUID = hardwareUid,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )
        return ResponseEntity.ok(device)
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    fun getDeviceByStatus(
        @PathVariable status: DeviceStatus,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<DeviceResponse>> {
        val devices = deviceService.getDeviceByStatus(
            status = status,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )
        return ResponseEntity.ok(devices)
    }

    @GetMapping("/offline")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    fun getOfflineDevices(
        @RequestParam(defaultValue = "300000") thresholdMillis: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<DeviceResponse>> {
        val devices = deviceService.getOfflineDevices(
            thresholdMillis = thresholdMillis,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )
        return ResponseEntity.ok(devices)
    }

    @GetMapping("/online")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    fun getOnlineDevices(
        @RequestParam(defaultValue = "300000") thresholdMillis: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<DeviceResponse>> {
        val devices = deviceService.getOnlineDevices(
            thresholdMillis = thresholdMillis,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )
        return ResponseEntity.ok(devices)
    }

    @GetMapping("/shipment/{shipmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    fun getDeviceByShipmentId(
        @PathVariable shipmentId: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<DeviceResponse>> {
        val devices = deviceService.getDeviceByShipmentId(
            shipmentId = shipmentId,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )
        return ResponseEntity.ok(devices)
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    fun getAllDevices(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<DeviceResponse>> {
        val devices = deviceService.getAllDevices(
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )
        return ResponseEntity.ok(devices)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    fun updateDevice(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: UpdateDeviceRequest
    ): ResponseEntity<DeviceResponse> {
        val updatedDevice = deviceService.updateDevice(
            id = id,
            request = request,
            providerId = principal.userId
        )
        return ResponseEntity.ok(updatedDevice)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDevice(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ) {
        deviceService.deleteDevice(
            id = id,
            providerId = principal.userId
        )
    }

    @PatchMapping("/{id}/assign/{shipmentId}")
    @PreAuthorize("hasRole('PROVIDER')")
    fun assignDeviceToShipment(
        @PathVariable id: UUID,
        @PathVariable shipmentId: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<DeviceResponse> {
        val updatedDevice = deviceService.assignToShipment(
            deviceId = id,
            shipmentId = shipmentId,
            providerId = principal.userId
        )
        return ResponseEntity.ok(updatedDevice)
    }

    @PatchMapping("/{id}/release")
    @PreAuthorize("hasRole('PROVIDER')")
    fun releaseDeviceFromShipment(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<DeviceResponse> {
        val updatedDevice = deviceService.releaseFromShipment(
            deviceId = id,
            providerId = principal.userId
        )
        return ResponseEntity.ok(updatedDevice)
    }

    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    fun filterDevices(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: DeviceFilterRequest
    ): ResponseEntity<DeviceListResponse> {
        val response = deviceService.filterDevices(
            request = request,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )
        return ResponseEntity.ok(response)
    }
}
