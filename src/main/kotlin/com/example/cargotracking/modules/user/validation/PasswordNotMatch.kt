package com.example.cargotracking.modules.user.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordNotMatchValidator::class])
@MustBeDocumented
annotation class PasswordNotMatch(
    val message: String = "New password must be different from old password",
    val oldPasswordField: String = "oldPassword",
    val newPasswordField: String = "newPassword",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)