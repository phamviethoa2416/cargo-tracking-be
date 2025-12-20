package com.example.cargotracking.modules.user.service

import com.example.cargotracking.modules.user.model.dto.request.ChangePasswordRequest
import com.example.cargotracking.modules.user.model.dto.request.UpdateProfileRequest
import com.example.cargotracking.modules.user.model.dto.response.UserResponse
import com.example.cargotracking.modules.user.model.entity.User
import com.example.cargotracking.modules.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun getUserById(id: UUID): User =
        userRepository.findById(id).orElseThrow { NoSuchElementException("User not found") }

    fun getAllUsers(): List<User> = userRepository.findAll()

    @Transactional
    fun updateProfile(userId: UUID, request: UpdateProfileRequest): UserResponse {
        val existing = getUserById(userId)

        val updated = User(
            id = existing.id,
            username = existing.username,
            email = existing.email,
            passwordHash = existing.passwordHash,
            fullName = request.fullName ?: existing.fullName,
            phoneNumber = request.phoneNumber ?: existing.phoneNumber,
            role = existing.role,
            address = request.address ?: existing.address,
            isActive = existing.isActive
        )

        return UserResponse.from(userRepository.save(updated))
    }

    @Transactional
    fun changePassword(userId: UUID, request: ChangePasswordRequest) {
        val user = getUserById(userId)

        if (!passwordEncoder.matches(request.oldPassword, user.passwordHash)) {
            throw IllegalArgumentException("Old password is incorrect")
        }

        val encodedNewPassword = passwordEncoder.encode(request.newPassword)
            ?: throw IllegalStateException("Password encoding failed")

        userRepository.updatePasswordHash(userId, encodedNewPassword)
    }
}