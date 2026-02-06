package com.example.cargotracking.modules.shipment.controller

import com.example.cargotracking.modules.shipment.model.dto.request.*
import com.example.cargotracking.modules.shipment.model.dto.response.ShipmentListResponse
import com.example.cargotracking.modules.shipment.model.dto.response.ShipmentResponse
import com.example.cargotracking.modules.shipment.service.ShipmentService
import com.example.cargotracking.modules.user.principal.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/shipments")
class ShipmentController(
    private val shipmentService: ShipmentService
) {
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'SHIPPER')")
    fun getShipmentById(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ShipmentResponse> {
        val shipment = shipmentService.getShipmentById(
            id = id,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(ShipmentResponse.from(shipment))
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'SHIPPER')")
    fun getAllShipments(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ShipmentListResponse> {
        val response = shipmentService.getAllShipments(
            currentUserId = principal.userId,
            currentUserRole = principal.role,
            page = page,
            pageSize = pageSize
        )

        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    fun updateShipment(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateShipmentRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ShipmentResponse> {
        val shipment = shipmentService.updateShipment(
            id = id,
            request = request,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(shipment)
    }

    @PatchMapping("/{id}/assign-shipper")
    @PreAuthorize("hasRole('PROVIDER')")
    fun assignShipper(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AssignShipperRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ShipmentResponse> {
        val shipment = shipmentService.assignShipper(
            shipmentId = id,
            request = request,
            providerId = principal.userId
        )

        return ResponseEntity.ok(shipment)
    }

    @PatchMapping("/{id}/assign-device")
    @PreAuthorize("hasRole('PROVIDER')")
    fun assignDevice(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AssignDeviceRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ShipmentResponse> {
        val shipment = shipmentService.assignDevice(
            shipmentId = id,
            request = request,
            providerId = principal.userId
        )

        return ResponseEntity.ok(shipment)
    }

    @PatchMapping("/{id}/start-transit")
    @PreAuthorize("hasRole('SHIPPER')")
    fun startTransit(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ShipmentResponse> {
        val shipment = shipmentService.startTransit(
            shipmentId = id,
            shipperId = principal.userId
        )

        return ResponseEntity.ok(shipment)
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('SHIPPER')")
    fun completeShipment(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CompleteShipmentRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ShipmentResponse> {
        val shipment = shipmentService.completeShipment(
            shipmentId = id,
            request = request,
            shipperId = principal.userId
        )

        return ResponseEntity.ok(shipment)
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    fun cancelShipment(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CancelShipmentRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ShipmentResponse> {
        val shipment = shipmentService.cancelShipment(
            shipmentId = id,
            request = request,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(shipment)
    }

    @PatchMapping("/{id}/fail")
    @PreAuthorize("hasRole('SHIPPER')")
    fun failShipment(
        @PathVariable id: UUID,
        @Valid @RequestBody request: FailShipmentRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ShipmentResponse> {
        val shipment = shipmentService.failShipment(
            shipmentId = id,
            request = request,
            shipperId = principal.userId
        )

        return ResponseEntity.ok(shipment)
    }

    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'SHIPPER')")
    fun filterShipments(
        @Valid @RequestBody request: ShipmentFilterRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ShipmentListResponse> {
        val response = shipmentService.filterShipments(
            request = request,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(response)
    }
}
