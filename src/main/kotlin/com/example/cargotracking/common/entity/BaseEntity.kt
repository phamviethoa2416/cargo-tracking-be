package com.example.cargotracking.common.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity(
    @Id
    @Column(updatable = false, nullable = false)
    val id: UUID,
) {
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    lateinit var createdAt: Instant
        protected set

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant
        protected set

    protected abstract fun validateInvariants()

    @PrePersist
    @PreUpdate
    protected fun onPersist() {
        validateInvariants()
    }
}