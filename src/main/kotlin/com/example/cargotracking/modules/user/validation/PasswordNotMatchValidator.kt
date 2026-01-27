package com.example.cargotracking.modules.user.validation

import com.example.cargotracking.common.utils.getPropValue
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class PasswordNotMatchValidator : ConstraintValidator<PasswordNotMatch, Any> {
    private lateinit var config: PasswordNotMatch

    override fun initialize(annotation: PasswordNotMatch) {
        this.config = annotation
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true

        val oldPassword = (value.getPropValue(config.oldPasswordField) as? String)?.trim()
        val newPassword = (value.getPropValue(config.newPasswordField) as? String)?.trim()

        if (oldPassword.isNullOrBlank() || newPassword.isNullOrBlank()) return true

        val isValid = oldPassword != newPassword

        return isValid.also { valid ->
            if (!valid) {
                context.apply {
                    disableDefaultConstraintViolation()
                    buildConstraintViolationWithTemplate(config.message)
                        .addPropertyNode(config.newPasswordField)
                        .addConstraintViolation()
                }
            }
        }
    }
}