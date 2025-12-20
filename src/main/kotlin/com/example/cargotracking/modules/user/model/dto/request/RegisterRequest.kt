package com.example.cargotracking.modules.user.model.dto.request

import com.example.cargotracking.modules.user.model.types.UserRole
import com.example.cargotracking.modules.user.validation.PasswordMatch
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@PasswordMatch(
    passwordField = "password",
    confirmPasswordField = "confirmPassword",
    message = "Confirm password must match password"
)
data class RegisterRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 100)
    val username: String,

    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Size(min = 8)
    val password: String,

    @field:NotBlank
    val confirmPassword: String,

    @field:NotBlank
    @field:Size(min = 2, max = 255)
    val fullName: String,

    @field:Pattern(
        regexp = "^[0-9+()\\s-]{8,20}$",
        message = "Invalid phone number"
    )
    val phoneNumber: String? = null,

    @field:NotNull(message = "Role must not be null")
    val role: UserRole,

    @field:Size(max = 500)
    val address: String? = null
)