package com.example.cargotracking.modules.device.validation

import com.example.cargotracking.modules.user.model.types.UserRole
import com.example.cargotracking.modules.user.repository.UserRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DeviceOwnershipValidator(
    private val userRepository: UserRepository
) {
    fun validateShipperOwnership(shipperId: UUID) {
        val user = userRepository.findById(shipperId)
            .orElseThrow { NoSuchElementException("User not found") }

        if (user.role != UserRole.SHIPPER) {
            throw IllegalArgumentException("Owner must be a shipper")
        }

        if (!user.isActive) {
            throw IllegalArgumentException("Shipper account is not active")
        }
    }
}