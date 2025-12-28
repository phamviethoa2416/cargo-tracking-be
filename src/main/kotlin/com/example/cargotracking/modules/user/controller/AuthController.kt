package com.example.cargotracking.modules.user.controller

import com.example.cargotracking.modules.user.model.dto.request.*
import com.example.cargotracking.modules.user.model.dto.response.AuthResponse
import com.example.cargotracking.modules.user.model.dto.response.SuccessResponse
import com.example.cargotracking.modules.user.model.dto.response.TokenResponse
import com.example.cargotracking.modules.user.model.dto.response.UserResponse
import com.example.cargotracking.modules.user.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun registerCustomer(
        @Valid @RequestBody request: CustomerRegisterRequest
    ): ResponseEntity<AuthResponse> {
        val response = authService.registerCustomer(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    fun createUserByAdmin(
        @Valid @RequestBody request: AdminCreateUserRequest
    ): ResponseEntity<UserResponse> {
        val response = authService.createUserByAdmin(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<AuthResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<TokenResponse> {
        val response = authService.refreshToken(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/forgotPassword")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<SuccessResponse> {
        authService.forgotPassword(request)
        return ResponseEntity.ok(
            SuccessResponse.of("If the email exists, a password reset link has been sent.")
        )
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody request: ResetPasswordRequest
    ): ResponseEntity<SuccessResponse> {
        val response = authService.resetPassword(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<SuccessResponse> {
        authService.logout(request.refreshToken)
        return ResponseEntity.ok(
            SuccessResponse.of("Logged out successfully")
        )
    }
}

