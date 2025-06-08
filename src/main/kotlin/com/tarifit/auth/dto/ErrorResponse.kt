package com.tarifit.auth.dto

data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: String
)
