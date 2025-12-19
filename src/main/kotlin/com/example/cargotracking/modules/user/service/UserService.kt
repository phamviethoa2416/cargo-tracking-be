package com.example.cargotracking.modules.user.service

import com.example.cargotracking.modules.user.model.dto.request.UpdateProfileRequest
import com.example.cargotracking.modules.user.model.dto.response.UserResponse
import com.example.cargotracking.modules.user.model.dto.response.toResponse
import com.example.cargotracking.modules.user.model.entity.User
import com.example.cargotracking.modules.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun getUserById(id: UUID): User =
        userRepository.findById(id).orElseThrow { NoSuchElementException("User not found") }

    fun getUserByEmail(email: String): User =
        userRepository.findByEmail(email).orElseThrow { NoSuchElementException("User not found") }

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

        return userRepository.save(updated).toResponse()
    }
}