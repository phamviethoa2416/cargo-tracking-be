package com.example.cargotracking.modules.user.model.dto.request

import com.example.cargotracking.modules.user.validation.PasswordMatch
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@PasswordMatch(
    passwordField = "password",
    confirmPasswordField = "confirmPassword",
    message = "Passwords do not match"
)
data class CustomerRegisterRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 100, message = "Username must be 3-100 characters")
    val username: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,

    @field:NotBlank(message = "Password confirmation is required")
    val confirmPassword: String,

    @field:NotBlank(message = "Full name is required")
    @field:Size(min = 2, max = 255, message = "Full name must be 2-255 characters")
    val fullName: String,

    @field:Pattern(
        regexp = "^[0-9+()\\s-]{8,20}$",
        message = "Invalid phone number format"
    )
    val phoneNumber: String? = null,

    @field:Size(max = 500, message = "Address must not exceed 500 characters")
    val address: String? = null
)