package com.example.cargotracking.common.exception

import com.example.cargotracking.modules.order.exception.OrderException
import com.example.cargotracking.modules.shipment.exception.ShipmentException
import com.example.cargotracking.modules.user.exception.UserException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException) =
        ex.bindingResult.allErrors
            .groupBy(
                { (it as? FieldError)?.field ?: it.objectName },
                { it.defaultMessage ?: "Invalid value" }
            ).let { errors ->
                logger.warn("Validation failed: {}", errors)
                buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "One or more fields have invalid values", errors)
            }

    @ExceptionHandler(IllegalArgumentException::class, MethodArgumentTypeMismatchException::class)
    fun handleBadRequest(ex: Exception) =
        ex.message.let { msg ->
            logger.warn("Bad request: $msg")
            val message = if (ex is MethodArgumentTypeMismatchException)
                "Invalid value '${ex.value}' for parameter '${ex.name}'. Expected: ${ex.requiredType?.simpleName}"
            else msg ?: "Invalid request"
            buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message)
        }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException) =
        buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.message ?: "Resource not found")
            .also { logger.warn("Resource not found: ${ex.message}") }

    @ExceptionHandler(IllegalStateException::class)
    fun handleConflict(ex: IllegalStateException) =
        buildResponse(HttpStatus.CONFLICT, "CONFLICT", ex.message ?: "Operation not allowed")
            .also { logger.warn("State conflict: ${ex.message}") }

    @ExceptionHandler(AuthenticationException::class)
    fun handleUnauthorized(ex: AuthenticationException) =
        buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.message ?: "Authentication required")
            .also { logger.warn("Authentication failed: ${ex.message}") }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleForbidden(ex: AccessDeniedException) =
        buildResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission")

    // User module custom exceptions - sealed class handler
    @ExceptionHandler(UserException::class)
    fun handleUserException(ex: UserException) = when (ex) {
        is UserException.UserNotFoundException ->
            buildResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.message ?: "User not found")
                .also { logger.warn("User not found: ${ex.message}") }
        
        is UserException.InvalidCredentialsException ->
            buildResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.message ?: "Invalid credentials")
                .also { logger.warn("Invalid credentials: ${ex.message}") }
        
        is UserException.UserAlreadyExistsException ->
            buildResponse(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", ex.message ?: "User already exists")
                .also { logger.warn("User already exists: ${ex.message}") }
        
        is UserException.UserAccountDisabledException ->
            buildResponse(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", ex.message ?: "User account is disabled")
                .also { logger.warn("Account disabled: ${ex.message}") }
        
        is UserException.InvalidTokenException ->
            buildResponse(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", ex.message ?: "Invalid token")
                .also { logger.warn("Invalid token: ${ex.message}") }
        
        is UserException.TokenExpiredException ->
            buildResponse(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", ex.message ?: "Token is expired")
                .also { logger.warn("Token expired: ${ex.message}") }
        
        is UserException.TokenRevokedException ->
            buildResponse(HttpStatus.UNAUTHORIZED, "TOKEN_REVOKED", ex.message ?: "Token has been revoked")
                .also { logger.warn("Token revoked: ${ex.message}") }
    }

    @ExceptionHandler(OrderException::class)
    fun handleOrderException(ex: OrderException) = when (ex) {
        is OrderException.OrderNotFoundException ->
            buildResponse(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", ex.message ?: "Order not found")
                .also { logger.warn("Order not found: ${ex.message}") }
        
        is OrderException.OrderAccessDeniedException ->
            buildResponse(HttpStatus.FORBIDDEN, "ORDER_ACCESS_DENIED", ex.message ?: "Access denied")
                .also { logger.warn("Order access denied: ${ex.message}") }
        
        is OrderException.OrderInvalidStateException ->
            buildResponse(HttpStatus.CONFLICT, "ORDER_INVALID_STATE", ex.message ?: "Invalid order state")
                .also { logger.warn("Order invalid state: ${ex.message}") }
        
        is OrderException.OrderAlreadyProcessedException ->
            buildResponse(HttpStatus.CONFLICT, "ORDER_ALREADY_PROCESSED", ex.message ?: "Order already processed")
                .also { logger.warn("Order already processed: ${ex.message}") }
        
        is OrderException.UserNotFoundException ->
            buildResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.message ?: "User not found")
                .also { logger.warn("User not found: ${ex.message}") }
        
        is OrderException.InvalidUserRoleException ->
            buildResponse(HttpStatus.BAD_REQUEST, "INVALID_USER_ROLE", ex.message ?: "Invalid user role")
                .also { logger.warn("Invalid user role: ${ex.message}") }
        
        is OrderException.UserAccountInactiveException ->
            buildResponse(HttpStatus.FORBIDDEN, "USER_ACCOUNT_INACTIVE", ex.message ?: "User account is inactive")
                .also { logger.warn("User account inactive: ${ex.message}") }
    }

    @ExceptionHandler(ShipmentException::class)
    fun handleShipmentException(ex: ShipmentException) = when (ex) {
        is ShipmentException.ShipmentNotFoundException ->
            buildResponse(HttpStatus.NOT_FOUND, "SHIPMENT_NOT_FOUND", ex.message ?: "Shipment not found")
                .also { logger.warn("Shipment not found: ${ex.message}") }
        
        is ShipmentException.ShipmentAccessDeniedException ->
            buildResponse(HttpStatus.FORBIDDEN, "SHIPMENT_ACCESS_DENIED", ex.message ?: "Access denied")
                .also { logger.warn("Shipment access denied: ${ex.message}") }
        
        is ShipmentException.ShipmentInvalidStateException ->
            buildResponse(HttpStatus.CONFLICT, "SHIPMENT_INVALID_STATE", ex.message ?: "Invalid shipment state")
                .also { logger.warn("Shipment invalid state: ${ex.message}") }
        
        is ShipmentException.UserNotFoundException ->
            buildResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.message ?: "User not found")
                .also { logger.warn("User not found: ${ex.message}") }
        
        is ShipmentException.InvalidUserRoleException ->
            buildResponse(HttpStatus.BAD_REQUEST, "INVALID_USER_ROLE", ex.message ?: "Invalid user role")
                .also { logger.warn("Invalid user role: ${ex.message}") }
        
        is ShipmentException.UserAccountInactiveException ->
            buildResponse(HttpStatus.FORBIDDEN, "USER_ACCOUNT_INACTIVE", ex.message ?: "User account is inactive")
                .also { logger.warn("User account inactive: ${ex.message}") }
        
        is ShipmentException.DeviceNotFoundException ->
            buildResponse(HttpStatus.NOT_FOUND, "DEVICE_NOT_FOUND", ex.message ?: "Device not found")
                .also { logger.warn("Device not found: ${ex.message}") }
        
        is ShipmentException.DeviceInvalidStateException ->
            buildResponse(HttpStatus.CONFLICT, "DEVICE_INVALID_STATE", ex.message ?: "Invalid device state")
                .also { logger.warn("Device invalid state: ${ex.message}") }
    }

    @ExceptionHandler(Exception::class)
    fun handleInternalError(ex: Exception) =
        buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred")
            .also { logger.error("Unhandled exception", ex) }

    private fun buildResponse(
        status: HttpStatus,
        error: String,
        message: String,
        details: Map<String, List<String>>? = null
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(status).body(
            ErrorResponse(
                status = status.value(),
                error = error,
                message = message,
                details = details,
                timestamp = Instant.now()
            )
        )
}

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val details: Map<String, List<String>>? = null,
    val timestamp: Instant
)