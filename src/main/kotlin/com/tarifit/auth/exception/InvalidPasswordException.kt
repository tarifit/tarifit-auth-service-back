package com.tarifit.auth.exception

data class InvalidPasswordException(val msg: String) : AuthException(msg)