package com.example.cargotracking.modules.order.repository

import com.example.cargotracking.modules.order.model.entity.Order
import com.example.cargotracking.modules.order.model.types.OrderStatus
import org.springframework.data.jpa.domain.Specification
import java.time.Instant
import java.util.*

object OrderSpecification {
    
    fun withStatus(status: OrderStatus?): Specification<Order> = Specification { root, _, cb ->
        status?.let { cb.equal(root.get<OrderStatus>("status"), it) } ?: cb.conjunction()
    }
    
    fun withCustomerId(customerId: UUID?): Specification<Order> = Specification { root, _, cb ->
        customerId?.let { cb.equal(root.get<UUID>("customerId"), it) } ?: cb.conjunction()
    }
    
    fun withProviderId(providerId: UUID?): Specification<Order> = Specification { root, _, cb ->
        providerId?.let { cb.equal(root.get<UUID>("providerId"), it) } ?: cb.conjunction()
    }
    
    fun createdAfter(createdAfter: Instant?): Specification<Order> = Specification { root, _, cb ->
        createdAfter?.let {
            val createdAtPath = root.get<Instant>("createdAt")
            cb.and(
                cb.isNotNull(createdAtPath),
                cb.greaterThanOrEqualTo(createdAtPath, it)
            )
        } ?: cb.conjunction()
    }
    
    fun createdBefore(createdBefore: Instant?): Specification<Order> = Specification { root, _, cb ->
        createdBefore?.let {
            val createdAtPath = root.get<Instant>("createdAt")
            cb.and(
                cb.isNotNull(createdAtPath),
                cb.lessThanOrEqualTo(createdAtPath, it)
            )
        } ?: cb.conjunction()
    }
    
    fun withSearchText(search: String?): Specification<Order> = Specification { root, _, cb ->
        if (search.isNullOrBlank()) {
            cb.conjunction()
        } else {
            val searchPattern = "%${search.lowercase()}%"
            val goodsDescPath = root.get<String>("goodsDescription")
            val pickupAddrPath = root.get<String>("pickupAddress")
            val deliveryAddrPath = root.get<String>("deliveryAddress")
            
            cb.or(
                cb.like(cb.lower(goodsDescPath), searchPattern),
                cb.like(cb.lower(pickupAddrPath), searchPattern),
                cb.like(cb.lower(deliveryAddrPath), searchPattern)
            )
        }
    }
    
    fun buildSpecification(
        status: OrderStatus? = null,
        customerId: UUID? = null,
        providerId: UUID? = null,
        createdAfter: Instant? = null,
        createdBefore: Instant? = null,
        search: String? = null
    ): Specification<Order> {
        return withStatus(status)
            .and(withCustomerId(customerId))
            .and(withProviderId(providerId))
            .and(createdAfter(createdAfter))
            .and(createdBefore(createdBefore))
            .and(withSearchText(search))
    }
}

