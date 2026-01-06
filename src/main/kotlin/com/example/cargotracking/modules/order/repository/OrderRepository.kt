package com.example.cargotracking.modules.order.repository

import com.example.cargotracking.modules.order.model.entity.Order
import com.example.cargotracking.modules.order.model.types.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    fun findByCustomerId(customerId: UUID): List<Order>
    fun findByProviderId(providerId: UUID): List<Order>
    fun findByStatus(status: OrderStatus): List<Order>
    
    fun findByCustomerIdAndStatus(customerId: UUID, status: OrderStatus): List<Order>
    fun findByProviderIdAndStatus(providerId: UUID, status: OrderStatus): List<Order>

    @Query("""
        SELECT o FROM Order o WHERE 
        (:status IS NULL OR o.status = :status) AND 
        (:customerId IS NULL OR o.customerId = :customerId) AND 
        (:providerId IS NULL OR o.providerId = :providerId) AND 
        (:createdAfter IS NULL OR (o.createdAt IS NOT NULL AND o.createdAt >= :createdAfter)) AND 
        (:createdBefore IS NULL OR (o.createdAt IS NOT NULL AND o.createdAt <= :createdBefore)) AND 
        (
            :search IS NULL OR 
            LOWER(o.goodsDescription) LIKE LOWER(CONCAT('%', :search, '%')) OR 
            LOWER(o.pickupAddress) LIKE LOWER(CONCAT('%', :search, '%')) OR 
            LOWER(o.deliveryAddress) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    fun findWithFilters(
        @Param("status") status: OrderStatus?,
        @Param("customerId") customerId: UUID?,
        @Param("providerId") providerId: UUID?,
        @Param("createdAfter") createdAfter: Instant?,
        @Param("createdBefore") createdBefore: Instant?,
        @Param("search") search: String?,
        pageable: Pageable
    ): Page<Order>
}
