package com.tarifit.auth.dto

data class AuthResponseDto(
    val token: String,
    val userId: String,
    val email: String,
    val username: String,
    val message: String,
    val expiresAt: Long
)