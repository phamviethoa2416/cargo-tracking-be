package com.example.cargotracking.modules.user.model.dto.request

import com.example.cargotracking.modules.user.validation.PasswordMatch
import com.example.cargotracking.modules.user.validation.PasswordNotMatch
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@PasswordMatch(
    passwordField = "newPassword",
    confirmPasswordField = "confirmNewPassword",
    message = "Confirm password must match new password"
)
@PasswordNotMatch(
    oldPasswordField = "oldPassword",
    newPasswordField = "newPassword",
    message = "New password must not match old password"
)
data class ChangePasswordRequest(
    @field:NotBlank(message = "Old password must not be blank")
    val oldPassword: String,

    @field:NotBlank(message = "New password must not be blank")
    @field:Size(message = "New password must be at least 8 characters long", min = 8)
    val newPassword: String,

    @field:NotBlank(message = "Confirm new password must not be blank")
    val confirmNewPassword: String,
)