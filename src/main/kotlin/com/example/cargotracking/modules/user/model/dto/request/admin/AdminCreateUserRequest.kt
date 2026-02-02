package com.example.cargotracking.modules.user.model.dto.request.admin

import com.example.cargotracking.modules.user.model.types.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class AdminCreateUserRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 100, message = "Username must be 3-100 characters")
    val username: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Full name is required")
    @field:Size(min = 2, max = 255, message = "Full name must be 2-255 characters")
    val fullName: String,

    @field:NotNull(message = "Role is required")
    val role: UserRole,

    @field:Pattern(
        regexp = "^[0-9+()\\s-]{8,20}$",
        message = "Invalid phone number format"
    )
    val phoneNumber: String? = null,

    @field:Size(max = 500, message = "Address must not exceed 500 characters")
    val address: String? = null
)