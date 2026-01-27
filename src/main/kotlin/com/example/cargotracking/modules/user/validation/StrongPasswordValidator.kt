package com.example.cargotracking.modules.user.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class StrongPasswordValidator : ConstraintValidator<StrongPassword, String> {
    private lateinit var config: StrongPassword

    override fun initialize(annotation: StrongPassword) {
        this.config = annotation
    }

    override fun isValid(password: String?, context: ConstraintValidatorContext): Boolean {
        if (password.isNullOrBlank()) return true

        val normalized = password.trim()

        val errors = buildList {
            with(config) {
                if (normalized.length < minLength) add("At least $minLength characters")
                if (requireUppercase && normalized.none { it.isUpperCase() }) add("Password must contain at least one uppercase letter")
                if (requireLowercase && normalized.none { it.isLowerCase() }) add("Password must contain at least one lowercase letter")
                if (requireDigit && normalized.none { it.isDigit() }) add("Password must contain at least one digit")
                if (requireSpecialChar && normalized.all { it.isLetterOrDigit() }) add("Password must contain at least one special character")
            }
        }

        if (errors.isNotEmpty()) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(errors.joinToString(". "))
                .addConstraintViolation()
            return false
        }

        return true
    }
}

