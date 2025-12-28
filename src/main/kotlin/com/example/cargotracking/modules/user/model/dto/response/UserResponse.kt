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
    val createdAt: Instant?,
    val updatedAt: Instant?
) {
    companion object {
        fun from(user: User): UserResponse = UserResponse(
            id = user.id,
            username = user.username,
            email = user.email,
            fullName = user.fullName,
            phoneNumber = user.phoneNumber,
            role = user.role,
            address = user.address,
            isActive = user.isActive,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
}
