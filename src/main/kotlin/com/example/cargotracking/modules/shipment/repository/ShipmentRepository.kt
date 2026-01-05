package com.example.cargotracking.modules.shipment.repository

import com.example.cargotracking.modules.shipment.model.entity.Shipment
import com.example.cargotracking.modules.shipment.model.types.ShipmentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface ShipmentRepository : JpaRepository<Shipment, UUID> {
    fun findByCustomerId(customerId: UUID): List<Shipment>
    fun findByProviderId(providerId: UUID): List<Shipment>
    fun findByShipperId(shipperId: UUID): List<Shipment>
    fun findByDeviceId(deviceId: UUID): List<Shipment>

    fun findByStatus(status: ShipmentStatus): List<Shipment>
    fun findByProviderIdAndStatus(
        providerId: UUID,
        status: ShipmentStatus
    ): List<Shipment>

    fun findByShipperIdAndStatus(
        shipperId: UUID,
        status: ShipmentStatus
    ): List<Shipment>

    fun findByEstimatedDeliveryAtBetween(start: Instant, end: Instant): List<Shipment>

    @Query("""
        SELECT s FROM Shipment s 
        WHERE s.status = :status 
        AND s.estimatedDeliveryAt < :now
    """)
    fun findDelayedShipments(
        @Param("status") status: ShipmentStatus,
        @Param("now") now: Instant
    ): List<Shipment>

    @Query("""
        SELECT s FROM Shipment s WHERE 
        (:status IS NULL OR s.status = :status) AND 
        (:customerId IS NULL OR s.customerId = :customerId) AND 
        (:providerId IS NULL OR s.providerId = :providerId) AND 
        (:shipperId IS NULL OR s.shipperId = :shipperId) AND 
        (:deviceId IS NULL OR s.deviceId = :deviceId) AND 
        (:createdAfter IS NULL OR (s.createdAt IS NOT NULL AND s.createdAt >= :createdAfter)) AND 
        (:createdBefore IS NULL OR (s.createdAt IS NOT NULL AND s.createdAt <= :createdBefore)) AND 
        (:deliveryAfter IS NULL OR (s.estimatedDeliveryAt IS NOT NULL AND s.estimatedDeliveryAt >= :deliveryAfter)) AND 
        (:deliveryBefore IS NULL OR (s.estimatedDeliveryAt IS NOT NULL AND s.estimatedDeliveryAt <= :deliveryBefore)) AND 
        (
            :search IS NULL OR 
            LOWER(s.goodsDescription) LIKE LOWER(CONCAT('%', :search, '%')) OR 
            LOWER(s.pickupAddress) LIKE LOWER(CONCAT('%', :search, '%')) OR 
            LOWER(s.deliveryAddress) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    fun findWithFilters(
        @Param("status") status: ShipmentStatus?,
        @Param("customerId") customerId: UUID?,
        @Param("providerId") providerId: UUID?,
        @Param("shipperId") shipperId: UUID?,
        @Param("deviceId") deviceId: UUID?,
        @Param("createdAfter") createdAfter: Instant?,
        @Param("createdBefore") createdBefore: Instant?,
        @Param("deliveryAfter") deliveryAfter: Instant?,
        @Param("deliveryBefore") deliveryBefore: Instant?,
        @Param("search") search: String?,
        pageable: Pageable
    ): Page<Shipment>
}