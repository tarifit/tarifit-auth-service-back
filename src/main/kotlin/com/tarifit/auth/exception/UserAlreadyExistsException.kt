package com.tarifit.auth.exception

data class UserAlreadyExistsException(val msg: String) : AuthException(msg)