package com.example.cargotracking.modules.order.model.dto.response

data class OrderListResponse(
    val orders: List<OrderResponse>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)
