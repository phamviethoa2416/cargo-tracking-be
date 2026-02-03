package com.example.cargotracking.modules.order.repository

import com.example.cargotracking.modules.order.model.entity.Order
import com.example.cargotracking.modules.order.model.types.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrderRepository : JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
    fun findByCustomerId(customerId: UUID): List<Order>
    fun findByProviderId(providerId: UUID): List<Order>
    fun findByProviderIdAndStatus(providerId: UUID, status: OrderStatus): List<Order>
    fun findByShipmentId(shipmentId: UUID): Optional<Order>
}
