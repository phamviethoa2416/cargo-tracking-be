package com.example.cargotracking.modules.user.model.dto.request

import com.example.cargotracking.modules.user.validation.PasswordMatch
import com.example.cargotracking.modules.user.validation.PasswordNotMatch
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@PasswordMatch(
    passwordField = "newPassword",
    confirmPasswordField = "confirmPassword",
    message = "Confirm password must match new password"
)
@PasswordNotMatch(
    oldPasswordField = "currentPassword",
    newPasswordField = "newPassword",
    message = "New password must not match old password"
)
data class ChangePasswordRequest(
    @field:NotBlank(message = "Current password is required")
    val currentPassword: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, message = "New password must be at least 8 characters")
    val newPassword: String,

    @field:NotBlank(message = "Password confirmation is required")
    val confirmPassword: String
)