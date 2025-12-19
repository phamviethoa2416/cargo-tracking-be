package com.example.cargotracking.modules.user.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordMatchValidator::class])
@MustBeDocumented
annotation class PasswordMatch(
    val message: String = "Password and confirm password must match",
    val passwordField: String = "password",
    val confirmPasswordField: String = "confirmPassword",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)