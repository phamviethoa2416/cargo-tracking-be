package com.example.cargotracking.modules.user.controller

import com.example.cargotracking.modules.user.model.dto.request.user.*
import com.example.cargotracking.common.response.SuccessResponse
import com.example.cargotracking.modules.user.model.dto.response.UserResponse
import com.example.cargotracking.modules.user.model.types.UserRole
import com.example.cargotracking.modules.user.principal.UserPrincipal
import com.example.cargotracking.modules.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<UserResponse> {
        val user = userService.getUserById(principal.userId)
        return ResponseEntity.ok(UserResponse.from(user))
    }

    @PutMapping("/me/profile")
    fun updateProfile(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserResponse> {
        val updatedUser = userService.updateProfile(principal.userId, request)
        return ResponseEntity.ok(updatedUser)
    }

    @PutMapping("/me/password")
    fun changePassword(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<SuccessResponse> {
        val response = userService.changePassword(principal.userId, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.userId == #id")
    fun getUserById(
        @PathVariable id: UUID
    ): ResponseEntity<UserResponse> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(UserResponse.from(user))
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users)
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    fun getUsersByRole(
        @PathVariable role: UserRole,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<UserResponse>> {
        if (principal.role == UserRole.PROVIDER && role != UserRole.SHIPPER) {
            return ResponseEntity.ok(emptyList())
        }
        
        val users = userService.getUsersByRole(role)
        return ResponseEntity.ok(users)
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    fun activateUser(
        @PathVariable id: UUID
    ): ResponseEntity<SuccessResponse> {
        val response = userService.activateUser(id)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    fun deactivateUser(
        @PathVariable id: UUID
    ): ResponseEntity<SuccessResponse> {
        val response = userService.deactivateUser(id)
        return ResponseEntity.ok(response)
    }
}

