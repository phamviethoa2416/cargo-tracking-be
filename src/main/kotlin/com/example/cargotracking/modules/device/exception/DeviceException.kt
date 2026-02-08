package com.example.cargotracking.modules.device.exception

sealed class DeviceException(message: String) : RuntimeException(message) {

    class DeviceNotFoundException(message: String = "Device not found") : DeviceException(message)

    class DeviceAlreadyExistsException(message: String) : DeviceException(message)

    class DeviceAccessDeniedException(message: String = "Access denied to this device") : DeviceException(message)

    class DeviceInvalidStateException(message: String) : DeviceException(message)

    class UserNotFoundException(message: String = "User not found") : DeviceException(message)

    class InvalidUserRoleException(message: String) : DeviceException(message)

    class UserAccountInactiveException(message: String = "User account is not active") : DeviceException(message)
}
