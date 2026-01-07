package com.example.cargotracking.modules.shipment.controller

import com.example.cargotracking.modules.shipment.model.dto.request.*
import com.example.cargotracking.modules.shipment.model.dto.response.ShipmentListResponse
import com.example.cargotracking.modules.shipment.model.dto.response.ShipmentResponse
import com.example.cargotracking.modules.shipment.model.types.ShipmentStatus
import com.example.cargotracking.modules.shipment.service.ShipmentService
import com.example.cargotracking.modules.user.principal.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/shipments")
class ShipmentController(
    private val shipmentService: ShipmentService
) {
    @PostMapping
    fun createShipment(
        @Valid @RequestBody request: CreateShipmentRequest,
        authentication: Authentication
    ): ResponseEntity<ShipmentResponse> {
        val principal = authentication.principal as UserPrincipal

        val shipment = shipmentService.createShipment(
            request = request,
            customerId = principal.userId
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(shipment)
    }

    @GetMapping("/{id}")
    fun getShipmentById(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<ShipmentResponse> {
        val principal = authentication.principal as UserPrincipal

        val shipment = shipmentService.getShipmentById(
            id = id,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(ShipmentResponse.from(shipment))
    }

    @GetMapping
    fun getAllShipments(
        authentication: Authentication
    ): ResponseEntity<List<ShipmentResponse>> {
        val principal = authentication.principal as UserPrincipal

        val shipments = shipmentService.getAllShipments(
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(shipments.map(ShipmentResponse::from))
    }

    @GetMapping("/status/{status}")
    fun getShipmentsByStatus(
        @PathVariable status: ShipmentStatus,
        authentication: Authentication
    ): ResponseEntity<List<ShipmentResponse>> {
        val principal = authentication.principal as UserPrincipal

        val shipments = shipmentService.getShipmentsByStatus(
            status = status,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(shipments.map(ShipmentResponse::from))
    }

    @PutMapping("/{id}")
    fun updateShipment(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateShipmentRequest,
        authentication: Authentication
    ): ResponseEntity<ShipmentResponse> {
        val principal = authentication.principal as UserPrincipal

        val shipment = shipmentService.updateShipment(
            id = id,
            request = request,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(shipment)
    }

    @PostMapping("/{id}/assign-shipper")
    fun assignShipper(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AssignShipperRequest,
        authentication: Authentication
    ): ResponseEntity<ShipmentResponse> {
        val principal = authentication.principal as UserPrincipal

        val shipment = shipmentService.assignShipper(
            shipmentId = id,
            request = request,
            providerId = principal.userId
        )

        return ResponseEntity.ok(shipment)
    }

    @PostMapping("/{id}/assign-device")
    fun assignDevice(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AssignDeviceRequest,
        authentication: Authentication
    ): ResponseEntity<ShipmentResponse> {
        val principal = authentication.principal as UserPrincipal

        val shipment = shipmentService.assignDevice(
            shipmentId = id,
            request = request,
            providerId = principal.userId
        )

        return ResponseEntity.ok(shipment)
    }

    @PostMapping("/{id}/start-transit")
    fun startTransit(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<ShipmentResponse> {
        val principal = authentication.principal as UserPrincipal

        val shipment = shipmentService.startTransit(
            shipmentId = id,
            shipperId = principal.userId
        )

        return ResponseEntity.ok(shipment)
    }

    @PostMapping("/{id}/complete")
    fun completeShipment(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CompleteShipmentRequest,
        authentication: Authentication
    ): ResponseEntity<ShipmentResponse> {
        val principal = authentication.principal as UserPrincipal

        val shipment = shipmentService.completeShipment(
            shipmentId = id,
            request = request,
            customerId = principal.userId
        )

        return ResponseEntity.ok(shipment)
    }

    @PostMapping("/{id}/cancel")
    fun cancelShipment(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CancelShipmentRequest,
        authentication: Authentication
    ): ResponseEntity<ShipmentResponse> {
        val principal = authentication.principal as UserPrincipal

        val shipment = shipmentService.cancelShipment(
            shipmentId = id,
            request = request,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(shipment)
    }

    @PostMapping("/filter")
    fun filterShipments(
        @Valid @RequestBody request: ShipmentFilterRequest,
        authentication: Authentication
    ): ResponseEntity<ShipmentListResponse> {
        val principal = authentication.principal as UserPrincipal

        val response = shipmentService.filterShipments(
            request = request,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(response)
    }
}
