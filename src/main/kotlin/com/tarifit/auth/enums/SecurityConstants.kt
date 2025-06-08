package com.tarifit.auth.enums

enum class SecurityConstants(val value: String) {
    TOKEN_PREFIX("Bearer "),
    HEADER_STRING("Authorization")
}