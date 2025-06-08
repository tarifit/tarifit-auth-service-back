package com.tarifit.auth.exception

sealed class AuthException(message: String) : RuntimeException(message)

class UserAlreadyExistsException(message: String) : AuthException(message)
class UserNotFoundException(message: String) : AuthException(message)
class InvalidPasswordException(message: String) : AuthException(message)
class TokenValidationException(message: String) : AuthException(message)
