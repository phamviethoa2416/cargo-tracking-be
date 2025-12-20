package com.example.cargotracking.modules.device.service

import com.example.cargotracking.modules.device.model.dto.request.CreateDeviceRequest
import com.example.cargotracking.modules.device.model.dto.request.UpdateDeviceRequest
import com.example.cargotracking.modules.device.model.dto.request.UpdateStatusRequest
import com.example.cargotracking.modules.device.model.dto.response.DeviceResponse
import com.example.cargotracking.modules.device.model.entity.Device
import com.example.cargotracking.modules.device.model.types.DeviceStatus
import com.example.cargotracking.modules.device.repository.DeviceRepository
import com.example.cargotracking.modules.device.validation.DeviceOwnershipValidator
import com.example.cargotracking.modules.device.validation.DeviceStatusValidator
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DeviceService(
    private val deviceRepository: DeviceRepository,
    private val deviceOwnershipValidator: DeviceOwnershipValidator
) {
    @Transactional
    fun createDevice(request: CreateDeviceRequest): DeviceResponse {
        if (deviceRepository.existsByHardwareUID(request.hardwareUID)) {
            throw IllegalStateException("Device with the same hardware UID already exists")
        }

//        request.providerId?.let {
//            deviceOwnershipValidator.validateShipperOwnership(it)
//        }

        val device = Device(
            hardwareUID = request.hardwareUID,
            deviceName = request.deviceName,
            model = request.model,
            providerId = request.providerId
        )

        val savedDevice = deviceRepository.save(device)
        return DeviceResponse.from(savedDevice)
    }

    @Transactional
    fun getDeviceById(id: UUID): Device =
        deviceRepository.findById(id).orElseThrow { NoSuchElementException("Device not found") }

    fun getDeviceByHardwareUID(hardwareUID: String): Device =
        deviceRepository.findByHardwareUID(hardwareUID) ?: throw NoSuchElementException("Device not found")

    fun getAllDevices(): List<Device> = deviceRepository.findAll()


    @Transactional
    fun updateDevice(id: UUID, request: UpdateDeviceRequest): DeviceResponse {
        val device = getDeviceById(id)

        request.batteryLevel.let { device.updateBatteryLevel(it) }
        request.firmwareVersion?.let { device.updateFirmware(it) }

        val updatedDevice = deviceRepository.save(device)
        return DeviceResponse.from(updatedDevice)
    }

    @Transactional
    fun deleteDevice(id: UUID) {
        val device = getDeviceById(id)

        if (device.status == DeviceStatus.IN_TRANSIT) {
            throw IllegalStateException(
                "Cannot delete device that is currently in transit. Release from shipment first."
            )
        }

        deviceRepository.delete(device)
    }

    @Transactional
    fun assignToShipment(deviceId: UUID, shipmentId: UUID): DeviceResponse {
        val device = getDeviceById(deviceId)
        device.assignToShipment(shipmentId)
        val savedDevice = deviceRepository.save(device)
        return DeviceResponse.from(savedDevice)
    }

    @Transactional
    fun releaseFromShipment(deviceId: UUID): DeviceResponse {
        val device = getDeviceById(deviceId)
        device.releaseFromShipment()
        val savedDevice = deviceRepository.save(device)
        return DeviceResponse.from(savedDevice)
    }

    @Transactional
    fun updateStatus(deviceId: UUID, request: UpdateStatusRequest): DeviceResponse {
        val device = getDeviceById(deviceId)

        DeviceStatusValidator.validateTransition(device.status, request.status)

        val shipmentId = when {
            request.status == DeviceStatus.IN_TRANSIT -> {
                request.shipmentId ?: device.currentShipmentId
                    ?: throw IllegalArgumentException("Shipment ID is required when setting status to IN_TRANSIT")
            }
            else -> null
        }
        
        device.updateStatus(request.status, shipmentId)
        val savedDevice = deviceRepository.save(device)
        return DeviceResponse.from(savedDevice)
    }
}