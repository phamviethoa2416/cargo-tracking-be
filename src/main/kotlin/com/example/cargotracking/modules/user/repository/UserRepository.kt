package com.example.cargotracking.modules.user.repository

import com.example.cargotracking.modules.user.model.entity.User
import com.example.cargotracking.modules.user.model.types.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): Optional<User>
    fun findByUsername(username: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
    fun findByRole(role: UserRole): List<User>
}