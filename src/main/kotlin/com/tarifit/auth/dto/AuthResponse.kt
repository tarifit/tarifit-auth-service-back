package com.tarifit.auth.dto

data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String,
    val username: String,
    val message: String = "Authentication successful"
)
