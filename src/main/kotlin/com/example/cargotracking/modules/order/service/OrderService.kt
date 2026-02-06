package com.example.cargotracking.modules.order.service

import com.example.cargotracking.modules.order.exception.OrderException
import com.example.cargotracking.modules.order.model.dto.request.order.*
import com.example.cargotracking.modules.order.model.dto.request.provider.*
import com.example.cargotracking.modules.order.model.dto.response.OrderListResponse
import com.example.cargotracking.modules.order.model.dto.response.OrderResponse
import com.example.cargotracking.modules.order.model.entity.Order
import com.example.cargotracking.modules.order.model.types.OrderStatus
import com.example.cargotracking.modules.order.repository.OrderRepository
import com.example.cargotracking.modules.order.repository.OrderSpecification
import com.example.cargotracking.modules.shipment.model.entity.Shipment
import com.example.cargotracking.modules.shipment.model.types.ShipmentStatus
import com.example.cargotracking.modules.shipment.repository.ShipmentRepository
import com.example.cargotracking.modules.user.model.types.UserRole
import com.example.cargotracking.modules.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val shipmentRepository: ShipmentRepository
) {
    @Transactional
    fun createOrder(
        request: CreateOrderRequest,
        customerId: UUID
    ): OrderResponse {
        val customer = userRepository.findById(customerId)
            .orElseThrow { OrderException.UserNotFoundException("Customer not found with id: $customerId") }

        if (customer.role != UserRole.CUSTOMER) {
            throw OrderException.InvalidUserRoleException("Only CUSTOMER can create orders")
        }

        if (!customer.isActive) {
            throw OrderException.UserAccountInactiveException("Customer account is not active")
        }

        val provider = userRepository.findById(request.providerId)
            .orElseThrow { OrderException.UserNotFoundException("Provider not found with id: ${request.providerId}") }

        if (provider.role != UserRole.PROVIDER) {
            throw OrderException.InvalidUserRoleException("Provider ID must belong to a PROVIDER user")
        }

        if (!provider.isActive) {
            throw OrderException.UserAccountInactiveException("Provider account is not active")
        }

        val order = Order.create(
            customerId = customerId,
            providerId = request.providerId,
            goodsDescription = request.goodsDescription,
            pickupAddress = request.pickupAddress,
            deliveryAddress = request.deliveryAddress,
            estimatedDeliveryAt = request.estimatedDeliveryAt,
            requireTemperatureTracking = request.requireTemperatureTracking,
            minTemperature = request.minTemperature,
            maxTemperature = request.maxTemperature,
            requireHumidityTracking = request.requireHumidityTracking,
            minHumidity = request.minHumidity,
            maxHumidity = request.maxHumidity,
            requireLocationTracking = request.requireLocationTracking,
            specialRequirements = request.specialRequirements
        )

        val savedOrder = orderRepository.save(order)
        return OrderResponse.from(savedOrder)
    }

    @Transactional(readOnly = true)
    fun getOrderById(
        id: UUID,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): OrderResponse {
        val order = orderRepository.findById(id)
            .orElseThrow { OrderException.OrderNotFoundException("Order not found with id: $id") }

        validateReadAccess(order, currentUserId, currentUserRole)
        return OrderResponse.from(order)
    }

    @Transactional(readOnly = true)
    fun getAllOrders(
        currentUserId: UUID,
        currentUserRole: UserRole,
        page: Int = 1,
        pageSize: Int = 20
    ): OrderListResponse {
        val customerIdFilter = when (currentUserRole) {
            UserRole.CUSTOMER -> currentUserId
            else -> null
        }
        val providerIdFilter = when (currentUserRole) {
            UserRole.PROVIDER -> currentUserId
            else -> null
        }

        val spec = OrderSpecification.buildSpecification(
            customerId = customerIdFilter,
            providerId = providerIdFilter
        )

        val pageable = PageRequest.of(
            page - 1,
            pageSize,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        val ordersPage = orderRepository.findAll(spec, pageable)

        return OrderListResponse(
            orders = ordersPage.content.map(OrderResponse::from),
            total = ordersPage.totalElements,
            page = page,
            pageSize = pageSize,
            totalPages = ordersPage.totalPages
        )
    }

    @Transactional(readOnly = true)
    fun getPendingOrders(providerId: UUID): List<OrderResponse> {
        val orders = orderRepository.findByProviderIdAndStatus(providerId, OrderStatus.PENDING)
        return orders.map(OrderResponse::from)
    }

    @Transactional(readOnly = true)
    fun getOrdersByStatus(
        status: OrderStatus,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): List<OrderResponse> {
        val customerIdFilter = when (currentUserRole) {
            UserRole.CUSTOMER -> currentUserId
            else -> null
        }
        val providerIdFilter = when (currentUserRole) {
            UserRole.PROVIDER -> currentUserId
            else -> null
        }

        val spec = OrderSpecification.buildSpecification(
            status = status,
            customerId = customerIdFilter,
            providerId = providerIdFilter
        )

        val orders = orderRepository.findAll(spec)
        return orders.map(OrderResponse::from)
    }

    @Transactional
    fun acceptOrder(
        orderId: UUID,
        request: AcceptOrderRequest,
        providerId: UUID
    ): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderException.OrderNotFoundException("Order not found with id: $orderId") }

        if (!order.canBeAcceptedBy(providerId)) {
            throw OrderException.OrderInvalidStateException("Order cannot be accepted. Order status: ${order.status}, Provider ID mismatch: ${order.providerId} != $providerId")
        }

        val provider = userRepository.findById(providerId)
            .orElseThrow { OrderException.UserNotFoundException("Provider not found with id: $providerId") }

        if (provider.role != UserRole.PROVIDER) {
            throw OrderException.InvalidUserRoleException("Only PROVIDER can accept orders")
        }

        if (!provider.isActive) {
            throw OrderException.UserAccountInactiveException("Provider account is not active")
        }

        val shipment = Shipment.create(
            customerId = order.customerId,
            providerId = order.providerId,
            goodsDescription = order.goodsDescription,
            pickupAddress = order.pickupAddress,
            deliveryAddress = order.deliveryAddress,
            estimatedDeliveryAt = order.estimatedDeliveryAt
        )
        val savedShipment = shipmentRepository.save(shipment)

        order.accept(savedShipment.id)
        val savedOrder = orderRepository.save(order)

        return OrderResponse.from(savedOrder)
    }

    @Transactional
    fun rejectOrder(
        orderId: UUID,
        request: RejectOrderRequest,
        providerId: UUID
    ): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderException.OrderNotFoundException("Order not found with id: $orderId") }

        if (!order.canBeRejectedBy(providerId)) {
            throw OrderException.OrderInvalidStateException("Order cannot be rejected. Order status: ${order.status}, Provider ID mismatch: ${order.providerId} != $providerId")
        }

        val provider = userRepository.findById(providerId)
            .orElseThrow { OrderException.UserNotFoundException("Provider not found with id: $providerId") }

        if (provider.role != UserRole.PROVIDER) {
            throw OrderException.InvalidUserRoleException("Only PROVIDER can reject orders")
        }

        if (!provider.isActive) {
            throw OrderException.UserAccountInactiveException("Provider account is not active")
        }

        val trimmedReason = request.reason.trim()
        require(trimmedReason.length >= 5) {
            "Rejection reason must be at least 5 characters to provide meaningful feedback"
        }

        order.reject(trimmedReason)
        val savedOrder = orderRepository.save(order)

        return OrderResponse.from(savedOrder)
    }

    @Transactional
    fun cancelOrder(
        orderId: UUID,
        request: CancelOrderRequest,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderException.OrderNotFoundException("Order not found with id: $orderId") }

        if (!order.canBeCancelledBy(currentUserId, currentUserRole)) {
            throw OrderException.OrderInvalidStateException("Order cannot be cancelled. Order status: ${order.status}, User: $currentUserId, Role: $currentUserRole")
        }

        val user = userRepository.findById(currentUserId)
            .orElseThrow { OrderException.UserNotFoundException("User not found with id: $currentUserId") }

        if (!user.isActive) {
            throw OrderException.UserAccountInactiveException("User account is not active")
        }

        order.shipmentId?.let { shipmentId ->
            val shipment = shipmentRepository.findById(shipmentId).orElse(null)
            shipment?.let {
                if (it.status != ShipmentStatus.COMPLETED && it.status != ShipmentStatus.CANCELLED) {
                    it.cancel()
                    shipmentRepository.save(it)
                }
            }
        }

        order.cancel(request.reason)
        val savedOrder = orderRepository.save(order)

        return OrderResponse.from(savedOrder)
    }

    @Transactional(readOnly = true)
    fun filterOrders(
        request: OrderFilterRequest,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): OrderListResponse {
        val customerIdFilter = when (currentUserRole) {
            UserRole.CUSTOMER -> currentUserId
            else -> null
        }

        val providerIdFilter = when (currentUserRole) {
            UserRole.PROVIDER -> currentUserId
            else -> null
        }

        val specification = OrderSpecification.buildSpecification(
            status = request.status,
            customerId = customerIdFilter ?: request.customerId,
            providerId = providerIdFilter ?: request.providerId,
            createdAfter = request.createdAfter,
            createdBefore = request.createdBefore,
            search = request.search
        )

        val pageable = PageRequest.of(
            request.page - 1,
            request.pageSize,
            Sort.by(
                if (request.sortOrder?.equals("asc", ignoreCase = true) == true)
                    Sort.Direction.ASC else Sort.Direction.DESC,
                request.sortBy ?: "createdAt"
            )
        )

        val page = orderRepository.findAll(specification, pageable)

        return OrderListResponse(
            orders = page.content.map(OrderResponse::from),
            total = page.totalElements,
            page = request.page,
            pageSize = request.pageSize,
            totalPages = page.totalPages
        )
    }

    @Transactional
    fun syncOrderStatusFromShipment(shipmentId: UUID) {
        val orderOpt = orderRepository.findByShipmentId(shipmentId)
        if (!orderOpt.isPresent) {
            return
        }
        val order = orderOpt.get()

        val shipmentOpt = shipmentRepository.findById(shipmentId)
        if (!shipmentOpt.isPresent) {
            return
        }
        val shipment = shipmentOpt.get()

        when (shipment.status) {
            ShipmentStatus.IN_TRANSIT -> {
                if (order.status == OrderStatus.ACCEPTED) {
                    order.markInProgress()
                    orderRepository.save(order)
                }
            }
            ShipmentStatus.COMPLETED -> {
                if (order.status == OrderStatus.IN_PROGRESS) {
                    order.markCompleted()
                    orderRepository.save(order)
                }
            }
            ShipmentStatus.CANCELLED -> {
                if (order.status !in listOf(OrderStatus.REJECTED, OrderStatus.CANCELLED, OrderStatus.COMPLETED)) {
                    order.cancel("Shipment was cancelled")
                    orderRepository.save(order)
                }
            }
            ShipmentStatus.FAILED -> {
                if (order.status !in listOf(OrderStatus.REJECTED, OrderStatus.CANCELLED, OrderStatus.COMPLETED)) {
                    order.cancel("Shipment failed")
                    orderRepository.save(order)
                }
            }
            else -> {
                // No status change needed for other shipment statuses
            }
        }
    }

    private fun validateReadAccess(order: Order, currentUserId: UUID, currentUserRole: UserRole) {
        when (currentUserRole) {
            UserRole.CUSTOMER -> {
                if (order.customerId != currentUserId) {
                    throw OrderException.OrderAccessDeniedException("Order does not belong to this customer")
                }
            }
            UserRole.PROVIDER -> {
                if (order.providerId != currentUserId) {
                    throw OrderException.OrderAccessDeniedException("Order does not belong to this provider")
                }
            }
            UserRole.SHIPPER, UserRole.ADMIN -> {
                throw OrderException.OrderAccessDeniedException("Shippers and Admins cannot access orders directly")
            }
        }
    }
}
