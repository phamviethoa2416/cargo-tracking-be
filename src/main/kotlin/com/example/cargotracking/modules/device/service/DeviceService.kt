package com.example.cargotracking.modules.device.service

import com.example.cargotracking.common.messaging.MessagePublisher
import com.example.cargotracking.modules.device.model.dto.request.CreateDeviceRequest
import com.example.cargotracking.modules.device.model.dto.request.DeviceFilterRequest
import com.example.cargotracking.modules.device.model.dto.request.UpdateDeviceRequest
import com.example.cargotracking.modules.device.model.dto.request.UpdateStatusRequest
import com.example.cargotracking.modules.device.model.dto.response.DeviceListResponse
import com.example.cargotracking.modules.device.model.dto.response.DeviceResponse
import com.example.cargotracking.modules.device.model.entity.Device
import com.example.cargotracking.modules.device.model.types.DeviceStatus
import com.example.cargotracking.modules.device.repository.DeviceRepository
import com.example.cargotracking.modules.device.validation.DeviceOwnershipValidator
import com.example.cargotracking.modules.device.validation.DeviceStatusValidator
import com.example.cargotracking.modules.user.model.types.UserRole
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class DeviceService(
    private val deviceRepository: DeviceRepository,
    private val deviceOwnershipValidator: DeviceOwnershipValidator,
    private val messagePublisher: MessagePublisher
) {
    @Transactional
    fun createDevice(
        request: CreateDeviceRequest,
        providerId: UUID
    ): DeviceResponse {
        deviceOwnershipValidator.validateProviderRole(providerId)

        if (deviceRepository.existsByHardwareUID(request.hardwareUID)) {
            throw IllegalStateException("Device with hardware UID '${request.hardwareUID}' already exists")
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
    fun getDeviceById(id: UUID, currentUserId: UUID, currentUserRole: UserRole): Device {
        val device = deviceRepository.findById(id)
            .orElseThrow { NoSuchElementException("Device not found with id: $id") }
        
        deviceOwnershipValidator.validateRead(device, currentUserId, currentUserRole)
        return device
    }

    @Transactional(readOnly = true)
    fun getDeviceByHardwareUID(hardwareUID: String, currentUserId: UUID, currentUserRole: UserRole): Device {
        val device = deviceRepository.findByHardwareUID(hardwareUID)
            ?: throw NoSuchElementException("Device not found with hardware UID: $hardwareUID")
        
        deviceOwnershipValidator.validateRead(device, currentUserId, currentUserRole)
        return device
    }

    @Transactional(readOnly = true)
    fun getAllDevices(currentUserId: UUID, currentUserRole: UserRole): List<Device> {
        return when (currentUserRole) {
            UserRole.ADMIN -> deviceRepository.findAll()
            UserRole.PROVIDER -> deviceRepository.findByProviderId(currentUserId)
            else -> throw IllegalStateException("Only ADMIN or PROVIDER can list all devices")
        }
    }

    @Transactional(readOnly = true)
    fun getOfflineDevices(
        thresholdMillis: Long,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): List<Device> {
        val providerIdFilter = when (currentUserRole) {
            UserRole.ADMIN -> null
            UserRole.PROVIDER -> currentUserId
            else -> throw IllegalStateException("Only ADMIN or PROVIDER can get offline devices")
        }

        val threshold = Instant.now().minusMillis(thresholdMillis)
        return deviceRepository.findOfflineDevices(threshold, providerIdFilter)
    }

    @Transactional(readOnly = true)
    fun getOnlineDevices(
        thresholdMillis: Long,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): List<Device> {
        val providerIdFilter = when (currentUserRole) {
            UserRole.ADMIN -> null
            UserRole.PROVIDER -> currentUserId
            else -> throw IllegalStateException("Only ADMIN or PROVIDER can get online devices")
        }

        val threshold = Instant.now().minusMillis(thresholdMillis)
        return deviceRepository.findOnlineDevices(threshold, providerIdFilter)
    }

    @Transactional(readOnly = true)
    fun getDeviceByStatus(
        status: DeviceStatus,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): List<Device> {
        val providerIdFilter = when (currentUserRole) {
            UserRole.ADMIN -> null
            UserRole.PROVIDER -> currentUserId
            else -> throw IllegalStateException("Only ADMIN or PROVIDER can get devices by status")
        }

        return deviceRepository.findByStatusAndProviderId(status, providerIdFilter)
    }

    @Transactional(readOnly = true)
    fun getDeviceByShipmentId(
        shipmentId: UUID,
        currentUserId: UUID,
        currentUserRole: UserRole
    ): List<Device> {
        val providerIdFilter = when (currentUserRole) {
            UserRole.ADMIN -> null
            UserRole.PROVIDER -> currentUserId
            else -> throw IllegalStateException("Only ADMIN or PROVIDER can get devices by shipment ID")
        }

        return deviceRepository.findByCurrentShipmentId(shipmentId, providerIdFilter)
    }

    @Transactional
    fun updateDevice(
        id: UUID,
        request: UpdateDeviceRequest,
        providerId: UUID
    ): DeviceResponse {
        val device = deviceRepository.findById(id)
            .orElseThrow { NoSuchElementException("Device not found with id: $id") }

        deviceOwnershipValidator.validateWrite(device, providerId)

        request.deviceName?.let(device::updateDeviceName)
        request.model?.let(device::updateModel)
        request.batteryLevel?.let(device::updateBatteryLevel)
        request.firmwareVersion?.let(device::updateFirmware)

        val savedDevice = deviceRepository.save(device)

        messagePublisher.publishDeviceConfigUpdate(savedDevice)
        
        return DeviceResponse.from(savedDevice)
    }

    @Transactional
    fun updateStatus(
        deviceId: UUID,
        request: UpdateStatusRequest,
        providerId: UUID
    ): DeviceResponse {
        val device = deviceRepository.findById(deviceId)
            .orElseThrow { NoSuchElementException("Device not found with id: $deviceId") }

        deviceOwnershipValidator.validateWrite(device, providerId)

        DeviceStatusValidator.validateTransition(device.status, request.status)

        device.updateStatus(request.status, request.shipmentId)

        val savedDevice = deviceRepository.save(device)

        messagePublisher.publishDeviceConfigUpdate(savedDevice)
        
        return DeviceResponse.from(savedDevice)
    }

    @Transactional
    fun deleteDevice(id: UUID, providerId: UUID) {
        val device = deviceRepository.findById(id)
            .orElseThrow { NoSuchElementException("Device not found with id: $id") }

        deviceOwnershipValidator.validateWrite(device, providerId)

        if (device.status == DeviceStatus.IN_TRANSIT) {
            throw IllegalStateException(
                "Cannot delete device that is currently in transit. Release from shipment first."
            )
        }

        deviceRepository.delete(device)
    }

    @Transactional
    fun assignToShipment(deviceId: UUID, shipmentId: UUID, providerId: UUID): DeviceResponse {
        val device = deviceRepository.findById(deviceId)
            .orElseThrow { NoSuchElementException("Device not found with id: $deviceId") }

        deviceOwnershipValidator.validateWrite(device, providerId)

        device.assignToShipment(shipmentId)
        val savedDevice = deviceRepository.save(device)

        messagePublisher.publishShipmentAssignment(deviceId, shipmentId, "assign")
        
        return DeviceResponse.from(savedDevice)
    }

    @Transactional
    fun releaseFromShipment(deviceId: UUID, providerId: UUID): DeviceResponse {
        val device = deviceRepository.findById(deviceId)
            .orElseThrow { NoSuchElementException("Device not found with id: $deviceId") }

        deviceOwnershipValidator.validateWrite(device, providerId)

        val shipmentId = device.currentShipmentId
        device.releaseFromShipment()
        val savedDevice = deviceRepository.save(device)

        if (shipmentId != null) {
            messagePublisher.publishShipmentAssignment(deviceId, shipmentId, "unassign")
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
            UserRole.ADMIN -> request.providerId
            UserRole.PROVIDER -> currentUserId
            else -> throw IllegalStateException("Only ADMIN or PROVIDER can filter devices")
        }

        val pageable = PageRequest.of(
            request.page - 1,
            request.pageSize,
            Sort.by(
                if (request.sortOrder.equals("asc", ignoreCase = true))
                    Sort.Direction.ASC else Sort.Direction.DESC,
                request.sortBy
            )
        )

        val page = deviceRepository.findWithFilters(
            status = request.status,
            providerId = providerIdFilter,
            minBattery = request.minBattery,
            maxBattery = request.maxBattery,
            search = request.search,
            pageable = pageable
        )

        return DeviceListResponse(
            devices = page.content.map(DeviceResponse::from),
            total = page.totalElements,
            page = request.page,
            pageSize = request.pageSize,
            totalPages = page.totalPages
        )
    }
}