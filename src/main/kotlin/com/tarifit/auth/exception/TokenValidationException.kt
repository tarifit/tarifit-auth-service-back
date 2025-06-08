package com.tarifit.auth.exception

data class TokenValidationException(val msg: String) : AuthException(msg)