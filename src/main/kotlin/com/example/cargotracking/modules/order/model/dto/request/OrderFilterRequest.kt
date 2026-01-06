package com.example.cargotracking.modules.order.model.dto.request

import com.example.cargotracking.modules.order.model.types.OrderStatus
import java.time.Instant
import java.util.UUID

data class OrderFilterRequest(
    val status: OrderStatus? = null,
    val customerId: UUID? = null,
    val providerId: UUID? = null,
    val createdAfter: Instant? = null,
    val createdBefore: Instant? = null,
    val search: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
    val sortBy: String? = "createdAt",
    val sortOrder: String? = "desc"
)
