package com.example.cargotracking.modules.device.repository

import com.example.cargotracking.modules.device.model.entity.Device
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DeviceRepository : JpaRepository<Device, UUID>, JpaSpecificationExecutor<Device> {

    fun findByHardwareUID(hardwareUid: String): Device?

    fun existsByHardwareUID(hardwareUid: String): Boolean
}