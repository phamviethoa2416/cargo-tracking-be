package com.example.cargotracking.modules.order.controller

import com.example.cargotracking.modules.order.model.dto.request.order.*
import com.example.cargotracking.modules.order.model.dto.request.provider.*
import com.example.cargotracking.modules.order.model.dto.response.OrderListResponse
import com.example.cargotracking.modules.order.model.dto.response.OrderResponse
import com.example.cargotracking.modules.order.model.types.OrderStatus
import com.example.cargotracking.modules.order.service.OrderService
import com.example.cargotracking.modules.user.principal.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    fun createOrder(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: CreateOrderRequest
    ): ResponseEntity<OrderResponse> {
        val order = orderService.createOrder(
            request = request,
            customerId = principal.userId
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(order)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    fun getOrderById(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<OrderResponse> {
        val order = orderService.getOrderById(
            id = id,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(order)
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    fun getAllOrders(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int
    ): ResponseEntity<OrderListResponse> {
        val response = orderService.getAllOrders(
            currentUserId = principal.userId,
            currentUserRole = principal.role,
            page = page,
            pageSize = pageSize
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    fun getOrdersByStatus(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable status: OrderStatus
    ): ResponseEntity<List<OrderResponse>> {
        val orders = orderService.getOrdersByStatus(
            status = status,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(orders)
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('PROVIDER')")
    fun getPendingOrders(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<OrderResponse>> {
        val orders = orderService.getPendingOrders(
            providerId = principal.userId
        )

        return ResponseEntity.ok(orders)
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasRole('PROVIDER')")
    fun acceptOrder(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @RequestBody request: AcceptOrderRequest?
    ): ResponseEntity<OrderResponse> {
        val order = orderService.acceptOrder(
            orderId = id,
            request = request ?: AcceptOrderRequest(),
            providerId = principal.userId
        )

        return ResponseEntity.ok(order)
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('PROVIDER')")
    fun rejectOrder(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: RejectOrderRequest
    ): ResponseEntity<OrderResponse> {
        val order = orderService.rejectOrder(
            orderId = id,
            request = request,
            providerId = principal.userId
        )

        return ResponseEntity.ok(order)
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    fun cancelOrder(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: CancelOrderRequest
    ): ResponseEntity<OrderResponse> {
        val order = orderService.cancelOrder(
            orderId = id,
            request = request,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(order)
    }

    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    fun filterOrders(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: OrderFilterRequest
    ): ResponseEntity<OrderListResponse> {
        val response = orderService.filterOrders(
            request = request,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(response)
    }
}
