package com.example.cargotracking.modules.user.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class PasswordNotMatchValidator : ConstraintValidator<PasswordNotMatch, Any> {
    private lateinit var oldPasswordFieldName: String
    private lateinit var newPasswordFieldName: String
    private lateinit var message: String

    override fun initialize(constraintAnnotation: PasswordNotMatch) {
        oldPasswordFieldName = constraintAnnotation.oldPasswordField
        newPasswordFieldName = constraintAnnotation.newPasswordField
        message = constraintAnnotation.message
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true

        return try {
            val oldPassword = getFieldValue(value, oldPasswordFieldName) as? String
            val newPassword = getFieldValue(value, newPasswordFieldName) as? String

            if (oldPassword == null || newPassword == null) return true

            val isValid = oldPassword != newPassword

            if (!isValid && context != null) {
                context.disableDefaultConstraintViolation()
                context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(newPasswordFieldName)
                    .addConstraintViolation()
            }

            isValid
        } catch (e: Exception) {
            false
        }
    }

    private fun getFieldValue(obj: Any, fieldName: String): Any? {
        val property = obj::class.memberProperties.find { it.name == fieldName }
        property?.let { it.isAccessible = true }
        return property?.call(obj)
    }
}

