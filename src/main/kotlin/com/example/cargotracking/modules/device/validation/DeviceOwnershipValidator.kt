package com.example.cargotracking.modules.device.validation

import com.example.cargotracking.modules.device.model.entity.Device
import com.example.cargotracking.modules.user.model.types.UserRole
import com.example.cargotracking.modules.user.repository.UserRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DeviceOwnershipValidator(
    private val userRepository: UserRepository
) {
    fun validateProviderRole(providerId: UUID) {
        val user = userRepository.findById(providerId)
            .orElseThrow { NoSuchElementException("User not found with id: $providerId") }

        if (user.role != UserRole.PROVIDER) {
            throw IllegalArgumentException("Only PROVIDER role can perform this operation. Current role: ${user.role}")
        }

        if (!user.isActive) {
            throw IllegalStateException("Provider account is not active")
        }
    }

    fun validateWrite(device: Device, currentUserId: UUID) {
        validateProviderRole(currentUserId)

        if (device.providerId != currentUserId) {
            throw IllegalStateException(
                "Device does not belong to this provider. Device provider: ${device.providerId}, Current user: $currentUserId"
            )
        }
    }

    fun validateRead(device: Device, currentUserId: UUID, currentUserRole: UserRole) {
        if (currentUserRole == UserRole.ADMIN) {
            return
        }

        if (currentUserRole == UserRole.PROVIDER) {
            if (device.providerId != currentUserId) {
                throw IllegalStateException(
                    "Device does not belong to this provider. Device provider: ${device.providerId}, Current user: $currentUserId"
                )
            }
        } else {
            throw IllegalStateException("Only ADMIN or PROVIDER can read device information")
        }
    }
}