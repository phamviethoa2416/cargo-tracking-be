package com.example.cargotracking.modules.user.exception

sealed class UserException(message: String) : RuntimeException(message) {
    
    class UserNotFoundException(message: String = "User not found") : UserException(message)
    
    class InvalidCredentialsException(message: String = "Invalid email or password") : UserException(message)
    
    class UserAlreadyExistsException(message: String) : UserException(message)
    
    class UserAccountDisabledException(message: String = "User account is disabled") : UserException(message)
    
    class InvalidTokenException(message: String) : UserException(message)
    
    class TokenExpiredException(message: String = "Token is expired") : UserException(message)
    
    class TokenRevokedException(message: String = "Token has been revoked") : UserException(message)
}
