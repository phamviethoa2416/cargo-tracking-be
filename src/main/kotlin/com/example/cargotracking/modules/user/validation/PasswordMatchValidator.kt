package com.example.cargotracking.modules.user.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.full.memberProperties

class PasswordMatchValidator : ConstraintValidator<PasswordMatch, Any> {
    private lateinit var passwordFieldName: String
    private lateinit var confirmPasswordFieldName: String
    private lateinit var message: String

    override fun initialize(constraintAnnotation: PasswordMatch) {
        passwordFieldName = constraintAnnotation.passwordField
        confirmPasswordFieldName = constraintAnnotation.confirmPasswordField
        message = constraintAnnotation.message
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true

        val password = getFieldValue(value, passwordFieldName) as? String
        val confirmPassword = getFieldValue(value, confirmPasswordFieldName) as? String

        val isValid = password != null && confirmPassword != null && password == confirmPassword

        if (!isValid) {
            context?.disableDefaultConstraintViolation()
            context?.buildConstraintViolationWithTemplate(message)
                ?.addPropertyNode(confirmPasswordFieldName)
                ?.addConstraintViolation()
        }

        return isValid
    }

    private fun getFieldValue(obj: Any, fieldName: String): Any? {
        return obj::class.memberProperties
            .find { it.name == fieldName }
            ?.call(obj)
    }
}

