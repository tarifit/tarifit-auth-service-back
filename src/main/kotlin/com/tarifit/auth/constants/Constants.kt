package com.tarifit.auth.constants

object ErrorMessages {
    const val EMAIL_ALREADY_EXISTS = "Email already exists"
    const val USERNAME_ALREADY_EXISTS = "Username already exists"
    const val INVALID_CREDENTIALS = "Invalid email or password"
    const val TOKEN_VALIDATION_FAILED = "Token validation failed"
    const val REGISTRATION_FAILED = "Registration failed"
    const val LOGIN_FAILED = "Login failed"
    const val USER_REGISTERED_SUCCESSFULLY = "User registered successfully"
    const val LOGIN_SUCCESSFUL = "Login successful"
    const val TOKEN_VALID = "Token is valid"
    const val TOKEN_INVALID = "Token is invalid"
}

object SecurityConstants {
    const val TOKEN_PREFIX = "Bearer "
    const val HEADER_STRING = "Authorization"
}
