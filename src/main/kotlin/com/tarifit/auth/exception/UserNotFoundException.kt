package com.tarifit.auth.exception

data class UserNotFoundException(val msg: String) : AuthException(msg)