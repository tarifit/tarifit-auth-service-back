package com.tarifit.auth.dto

data class ErrorResponseDto(
    val error: String,
    val message: String,
    val timestamp: String
)