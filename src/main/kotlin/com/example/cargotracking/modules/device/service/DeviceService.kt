package com.example.cargotracking.modules.device.service

import com.example.cargotracking.modules.device.model.dto.request.*
import com.example.cargotracking.modules.device.model.dto.response.*
import com.example.cargotracking.modules.device.model.entity.Device
import com.example.cargotracking.modules.device.model.types.DeviceStatus
import com.example.cargotracking.modules.device.repository.DeviceRepository
import com.example.cargotracking.modules.device.exception.DeviceException
import com.example.cargotracking.modules.device.messaging.publisher.DevicePublisher
import com.example.cargotracking.modules.device.repository.DeviceSpecification
import com.example.cargotracking.modules.shipment.messaging.publisher.ShipmentPublisher
import com.example.cargotracking.modules.user.model.types.UserRole
import com.example.cargotracking.modules.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class DeviceService(
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository,
    private val devicePublisher: DevicePublisher,
    private val shipmentPublisher: ShipmentPublisher
) {
    @Transactional
    fun createDevice(
        request: CreateDeviceRequest,
        providerId: UUID
    ): DeviceResponse {
        validateProviderRole(providerId)

        if (deviceRepository.existsByHardwareUID(request.hardwareUID)) {
            throw DeviceException.DeviceAlreadyExistsException("Device with hardware UID '${request.hardwareUID}' already exists")
        }

        val device = Device.create(
            hardwareUID = request.hardwareUID,
            providerId = providerId,
            deviceName = request.deviceName,
            model = request.model
        )

        request.firmwareVersion?.let(device::updateFirmware)

        return DeviceResponse.from(deviceRepository.save(device))
    }

    @Transactional(readOnly = true)
    fun getDeviceById(id: UUID, currentUserId: UUID, currentUserRole: UserRole): DeviceResponse {
        val device = deviceRepository.findById(id)
            .orElseThrow { DeviceException.DeviceNotFoundException("Device not found with id: $id") }
        
        validateRead(device, currentUserId, currentUserRole)
        return DeviceResponse.from(device)
    }

    @Transactional(readOnly = true)
    fun getDeviceByHardwareUID(hardwareUID: String, currentUserId: UUID, currentUserRole: UserRole): DeviceResponse {
        val device = deviceRepository.findByHardwareUID(hardwareUID)
            ?: throw DeviceException.DeviceNotFoundException("Device not found with hardware UID: $hardwareUID")
        
        validateRead(device, currentUserId, currentUserRole)
        return DeviceResponse.from(device)
    }

    @Transactional(readOnly = true)
    fun getAllDevices(currentUserId: UUID, currentUserRole: UserRole): List<DeviceResponse> {
        val spec = when (currentUserRole) {
            UserRole.ADMIN -> DeviceSpecification.buildSpecification()
            UserRole.PROVIDER -> DeviceSpecification.buildSpecification(providerId = currentUserId)
            else -> throw DeviceException.InvalidUserRoleException("Only ADMIN or PROVIDER can list all devices")
        }
        return deviceRepository.findAll(spec, Pageable.unpaged()).content.map(DeviceResponse::from)
    }

    @Transactional(readOnly = true)
    fun getOfflineDevices(
        thresholdMillis: Long,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): List<DeviceResponse> {
        val providerIdFilter = when (currentUserRole) {
            UserRole.ADMIN -> null
            UserRole.PROVIDER -> currentUserId
            else -> throw DeviceException.InvalidUserRoleException("Only ADMIN or PROVIDER can get offline devices")
        }

        val threshold = Instant.now().minusMillis(thresholdMillis)

        val spec = DeviceSpecification.buildSpecification(
            providerId = providerIdFilter,
            lastSeenBefore = threshold
        )

        return deviceRepository.findAll(spec).map(DeviceResponse::from)
    }

    @Transactional(readOnly = true)
    fun getOnlineDevices(
        thresholdMillis: Long,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): List<DeviceResponse> {
        val providerIdFilter = when (currentUserRole) {
            UserRole.ADMIN -> null
            UserRole.PROVIDER -> currentUserId
            else -> throw DeviceException.InvalidUserRoleException("Only ADMIN or PROVIDER can get online devices")
        }

        val threshold = Instant.now().minusMillis(thresholdMillis)

        val spec = DeviceSpecification.buildSpecification(
            providerId = providerIdFilter,
            lastSeenAfter = threshold
        )

        return deviceRepository.findAll(spec).map(DeviceResponse::from)
    }

    @Transactional(readOnly = true)
    fun getDeviceByStatus(
        status: DeviceStatus,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): List<DeviceResponse> {
        val providerIdFilter = when (currentUserRole) {
            UserRole.ADMIN -> null
            UserRole.PROVIDER -> currentUserId
            else -> throw DeviceException.InvalidUserRoleException("Only ADMIN or PROVIDER can get devices by status")
        }

        val spec = DeviceSpecification.buildSpecification(
            status = status,
            providerId = providerIdFilter
        )

        return deviceRepository.findAll(spec).map(DeviceResponse::from)
    }

    @Transactional(readOnly = true)
    fun getDeviceByShipmentId(
        shipmentId: UUID,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): List<DeviceResponse> {
        val providerIdFilter = when (currentUserRole) {
            UserRole.ADMIN -> null
            UserRole.PROVIDER -> currentUserId
            else -> throw DeviceException.InvalidUserRoleException("Only ADMIN or PROVIDER can get devices by shipment ID")
        }

        val spec = DeviceSpecification.buildSpecification(
            currentShipmentId = shipmentId,
            providerId = providerIdFilter
        )

        return deviceRepository.findAll(spec).map(DeviceResponse::from)
    }

    @Transactional
    fun updateDevice(
        id: UUID,
        request: UpdateDeviceRequest,
        providerId: UUID
    ): DeviceResponse {
        val device = deviceRepository.findById(id)
            .orElseThrow { DeviceException.DeviceNotFoundException("Device not found with id: $id") }

        validateWrite(device, providerId)

        request.deviceName?.let(device::updateDeviceName)
        request.model?.let(device::updateModel)
        request.batteryLevel?.let(device::updateBatteryLevel)
        request.firmwareVersion?.let(device::updateFirmware)

        val savedDevice = deviceRepository.save(device)

        devicePublisher.publishDeviceConfigUpdate(savedDevice)
        
        return DeviceResponse.from(savedDevice)
    }

    @Transactional
    fun deleteDevice(id: UUID, providerId: UUID) {
        val device = deviceRepository.findById(id)
            .orElseThrow { DeviceException.DeviceNotFoundException("Device not found with id: $id") }

        validateWrite(device, providerId)

        if (device.status == DeviceStatus.IN_USE) {
            throw DeviceException.DeviceInvalidStateException(
                "Cannot delete device that is currently in transit. Release from shipment first."
            )
        }

        deviceRepository.delete(device)
    }

    @Transactional
    fun assignToShipment(deviceId: UUID, shipmentId: UUID, providerId: UUID): DeviceResponse {
        val device = deviceRepository.findById(deviceId)
            .orElseThrow { DeviceException.DeviceNotFoundException("Device not found with id: $deviceId") }

        validateWrite(device, providerId)

        device.assignToShipment(shipmentId)
        val savedDevice = deviceRepository.save(device)

        shipmentPublisher.publishShipmentAssignment(deviceId, shipmentId, "assign")
        
        return DeviceResponse.from(savedDevice)
    }

    @Transactional
    fun releaseFromShipment(deviceId: UUID, providerId: UUID): DeviceResponse {
        val device = deviceRepository.findById(deviceId)
            .orElseThrow { DeviceException.DeviceNotFoundException("Device not found with id: $deviceId") }

        validateWrite(device, providerId)

        val shipmentId = device.currentShipmentId
        device.releaseFromShipment()
        val savedDevice = deviceRepository.save(device)

        if (shipmentId != null) {
            shipmentPublisher.publishShipmentAssignment(deviceId, shipmentId, "unassign")
        }
        
        return DeviceResponse.from(savedDevice)
    }

    @Transactional(readOnly = true)
    fun filterDevices(
        request: DeviceFilterRequest,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): DeviceListResponse {
        val providerIdFilter = when (currentUserRole) {
            UserRole.PROVIDER -> currentUserId
            else -> null
        }

        val offlineThreshold = Instant.now().minusMillis(300_000L) // 5 minutes
        val lastSeenBefore = if (request.isOffline == true) offlineThreshold else null
        val lastSeenAfter = if (request.isOffline == false) offlineThreshold else null

        val specification = DeviceSpecification.buildSpecification(
            status = request.status,
            providerId = providerIdFilter,
            minBattery = request.minBattery,
            maxBattery = request.maxBattery,
            lastSeenBefore = lastSeenBefore,
            lastSeenAfter = lastSeenAfter,
            search = request.search
        )

        val pageable = PageRequest.of(
            request.page - 1,
            request.pageSize,
            Sort.by(
                if (request.sortOrder.equals("asc", ignoreCase = true))
                    Sort.Direction.ASC else Sort.Direction.DESC,
                request.sortBy
            )
        )

        val page = deviceRepository.findAll(specification, pageable)

        return DeviceListResponse(
            devices = page.content.map(DeviceResponse::from),
            total = page.totalElements,
            page = request.page,
            pageSize = request.pageSize,
            totalPages = page.totalPages
        )
    }

    private fun validateProviderRole(providerId: UUID) {
        val user = userRepository.findById(providerId)
            .orElseThrow { DeviceException.UserNotFoundException("User not found with id: $providerId") }

        if (user.role != UserRole.PROVIDER) {
            throw DeviceException.InvalidUserRoleException("Only PROVIDER role can perform this operation. Current role: ${user.role}")
        }

        if (!user.isActive) {
            throw DeviceException.UserAccountInactiveException("Provider account is not active")
        }
    }

    private fun validateWrite(device: Device, currentUserId: UUID) {
        validateProviderRole(currentUserId)

        if (device.providerId != currentUserId) {
            throw DeviceException.DeviceAccessDeniedException(
                "Device does not belong to this provider. Device provider: ${device.providerId}, Current user: $currentUserId"
            )
        }
    }

    private fun validateRead(device: Device, currentUserId: UUID, currentUserRole: UserRole) {
        if (currentUserRole == UserRole.ADMIN) {
            return
        }

        if (currentUserRole == UserRole.PROVIDER) {
            if (device.providerId != currentUserId) {
                throw DeviceException.DeviceAccessDeniedException(
                    "Device does not belong to this provider. Device provider: ${device.providerId}, Current user: $currentUserId"
                )
            }
        } else {
            throw DeviceException.InvalidUserRoleException("Only ADMIN or PROVIDER can read device information")
        }
    }
}