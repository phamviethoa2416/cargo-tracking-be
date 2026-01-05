package com.example.cargotracking.modules.device.repository

import com.example.cargotracking.modules.device.model.entity.Device
import com.example.cargotracking.modules.device.model.types.DeviceStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    @Query("SELECT d FROM Device d WHERE d.currentShipmentId = :shipmentId AND (:providerId IS NULL OR d.providerId = :providerId)")
    fun findByCurrentShipmentId(
        @Param("shipmentId") shipmentId: UUID,
        @Param("providerId") providerId: UUID?
    ): List<Device>

    @Query("SELECT d FROM Device d WHERE (d.lastSeenAt IS NULL OR d.lastSeenAt < :threshold) AND (:providerId IS NULL OR d.providerId = :providerId)")
    fun findOfflineDevices(
        @Param("threshold") threshold: Instant,
        @Param("providerId") providerId: UUID?
    ): List<Device>
    
    @Query("SELECT d FROM Device d WHERE d.lastSeenAt IS NOT NULL AND d.lastSeenAt >= :threshold AND (:providerId IS NULL OR d.providerId = :providerId)")
    fun findOnlineDevices(
        @Param("threshold") threshold: Instant,
        @Param("providerId") providerId: UUID?
    ): List<Device>
    
    @Query("SELECT d FROM Device d WHERE d.status = :status AND (:providerId IS NULL OR d.providerId = :providerId)")
    fun findByStatusAndProviderId(
        @Param("status") status: DeviceStatus,
        @Param("providerId") providerId: UUID?
    ): List<Device>

    @Query("""
        SELECT d FROM Device d WHERE 
        (:status IS NULL OR d.status = :status) AND 
        (:providerId IS NULL OR d.providerId = :providerId) AND 
        (:minBattery IS NULL OR d.batteryLevel IS NULL OR d.batteryLevel >= :minBattery) AND 
        (:maxBattery IS NULL OR d.batteryLevel IS NULL OR d.batteryLevel <= :maxBattery) AND 
        (
            :search IS NULL OR 
            LOWER(d.deviceName) LIKE LOWER(CONCAT('%', :search, '%')) OR 
            LOWER(d.hardwareUID) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    fun findWithFilters(
        @Param("status") status: DeviceStatus?,
        @Param("providerId") providerId: UUID?,
        @Param("minBattery") minBattery: Int?,
        @Param("maxBattery") maxBattery: Int?,
        @Param("search") search: String?,
        pageable: Pageable
    ): Page<Device>
}