package com.example.cargotracking.modules.user.validation

import com.example.cargotracking.common.utils.getPropValue
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class PasswordMatchValidator : ConstraintValidator<PasswordMatch, Any> {
    private lateinit var config: PasswordMatch

    override fun initialize(annotation: PasswordMatch) {
        this.config = annotation
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true

        val password = (value.getPropValue(config.passwordField) as? String)?.trim()
        val confirm = (value.getPropValue(config.confirmPasswordField) as? String)?.trim()

        if (password.isNullOrBlank() || confirm.isNullOrBlank()) return true

        val isValid = password == confirm

        return isValid.also { valid ->
            if (!valid) {
                context.apply {
                    disableDefaultConstraintViolation()
                    buildConstraintViolationWithTemplate(config.message)
                        .addPropertyNode(config.confirmPasswordField)
                        .addConstraintViolation()
                }
            }
        }
    }
}