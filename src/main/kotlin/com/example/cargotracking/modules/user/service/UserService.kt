package com.example.cargotracking.modules.user.service

import com.example.cargotracking.modules.user.model.dto.request.ChangePasswordRequest
import com.example.cargotracking.modules.user.model.dto.request.UpdateProfileRequest
import com.example.cargotracking.modules.user.model.dto.response.SuccessResponse
import com.example.cargotracking.modules.user.model.dto.response.UserResponse
import com.example.cargotracking.modules.user.model.entity.User
import com.example.cargotracking.modules.user.model.types.UserRole
import com.example.cargotracking.modules.user.repository.RefreshTokenRepository
import com.example.cargotracking.modules.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    fun getUserById(id: UUID): User =
        userRepository.findById(id).orElseThrow { NoSuchElementException("User not found with $id") }

    fun getAllUsers(): List<UserResponse> = userRepository.findAll().map { UserResponse.from(it) }

    fun getUsersByRole(role: UserRole): List<UserResponse> {
        return userRepository.findByRole(role)
            .map { UserResponse.from(it) }
    }

    @Transactional
    fun updateProfile(userId: UUID, request: UpdateProfileRequest): UserResponse {
        val user = getUserById(userId)

        user.updateProfile(
            fullName = request.fullName,
            phoneNumber = request.phoneNumber,
            address = request.address
        )

        val updated = userRepository.save(user)

        return UserResponse.from(updated)
    }

    @Transactional
    fun changePassword(userId: UUID, request: ChangePasswordRequest): SuccessResponse {
        val user = getUserById(userId)

        if (!passwordEncoder.matches(request.currentPassword, user.passwordHash)) {
            throw IllegalArgumentException("Old password is incorrect")
        }

        val encodedPassword = passwordEncoder.encode(request.newPassword)
            ?: throw IllegalStateException("Password encoding failed")
        user.updatePassword(encodedPassword)
        userRepository.save(user)
        revokeAllUserTokens(userId)

        return SuccessResponse.of(
            message = "Password changed successfully. Please login again with your new password."
        )
    }

    @Transactional
    fun deactivateUser(userId: UUID): SuccessResponse {
        val user = getUserById(userId)

        user.deactivate()
        userRepository.save(user)
        revokeAllUserTokens(userId)

        return SuccessResponse(
            message = "User account has been deactivated"
        )
    }

    @Transactional
    fun activateUser(userId: UUID): SuccessResponse {
        val user = getUserById(userId)

        user.activate()
        userRepository.save(user)

        return SuccessResponse(
            message = "User account has been activated"
        )
    }

    private fun revokeAllUserTokens(userId: UUID) {
        val activeTokens = refreshTokenRepository.findByUserIdAndRevoked(userId, false)
        activeTokens.forEach { it.revoke() }
        refreshTokenRepository.saveAll(activeTokens)
    }
}