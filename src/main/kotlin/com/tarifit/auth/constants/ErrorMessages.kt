package com.tarifit.auth.constants

enum class ErrorMessages(val message: String) {
    EMAIL_ALREADY_EXISTS("Email already exists"),
    USERNAME_ALREADY_EXISTS("Username already exists"),
    INVALID_CREDENTIALS("Invalid email or password"),
    TOKEN_VALIDATION_FAILED("Token validation failed"),
    REGISTRATION_FAILED("Registration failed"),
    LOGIN_FAILED("Login failed"),
    USER_REGISTERED_SUCCESSFULLY("User registered successfully"),
    LOGIN_SUCCESSFUL("Login successful"),
    TOKEN_VALID("Token is valid"),
    TOKEN_INVALID("Token is invalid")
}