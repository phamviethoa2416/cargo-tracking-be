package com.example.cargotracking.modules.user.model.dto.request

import com.example.cargotracking.modules.user.validation.PasswordMatch
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@PasswordMatch(
    passwordField = "newPassword",
    confirmPasswordField = "confirmPassword",
    message = "Passwords do not match"
)
data class ResetPasswordRequest(
    @field:NotBlank(message = "Reset token is required")
    val token: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val newPassword: String,

    @field:NotBlank(message = "Please confirm your new password")
    val confirmPassword: String
)
