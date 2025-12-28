package com.example.cargotracking.modules.device.controller

import com.example.cargotracking.modules.device.model.dto.request.CreateDeviceRequest
import com.example.cargotracking.modules.device.model.dto.request.DeviceFilterRequest
import com.example.cargotracking.modules.device.model.dto.request.UpdateDeviceRequest
import com.example.cargotracking.modules.device.model.dto.request.UpdateStatusRequest
import com.example.cargotracking.modules.device.model.dto.response.DeviceListResponse
import com.example.cargotracking.modules.device.model.dto.response.DeviceResponse
import com.example.cargotracking.modules.device.model.types.DeviceStatus
import com.example.cargotracking.modules.device.service.DeviceService
import com.example.cargotracking.modules.user.principal.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/devices")
class DeviceController(
    private val deviceService: DeviceService
) {
    @PostMapping
    fun createDevice(
        @Valid @RequestBody request: CreateDeviceRequest,
        authentication: Authentication
    ): ResponseEntity<DeviceResponse> {
        val principal = authentication.principal as UserPrincipal

        val createdDevice = deviceService.createDevice(
            request = request,
            providerId = principal.userId
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(createdDevice)
    }

    @GetMapping("/{id}")
    fun getDeviceById(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<DeviceResponse> {
        val principal = authentication.principal as UserPrincipal

        val device = deviceService.getDeviceById(
            id = id,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(DeviceResponse.from(device))
    }

    @GetMapping("/status/{status}")
    fun getDeviceByStatus(
        @PathVariable status: DeviceStatus,
        authentication: Authentication
    ): ResponseEntity<List<DeviceResponse>> {
        val principal = authentication.principal as UserPrincipal

        val devices = deviceService.getDeviceByStatus(
            status = status,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(devices.map(DeviceResponse::from))
    }


    @GetMapping("/hardware/{hardwareUid}")
    fun getDeviceByHardwareUid(
        @PathVariable hardwareUid: String,
        authentication: Authentication
    ): ResponseEntity<DeviceResponse> {
        val principal = authentication.principal as UserPrincipal

        val device = deviceService.getDeviceByHardwareUID(
            hardwareUID = hardwareUid,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(DeviceResponse.from(device))
    }

    @GetMapping("/offline")
    fun getOfflineDevices(
        @RequestParam(defaultValue = "300000") thresholdMillis: Long,
        authentication: Authentication
    ): ResponseEntity<List<DeviceResponse>> {
        val principal = authentication.principal as UserPrincipal

        val devices = deviceService.getOfflineDevices(
            thresholdMillis = thresholdMillis,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(devices.map(DeviceResponse::from))
    }

    @GetMapping("/online")
    fun getOnlineDevices(
        @RequestParam(defaultValue = "300000") thresholdMillis: Long,
        authentication: Authentication
    ): ResponseEntity<List<DeviceResponse>> {
        val principal = authentication.principal as UserPrincipal

        val devices = deviceService.getOnlineDevices(
            thresholdMillis = thresholdMillis,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(devices.map(DeviceResponse::from))
    }

    @GetMapping("/shipment/{shipmentId}")
    fun getDeviceByShipmentId(
        @PathVariable shipmentId: UUID,
        authentication: Authentication
    ): ResponseEntity<List<DeviceResponse>> {
        val principal = authentication.principal as UserPrincipal

        val devices = deviceService.getDeviceByShipmentId(
            shipmentId = shipmentId,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(devices.map(DeviceResponse::from))
    }

    @GetMapping
    fun getAllDevices(
        authentication: Authentication
    ): ResponseEntity<List<DeviceResponse>> {
        val principal = authentication.principal as UserPrincipal

        val devices = deviceService.getAllDevices(
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(devices.map(DeviceResponse::from))
    }

    @PutMapping("/{id}")
    fun updateDevice(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateDeviceRequest,
        authentication: Authentication
    ): ResponseEntity<DeviceResponse> {
        val principal = authentication.principal as UserPrincipal

        val updatedDevice = deviceService.updateDevice(
            id = id,
            request = request,
            providerId = principal.userId
        )

        return ResponseEntity.ok(updatedDevice)
    }

    @PatchMapping("/{id}/status")
    fun updateDeviceStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateStatusRequest,
        authentication: Authentication
    ): ResponseEntity<DeviceResponse> {
        val principal = authentication.principal as UserPrincipal

        val updatedDevice = deviceService.updateStatus(
            deviceId = id,
            request = request,
            providerId = principal.userId
        )

        return ResponseEntity.ok(updatedDevice)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDevice(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<Unit> {
        val principal = authentication.principal as UserPrincipal

        deviceService.deleteDevice(
            id = id,
            providerId = principal.userId
        )

        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/assign/{shipmentId}")
    fun assignDeviceToShipment(
        @PathVariable id: UUID,
        @PathVariable shipmentId: UUID,
        authentication: Authentication
    ): ResponseEntity<DeviceResponse> {
        val principal = authentication.principal as UserPrincipal

        val updatedDevice = deviceService.assignToShipment(
            deviceId = id,
            shipmentId = shipmentId,
            providerId = principal.userId
        )

        return ResponseEntity.ok(updatedDevice)
    }

    @PostMapping("/{id}/release")
    fun releaseDeviceFromShipment(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<DeviceResponse> {
        val principal = authentication.principal as UserPrincipal

        val updatedDevice = deviceService.releaseFromShipment(
            deviceId = id,
            providerId = principal.userId
        )

        return ResponseEntity.ok(updatedDevice)
    }

    @PostMapping("/filter")
    fun filterDevices(
        @Valid @RequestBody request: DeviceFilterRequest,
        authentication: Authentication
    ): ResponseEntity<DeviceListResponse> {
        val principal = authentication.principal as UserPrincipal

        val response = deviceService.filterDevices(
            request = request,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(response)
    }
}