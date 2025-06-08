package com.tarifit.auth.enums

enum class ErrorMessages(val message: String) {
    EMAIL_ALREADY_EXISTS("Email already exists"),
    USERNAME_ALREADY_EXISTS("Username already exists"),
    INVALID_CREDENTIALS("Invalid email or password"),
    USER_REGISTERED_SUCCESSFULLY("User registered successfully"),
    LOGIN_SUCCESSFUL("Login successful"),
    TOKEN_VALID("Token is valid"),
    TOKEN_INVALID("Token is invalid")
}