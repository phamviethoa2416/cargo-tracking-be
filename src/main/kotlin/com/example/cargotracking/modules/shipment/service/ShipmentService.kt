package com.example.cargotracking.modules.shipment.service

import com.example.cargotracking.common.messaging.MessagePublisher
import com.example.cargotracking.modules.device.model.types.DeviceStatus
import com.example.cargotracking.modules.device.repository.DeviceRepository
import com.example.cargotracking.modules.order.repository.OrderRepository
import com.example.cargotracking.modules.order.service.OrderService
import com.example.cargotracking.modules.shipment.model.dto.request.*
import com.example.cargotracking.modules.shipment.model.dto.response.ShipmentListResponse
import com.example.cargotracking.modules.shipment.model.dto.response.ShipmentResponse
import com.example.cargotracking.modules.shipment.model.entity.Shipment
import com.example.cargotracking.modules.shipment.model.types.ShipmentStatus
import com.example.cargotracking.modules.shipment.repository.ShipmentRepository
import com.example.cargotracking.modules.shipment.repository.ShipmentSpecification
import com.example.cargotracking.modules.user.model.types.UserRole
import com.example.cargotracking.modules.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class ShipmentService(
    private val shipmentRepository: ShipmentRepository,
    private val orderRepository: OrderRepository,
    private val orderService: OrderService,
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository,
    private val messagePublisher: MessagePublisher
) {
    @Transactional(readOnly = true)
    fun getShipmentById(
        id: UUID,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): Shipment {
        val shipment = shipmentRepository.findById(id)
            .orElseThrow { NoSuchElementException("Shipment not found with id: $id") }

        validateReadAccess(shipment, currentUserId, currentUserRole)
        return shipment
    }

    @Transactional(readOnly = true)
    fun getAllShipments(
        currentUserId: UUID,
        currentUserRole: UserRole,
        page: Int = 1,
        pageSize: Int = 20
    ): ShipmentListResponse {
        val customerIdFilter = when (currentUserRole) {
            UserRole.CUSTOMER -> currentUserId
            else -> null
        }
        val providerIdFilter = when (currentUserRole) {
            UserRole.PROVIDER -> currentUserId
            else -> null
        }
        val shipperIdFilter = when (currentUserRole) {
            UserRole.SHIPPER -> currentUserId
            else -> null
        }

        val spec = ShipmentSpecification.buildSpecification(
            customerId = customerIdFilter,
            providerId = providerIdFilter,
            shipperId = shipperIdFilter
        )

        val pageable = PageRequest.of(
            page - 1,
            pageSize,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        val resultPage = shipmentRepository.findAll(spec, pageable)

        return ShipmentListResponse(
            shipments = resultPage.content.map(ShipmentResponse::from),
            total = resultPage.totalElements,
            page = page,
            pageSize = pageSize,
            totalPages = resultPage.totalPages
        )
    }

    @Transactional
    fun updateShipment(
        id: UUID,
        request: UpdateShipmentRequest,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): ShipmentResponse {
        val shipment = shipmentRepository.findById(id)
            .orElseThrow { NoSuchElementException("Shipment not found with id: $id") }

        validateWriteAccess(shipment, currentUserId, currentUserRole)

        shipment.updateDetails(
            goodsDescription = request.goodsDescription,
            pickupAddress = request.pickupAddress,
            deliveryAddress = request.deliveryAddress,
            estimatedDeliveryAt = request.estimatedDeliveryAt
        )

        val updatedShipment = shipmentRepository.save(shipment)
        return ShipmentResponse.from(updatedShipment)
    }

    @Transactional
    fun assignShipper(
        shipmentId: UUID,
        request: AssignShipperRequest,
        providerId: UUID
    ): ShipmentResponse {
        val shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow { NoSuchElementException("Shipment not found with id: $shipmentId") }

        if (shipment.providerId != providerId) {
            throw IllegalStateException("Shipment does not belong to this provider")
        }

        val provider = userRepository.findById(providerId)
            .orElseThrow { NoSuchElementException("Provider not found with id: $providerId") }

        if (provider.role != UserRole.PROVIDER) {
            throw IllegalStateException("Only PROVIDER can assign shipper")
        }

        val shipper = userRepository.findById(request.shipperId)
            .orElseThrow { NoSuchElementException("Shipper not found with id: ${request.shipperId}") }

        if (shipper.role != UserRole.SHIPPER) {
            throw IllegalStateException("Shipper ID must belong to a SHIPPER user")
        }

        if (!shipper.isActive) {
            throw IllegalStateException("Shipper account is not active")
        }

        shipment.assignShipper(request.shipperId)
        val savedShipment = shipmentRepository.save(shipment)
        return ShipmentResponse.from(savedShipment)
    }

    @Transactional
    fun assignDevice(
        shipmentId: UUID,
        request: AssignDeviceRequest,
        providerId: UUID
    ): ShipmentResponse {
        val shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow { NoSuchElementException("Shipment not found with id: $shipmentId") }

        if (shipment.providerId != providerId) {
            throw IllegalStateException("Shipment does not belong to this provider")
        }

        if (shipment.status != ShipmentStatus.CREATED) {
            throw IllegalStateException("Only CREATED shipments can have device assigned. Current status: ${shipment.status}")
        }

        val device = deviceRepository.findById(request.deviceId)
            .orElseThrow { NoSuchElementException("Device not found with id: ${request.deviceId}") }

        if (device.providerId != providerId) {
            throw IllegalStateException("Device does not belong to this provider")
        }

        if (device.status != DeviceStatus.AVAILABLE) {
            throw IllegalStateException("Device must be AVAILABLE to be assigned. Current status: ${device.status}")
        }

        shipment.assignDevice(request.deviceId)
        device.assignToShipment(shipmentId)
        
        val savedShipment = shipmentRepository.save(shipment)
        deviceRepository.save(device)

        messagePublisher.publishShipmentAssignment(request.deviceId, shipmentId, "assign")
        
        return ShipmentResponse.from(savedShipment)
    }

    @Transactional
    fun startTransit(
        shipmentId: UUID,
        shipperId: UUID
    ): ShipmentResponse {
        val shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow { NoSuchElementException("Shipment not found with id: $shipmentId") }

        if (shipment.shipperId != shipperId) {
            throw IllegalStateException("Shipment does not belong to this shipper")
        }

        val shipper = userRepository.findById(shipperId)
            .orElseThrow { NoSuchElementException("Shipper not found with id: $shipperId") }

        if (shipper.role != UserRole.SHIPPER) {
            throw IllegalStateException("Only SHIPPER can start transit")
        }

        if (!shipper.isActive) {
            throw IllegalStateException("Shipper account is not active")
        }

        shipment.startTransit()
        
        val savedShipment = shipmentRepository.save(shipment)
        orderService.syncOrderStatusFromShipment(shipmentId)
        
        return ShipmentResponse.from(savedShipment)
    }

    @Transactional
    fun completeShipment(
        shipmentId: UUID,
        request: CompleteShipmentRequest,
        shipperId: UUID
    ): ShipmentResponse {
        val shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow { NoSuchElementException("Shipment not found with id: $shipmentId") }

        if (shipment.shipperId != shipperId) {
            throw IllegalStateException("Shipment does not belong to this shipper")
        }

        val shipper = userRepository.findById(shipperId)
            .orElseThrow { NoSuchElementException("Shipper not found with id: $shipperId") }

        if (shipper.role != UserRole.SHIPPER) {
            throw IllegalStateException("Only SHIPPER can complete shipments")
        }

        if (!shipper.isActive) {
            throw IllegalStateException("Shipper account is not active")
        }

        val deliveredAt = request.deliveredAt ?: Instant.now()
        shipment.complete(deliveredAt)

        shipment.deviceId?.let { deviceId ->
            val device = deviceRepository.findById(deviceId)
                .orElseThrow { NoSuchElementException("Device not found with id: $deviceId") }
            device.releaseFromShipment()
            deviceRepository.save(device)

            messagePublisher.publishShipmentAssignment(deviceId, shipmentId, "unassign")
        }

        val savedShipment = shipmentRepository.save(shipment)
        orderService.syncOrderStatusFromShipment(shipmentId)
        
        return ShipmentResponse.from(savedShipment)
    }

    @Transactional
    fun cancelShipment(
        shipmentId: UUID,
        request: CancelShipmentRequest,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): ShipmentResponse {
        val shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow { NoSuchElementException("Shipment not found with id: $shipmentId") }

        when (currentUserRole) {
            UserRole.CUSTOMER -> {
                if (shipment.customerId != currentUserId) {
                    throw IllegalStateException("Shipment does not belong to this customer")
                }
            }
            UserRole.PROVIDER -> {
                if (shipment.providerId != currentUserId) {
                    throw IllegalStateException("Shipment does not belong to this provider")
                }
            }
            UserRole.ADMIN, UserRole.SHIPPER -> {
                throw IllegalStateException("Only CUSTOMER or PROVIDER can cancel shipments")
            }
        }


        shipment.deviceId?.let { deviceId ->
            val device = deviceRepository.findById(deviceId)
                .orElseThrow { NoSuchElementException("Device not found with id: $deviceId") }
            device.releaseFromShipment()
            deviceRepository.save(device)

            messagePublisher.publishShipmentAssignment(deviceId, shipmentId, "unassign")
        }

        shipment.cancel()
        val savedShipment = shipmentRepository.save(shipment)
        orderService.syncOrderStatusFromShipment(shipmentId)
        
        return ShipmentResponse.from(savedShipment)
    }

    @Transactional(readOnly = true)
    fun filterShipments(
        request: ShipmentFilterRequest,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): ShipmentListResponse {
        val customerIdFilter = when (currentUserRole) {
            UserRole.CUSTOMER -> currentUserId
            else -> null
        }

        val providerIdFilter = when (currentUserRole) {
            UserRole.PROVIDER -> currentUserId
            else -> null
        }

        val shipperIdFilter = when (currentUserRole) {
            UserRole.SHIPPER -> currentUserId
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

        val spec = ShipmentSpecification.buildSpecification(
            status = request.status,
            customerId = customerIdFilter,
            providerId = providerIdFilter,
            shipperId = shipperIdFilter,
            deviceId = request.deviceId,
            createdAfter = request.createdAfter,
            createdBefore = request.createdBefore,
            search = request.search
        )

        val page = shipmentRepository.findAll(spec, pageable)

        return ShipmentListResponse(
            shipments = page.content.map(ShipmentResponse::from),
            total = page.totalElements,
            page = request.page,
            pageSize = request.pageSize,
            totalPages = page.totalPages
        )
    }

    @Transactional(readOnly = true)
    fun getShipmentsByStatus(
        status: ShipmentStatus,
        currentUserId: UUID,
        currentUserRole: UserRole,
        page: Int = 1,
        pageSize: Int = 20
    ): ShipmentListResponse {
        val customerIdFilter = when (currentUserRole) {
            UserRole.CUSTOMER -> currentUserId
            else -> null
        }
        val providerIdFilter = when (currentUserRole) {
            UserRole.PROVIDER -> currentUserId
            else -> null
        }
        val shipperIdFilter = when (currentUserRole) {
            UserRole.SHIPPER -> currentUserId
            else -> null
        }

        val spec = ShipmentSpecification.buildSpecification(
            status = status,
            customerId = customerIdFilter,
            providerId = providerIdFilter,
            shipperId = shipperIdFilter
        )

        val pageable = PageRequest.of(
            page - 1,
            pageSize,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        val resultPage = shipmentRepository.findAll(spec, pageable)

        return ShipmentListResponse(
            shipments = resultPage.content.map(ShipmentResponse::from),
            total = resultPage.totalElements,
            page = page,
            pageSize = pageSize,
            totalPages = resultPage.totalPages
        )
    }

    private fun validateReadAccess(shipment: Shipment, currentUserId: UUID, currentUserRole: UserRole) {
        when (currentUserRole) {
            UserRole.CUSTOMER -> {
                if (shipment.customerId != currentUserId) {
                    throw IllegalStateException("Shipment does not belong to this customer")
                }
            }
            UserRole.PROVIDER -> {
                if (shipment.providerId != currentUserId) {
                    throw IllegalStateException("Shipment does not belong to this provider")
                }
            }
            UserRole.SHIPPER -> {
                if (shipment.shipperId != currentUserId) {
                    throw IllegalStateException("Shipment does not belong to this shipper")
                }
            }
            UserRole.ADMIN -> {
                throw IllegalStateException("Admins cannot access shipments directly")
            }
        }
    }

    private fun validateWriteAccess(shipment: Shipment, currentUserId: UUID, currentUserRole: UserRole) {
        when (currentUserRole) {
            UserRole.CUSTOMER -> {
                if (shipment.customerId != currentUserId) {
                    throw IllegalStateException("Shipment does not belong to this customer")
                }
            }
            UserRole.PROVIDER -> {
                if (shipment.providerId != currentUserId) {
                    throw IllegalStateException("Shipment does not belong to this provider")
                }
            }
            UserRole.ADMIN, UserRole.SHIPPER -> {
                throw IllegalStateException("Only CUSTOMER or PROVIDER can modify shipments")
            }
        }
    }
}
