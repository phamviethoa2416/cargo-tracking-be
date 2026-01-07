package com.example.cargotracking.modules.user.controller

import com.example.cargotracking.modules.user.model.dto.request.ChangePasswordRequest
import com.example.cargotracking.modules.user.model.dto.request.UpdateProfileRequest
import com.example.cargotracking.modules.user.model.dto.response.SuccessResponse
import com.example.cargotracking.modules.user.model.dto.response.UserResponse
import com.example.cargotracking.modules.user.model.types.UserRole
import com.example.cargotracking.modules.user.principal.UserPrincipal
import com.example.cargotracking.modules.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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
    fun getCurrentUser(authentication: Authentication): ResponseEntity<UserResponse> {
        val principal = authentication.principal as UserPrincipal
        val user = userService.getUserById(principal.userId)
        return ResponseEntity.ok(UserResponse.from(user))
    }

    @PutMapping("/me/profile")
    fun updateProfile(
        authentication: Authentication,
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserResponse> {
        val principal = authentication.principal as UserPrincipal
        val updatedUser = userService.updateProfile(principal.userId, request)
        return ResponseEntity.ok(updatedUser)
    }

    @PutMapping("/me/password")
    fun changePassword(
        authentication: Authentication,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<SuccessResponse> {
        val principal = authentication.principal as UserPrincipal
        val response = userService.changePassword(principal.userId, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getUserById(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<UserResponse> {
        val principal = authentication.principal as UserPrincipal

//        if (principal.userId != id && principal.role != UserRole.ADMIN) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
//        }
        
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
//    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    fun getUsersByRole(
        authentication: Authentication,
        @PathVariable role: UserRole
    ): ResponseEntity<List<UserResponse>> {
        val principal = authentication.principal as UserPrincipal

        // Allow all roles to get users by role for demonstration purposes

//        if (principal.role == UserRole.ADMIN) {
//            val users = userService.getUsersByRole(role)
//            return ResponseEntity.ok(users)
//        }
//
//        if (principal.role == UserRole.PROVIDER && role == UserRole.SHIPPER) {
//            val users = userService.getUsersByRole(UserRole.SHIPPER)
//            return ResponseEntity.ok(users)
//        }

        val users = userService.getUsersByRole(role)
        return ResponseEntity.ok(users)

//        return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    fun deactivateUser(
        @PathVariable id: UUID
    ): ResponseEntity<SuccessResponse> {
        val response = userService.deactivateUser(id)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    fun activateUser(@PathVariable id: UUID): ResponseEntity<SuccessResponse> {
        val response = userService.activateUser(id)
        return ResponseEntity.ok(response)
    }
}

