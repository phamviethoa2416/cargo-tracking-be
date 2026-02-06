package com.example.cargotracking.modules.user.service

import com.example.cargotracking.modules.user.exception.UserException
import com.example.cargotracking.modules.user.model.dto.request.user.*
import com.example.cargotracking.common.response.SuccessResponse
import com.example.cargotracking.modules.user.model.dto.response.UserListResponse
import com.example.cargotracking.modules.user.model.dto.response.UserResponse
import com.example.cargotracking.modules.user.model.entity.User
import com.example.cargotracking.modules.user.model.types.UserRole
import com.example.cargotracking.modules.user.repository.RefreshTokenRepository
import com.example.cargotracking.modules.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @Transactional(readOnly = true)
    fun getUserById(id: UUID): User =
        userRepository.findById(id).orElseThrow { UserException.UserNotFoundException("User not found with $id") }

    @Transactional(readOnly = true)
    fun getAllUsers(
        page: Int = 1,
        pageSize: Int = 20
    ): UserListResponse {
        val pageable = PageRequest.of(
            page - 1,
            pageSize,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        val usersPage = userRepository.findAll(pageable)

        return UserListResponse(
            users = usersPage.content.map(UserResponse::from),
            total = usersPage.totalElements,
            page = page,
            pageSize = pageSize,
            totalPages = usersPage.totalPages
        )
    }

    @Transactional(readOnly = true)
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
            throw UserException.InvalidCredentialsException("Current password is incorrect")
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