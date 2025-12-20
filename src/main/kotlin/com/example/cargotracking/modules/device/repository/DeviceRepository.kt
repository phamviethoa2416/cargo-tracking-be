package com.example.cargotracking.modules.device.repository

import com.example.cargotracking.modules.device.model.entity.Device
import com.example.cargotracking.modules.device.model.types.DeviceStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface DeviceRepository : JpaRepository<Device, UUID> {
    fun findByHardwareUID(hardwareUid: String): Device?

    fun existsByHardwareUID(hardwareUid: String): Boolean

    fun findByProviderId(providerId: UUID): List<Device>

    @Query("SELECT d FROM Device d WHERE d._status = :status")
    fun findByStatus(@Param("status") status: DeviceStatus): List<Device>

    fun findByProviderIdAndStatus(
        @Param("providerId") providerId: UUID,
        @Param("status") status: DeviceStatus
    ): List<Device>

    @Query("SELECT d FROM Device d WHERE d._currentShipmentId = :shipmentId")
    fun findByCurrentShipmentId(@Param("shipmentId") shipmentId: UUID): List<Device>

    @Query("SELECT d FROM Device d WHERE d._batteryLevel IS NOT NULL AND d._batteryLevel BETWEEN :minBattery AND :maxBattery")
    fun findByBatteryLevelBetween(
        @Param("minBattery") minBattery: Int,
        @Param("maxBattery") maxBattery: Int
    ): List<Device>

    @Query("SELECT d FROM Device d WHERE d._lastSeenAt IS NULL OR d._lastSeenAt < :threshold")
    fun findOfflineDevices(@Param("threshold") threshold: Instant): List<Device>
    @Query("SELECT d FROM Device d WHERE d._lastSeenAt IS NOT NULL AND d._lastSeenAt >= :threshold")
    fun findOnlineDevices(@Param("threshold") threshold: Instant): List<Device>

    @Query("""
        SELECT d FROM Device d WHERE 
        (:status IS NULL OR d._status = :status) AND 
        (:providerId IS NULL OR d.providerId = :providerId) AND 
        (:minBattery IS NULL OR d._batteryLevel IS NULL OR d._batteryLevel >= :minBattery) AND 
        (:maxBattery IS NULL OR d._batteryLevel IS NULL OR d._batteryLevel <= :maxBattery) AND 
        (:search IS NULL OR LOWER(d.deviceName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.hardwareUID) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    fun findWithFilters(
        @Param("status") status: DeviceStatus?,
        @Param("providerId") providerId: UUID?,
        @Param("minBattery") minBattery: Int?,
        @Param("maxBattery") maxBattery: Int?,
        @Param("search") search: String?
    ): List<Device>
}