package com.example.cargotracking.modules.user.principal

import com.example.cargotracking.modules.user.model.types.UserRole
import java.util.UUID

data class UserPrincipal(
    val userId: UUID,
    val email: String,
    val role: UserRole
)
