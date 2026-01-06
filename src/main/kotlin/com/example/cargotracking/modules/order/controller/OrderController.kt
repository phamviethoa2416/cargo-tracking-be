package com.example.cargotracking.modules.order.controller

import com.example.cargotracking.modules.order.model.dto.request.AcceptOrderRequest
import com.example.cargotracking.modules.order.model.dto.request.CreateOrderRequest
import com.example.cargotracking.modules.order.model.dto.request.OrderFilterRequest
import com.example.cargotracking.modules.order.model.dto.request.RejectOrderRequest
import com.example.cargotracking.modules.order.model.dto.response.OrderListResponse
import com.example.cargotracking.modules.order.model.dto.response.OrderResponse
import com.example.cargotracking.modules.order.model.types.OrderStatus
import com.example.cargotracking.modules.order.service.OrderService
import com.example.cargotracking.modules.user.principal.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {
    @PostMapping
    fun createOrder(
        @Valid @RequestBody request: CreateOrderRequest,
        authentication: Authentication
    ): ResponseEntity<OrderResponse> {
        val principal = authentication.principal as UserPrincipal

        val order = orderService.createOrder(
            request = request,
            customerId = principal.userId
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(order)
    }

    @GetMapping("/{id}")
    fun getOrderById(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<OrderResponse> {
        val principal = authentication.principal as UserPrincipal

        val order = orderService.getOrderById(
            id = id,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(OrderResponse.from(order))
    }

    @GetMapping
    fun getAllOrders(
        authentication: Authentication
    ): ResponseEntity<List<OrderResponse>> {
        val principal = authentication.principal as UserPrincipal

        val orders = orderService.getAllOrders(
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(orders.map(OrderResponse::from))
    }

    @GetMapping("/status/{status}")
    fun getOrdersByStatus(
        @PathVariable status: OrderStatus,
        authentication: Authentication
    ): ResponseEntity<List<OrderResponse>> {
        val principal = authentication.principal as UserPrincipal

        val orders = orderService.getOrdersByStatus(
            status = status,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(orders.map(OrderResponse::from))
    }

    @GetMapping("/pending")
    fun getPendingOrders(
        authentication: Authentication
    ): ResponseEntity<List<OrderResponse>> {
        val principal = authentication.principal as UserPrincipal

        val orders = orderService.getPendingOrders(
            providerId = principal.userId
        )

        return ResponseEntity.ok(orders.map(OrderResponse::from))
    }

    @PostMapping("/{id}/accept")
    fun acceptOrder(
        @PathVariable id: UUID,
        @RequestBody request: AcceptOrderRequest?,
        authentication: Authentication
    ): ResponseEntity<OrderResponse> {
        val principal = authentication.principal as UserPrincipal

        val order = orderService.acceptOrder(
            orderId = id,
            request = request ?: AcceptOrderRequest(),
            providerId = principal.userId
        )

        return ResponseEntity.ok(order)
    }

    @PostMapping("/{id}/reject")
    fun rejectOrder(
        @PathVariable id: UUID,
        @Valid @RequestBody request: RejectOrderRequest,
        authentication: Authentication
    ): ResponseEntity<OrderResponse> {
        val principal = authentication.principal as UserPrincipal

        val order = orderService.rejectOrder(
            orderId = id,
            request = request,
            providerId = principal.userId
        )

        return ResponseEntity.ok(order)
    }

    @PostMapping("/filter")
    fun filterOrders(
        @Valid @RequestBody request: OrderFilterRequest,
        authentication: Authentication
    ): ResponseEntity<OrderListResponse> {
        val principal = authentication.principal as UserPrincipal

        val response = orderService.filterOrders(
            request = request,
            currentUserId = principal.userId,
            currentUserRole = principal.role
        )

        return ResponseEntity.ok(response)
    }
}
