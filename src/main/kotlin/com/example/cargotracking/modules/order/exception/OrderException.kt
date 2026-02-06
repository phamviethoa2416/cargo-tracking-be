package com.example.cargotracking.modules.order.exception

sealed class OrderException(message: String) : RuntimeException(message) {
    
    class OrderNotFoundException(message: String = "Order not found") : OrderException(message)
    
    class OrderAccessDeniedException(message: String = "Access denied to this order") : OrderException(message)
    
    class OrderInvalidStateException(message: String) : OrderException(message)
    
    class OrderAlreadyProcessedException(message: String = "Order has already been processed") : OrderException(message)
    
    class UserNotFoundException(message: String = "User not found") : OrderException(message)
    
    class InvalidUserRoleException(message: String) : OrderException(message)
    
    class UserAccountInactiveException(message: String = "User account is not active") : OrderException(message)
}
