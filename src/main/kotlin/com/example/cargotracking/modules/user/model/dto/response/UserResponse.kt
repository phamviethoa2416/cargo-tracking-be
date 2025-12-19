package com.example.cargotracking.modules.user.model.dto.response

import com.example.cargotracking.modules.user.model.entity.User
import com.example.cargotracking.modules.user.model.types.UserRole
import java.time.Instant
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val username: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String?,
    val role: UserRole,
    val address: String?,
    val isActive: Boolean,
    val createdAt: Instant
)

fun User.toResponse(): UserResponse = UserResponse(
    id = this.id,
    username = this.username,
    email = this.email,
    fullName = this.fullName,
    phoneNumber = this.phoneNumber,
    role = this.role,
    address = this.address,
    isActive = this.isActive,
    createdAt = this.createdAt ?: Instant.now()
)
