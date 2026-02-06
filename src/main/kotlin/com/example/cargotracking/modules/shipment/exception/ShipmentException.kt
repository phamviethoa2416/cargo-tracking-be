package com.example.cargotracking.modules.shipment.exception

sealed class ShipmentException(message: String) : RuntimeException(message) {
    
    class ShipmentNotFoundException(message: String = "Shipment not found") : ShipmentException(message)
    
    class ShipmentAccessDeniedException(message: String = "Access denied to this shipment") : ShipmentException(message)
    
    class ShipmentInvalidStateException(message: String) : ShipmentException(message)
    
    class UserNotFoundException(message: String = "User not found") : ShipmentException(message)
    
    class InvalidUserRoleException(message: String) : ShipmentException(message)
    
    class UserAccountInactiveException(message: String = "User account is not active") : ShipmentException(message)
    
    class DeviceNotFoundException(message: String = "Device not found") : ShipmentException(message)
    
    class DeviceInvalidStateException(message: String) : ShipmentException(message)
}
