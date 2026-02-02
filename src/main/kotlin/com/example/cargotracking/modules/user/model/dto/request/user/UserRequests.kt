package com.example.cargotracking.modules.user.model.dto.request.user

import com.example.cargotracking.modules.user.validation.PasswordMatch
import com.example.cargotracking.modules.user.validation.PasswordNotMatch
import com.example.cargotracking.modules.user.validation.StrongPassword
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
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
    @field:StrongPassword
    val newPassword: String,

    @field:NotBlank(message = "Password confirmation is required")
    val confirmPassword: String
)

data class UpdateProfileRequest(
    @field:Size(min = 2, max = 255, message = "Full name must be 2-255 characters")
    val fullName: String? = null,

    @field:Pattern(
        regexp = "^[0-9+()\\s-]{8,20}$",
        message = "Invalid phone number format"
    )
    val phoneNumber: String? = null,

    @field:Size(max = 500, message = "Address must not exceed 500 characters")
    val address: String? = null
)