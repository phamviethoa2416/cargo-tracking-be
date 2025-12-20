package com.example.cargotracking.modules.user.controller

import com.example.cargotracking.modules.user.model.dto.request.ForgotPasswordRequest
import com.example.cargotracking.modules.user.model.dto.request.LoginRequest
import com.example.cargotracking.modules.user.model.dto.request.RefreshTokenRequest
import com.example.cargotracking.modules.user.model.dto.request.RegisterRequest
import com.example.cargotracking.modules.user.model.dto.request.ResetPasswordRequest
import com.example.cargotracking.modules.user.model.dto.response.AuthResponse
import com.example.cargotracking.modules.user.model.dto.response.SuccessResponse
import com.example.cargotracking.modules.user.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        val response = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/forgotPassword")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<SuccessResponse> {
        authService.forgotPassword(request)
        return ResponseEntity.ok(
            SuccessResponse.of("If the email exists, a password reset link has been sent.")
        )
    }

    @PostMapping("/resetPassword")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<SuccessResponse> {
        authService.resetPassword(request)
        return ResponseEntity.ok(
            SuccessResponse.of("Password has been reset successfully")
        )
    }
}

