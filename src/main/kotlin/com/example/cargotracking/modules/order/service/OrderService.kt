package com.example.cargotracking.modules.order.service

import com.example.cargotracking.modules.order.model.dto.request.AcceptOrderRequest
import com.example.cargotracking.modules.order.model.dto.request.CreateOrderRequest
import com.example.cargotracking.modules.order.model.dto.request.OrderFilterRequest
import com.example.cargotracking.modules.order.model.dto.request.RejectOrderRequest
import com.example.cargotracking.modules.order.model.dto.response.OrderListResponse
import com.example.cargotracking.modules.order.model.dto.response.OrderResponse
import com.example.cargotracking.modules.order.model.entity.Order
import com.example.cargotracking.modules.order.model.types.OrderStatus
import com.example.cargotracking.modules.order.repository.OrderRepository
import com.example.cargotracking.modules.shipment.model.entity.Shipment
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
            .orElseThrow { NoSuchElementException("Customer not found with id: $customerId") }

        if (customer.role != UserRole.CUSTOMER) {
            throw IllegalStateException("Only CUSTOMER can create orders")
        }

        if (!customer.isActive) {
            throw IllegalStateException("Customer account is not active")
        }

        val provider = userRepository.findById(request.providerId)
            .orElseThrow { NoSuchElementException("Provider not found with id: ${request.providerId}") }

        if (provider.role != UserRole.PROVIDER) {
            throw IllegalStateException("Provider ID must belong to a PROVIDER user")
        }

        if (!provider.isActive) {
            throw IllegalStateException("Provider account is not active")
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
    ): Order {
        val order = orderRepository.findById(id)
            .orElseThrow { NoSuchElementException("Order not found with id: $id") }

        validateReadAccess(order, currentUserId, currentUserRole)
        return order
    }

    @Transactional(readOnly = true)
    fun getAllOrders(
        currentUserId: UUID,
        currentUserRole: UserRole
    ): List<Order> {
        return when (currentUserRole) {
            UserRole.CUSTOMER -> orderRepository.findByCustomerId(currentUserId)
            UserRole.PROVIDER -> orderRepository.findByProviderId(currentUserId)
            UserRole.SHIPPER, UserRole.ADMIN -> emptyList() // Shippers and Admins don't see orders
        }
    }

    @Transactional(readOnly = true)
    fun getPendingOrders(providerId: UUID): List<Order> {
        return orderRepository.findByProviderIdAndStatus(providerId, OrderStatus.PENDING)
    }

    @Transactional(readOnly = true)
    fun getOrdersByStatus(
        status: OrderStatus,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): List<Order> {
        val allByStatus = orderRepository.findByStatus(status)

        return when (currentUserRole) {
            UserRole.CUSTOMER -> allByStatus.filter { it.customerId == currentUserId }
            UserRole.PROVIDER -> allByStatus.filter { it.providerId == currentUserId }
            UserRole.SHIPPER, UserRole.ADMIN -> emptyList()
        }
    }

    @Transactional
    fun acceptOrder(
        orderId: UUID,
        request: AcceptOrderRequest,
        providerId: UUID
    ): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { NoSuchElementException("Order not found with id: $orderId") }

        if (order.providerId != providerId) {
            throw IllegalStateException("Order does not belong to this provider")
        }

        val provider = userRepository.findById(providerId)
            .orElseThrow { NoSuchElementException("Provider not found with id: $providerId") }

        if (provider.role != UserRole.PROVIDER) {
            throw IllegalStateException("Only PROVIDER can accept orders")
        }

        // Create shipment from order
        val shipment = Shipment.create(
            customerId = order.customerId,
            providerId = order.providerId,
            goodsDescription = order.goodsDescription ?: "No description provided",
            pickupAddress = order.pickupAddress,
            deliveryAddress = order.deliveryAddress,
            estimatedDeliveryAt = order.estimatedDeliveryAt
        )
        val savedShipment = shipmentRepository.save(shipment)

        // Accept order with shipment ID
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
            .orElseThrow { NoSuchElementException("Order not found with id: $orderId") }

        if (order.providerId != providerId) {
            throw IllegalStateException("Order does not belong to this provider")
        }

        val provider = userRepository.findById(providerId)
            .orElseThrow { NoSuchElementException("Provider not found with id: $providerId") }

        if (provider.role != UserRole.PROVIDER) {
            throw IllegalStateException("Only PROVIDER can reject orders")
        }

        order.reject(request.reason)
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

        val pageable = PageRequest.of(
            request.page - 1,
            request.pageSize,
            Sort.by(
                if (request.sortOrder?.equals("asc", ignoreCase = true) == true)
                    Sort.Direction.ASC else Sort.Direction.DESC,
                request.sortBy ?: "createdAt"
            )
        )

        val page = orderRepository.findWithFilters(
            status = request.status,
            customerId = customerIdFilter,
            providerId = providerIdFilter,
            createdAfter = request.createdAfter,
            createdBefore = request.createdBefore,
            search = request.search,
            pageable = pageable
        )

        return OrderListResponse(
            orders = page.content.map(OrderResponse::from),
            total = page.totalElements,
            page = request.page,
            pageSize = request.pageSize,
            totalPages = page.totalPages
        )
    }

    private fun validateReadAccess(order: Order, currentUserId: UUID, currentUserRole: UserRole) {
        when (currentUserRole) {
            UserRole.CUSTOMER -> {
                if (order.customerId != currentUserId) {
                    throw IllegalStateException("Order does not belong to this customer")
                }
            }
            UserRole.PROVIDER -> {
                if (order.providerId != currentUserId) {
                    throw IllegalStateException("Order does not belong to this provider")
                }
            }
            UserRole.SHIPPER, UserRole.ADMIN -> {
                throw IllegalStateException("Shippers and Admins cannot access orders directly")
            }
        }
    }
}
