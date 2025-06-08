package com.tarifit.auth.exception

import com.tarifit.auth.constants.ErrorMessages
import com.tarifit.auth.dto.ErrorResponseDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.Instant

@ControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExists(ex: UserAlreadyExistsException): ResponseEntity<ErrorResponseDto> {
        logger.warn("User already exists: ${ex.message}")
        return ResponseEntity.badRequest().body(
            ErrorResponseDto(
                error = "USER_ALREADY_EXISTS",
                message = ex.message,
                timestamp = Instant.now().toString()
            )
        )
    }
    
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ErrorResponseDto> {
        logger.warn("User not found: ${ex.message}")
        return ResponseEntity.badRequest().body(
            ErrorResponseDto(
                error = "USER_NOT_FOUND",
                message = ex.message,
                timestamp = Instant.now().toString()
            )
        )
    }
    
    @ExceptionHandler(InvalidPasswordException::class)
    fun handleInvalidPassword(ex: InvalidPasswordException): ResponseEntity<ErrorResponseDto> {
        logger.warn("Invalid password attempt")
        return ResponseEntity.badRequest().body(
            ErrorResponseDto(
                error = "INVALID_CREDENTIALS",
                message = ErrorMessages.INVALID_CREDENTIALS.message,
                timestamp = Instant.now().toString()
            )
        )
    }
    
    @ExceptionHandler(TokenValidationException::class)
    fun handleTokenValidation(ex: TokenValidationException): ResponseEntity<ErrorResponseDto> {
        logger.warn("Token validation failed: ${ex.message}")
        return ResponseEntity.badRequest().body(
            ErrorResponseDto(
                error = "TOKEN_VALIDATION_FAILED",
                message = ex.message,
                timestamp = Instant.now().toString()
            )
        )
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponseDto> {
        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { error: FieldError ->
            "${error.field}: ${error.defaultMessage}"
        }
        
        logger.warn("Validation failed: $errors")
        return ResponseEntity.badRequest().body(
            ErrorResponseDto(
                error = "VALIDATION_FAILED",
                message = "Validation failed: $errors",
                timestamp = Instant.now().toString()
            )
        )
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponseDto> {
        logger.error("Unexpected error occurred", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponseDto(
                error = "INTERNAL_SERVER_ERROR",
                message = "An unexpected error occurred",
                timestamp = Instant.now().toString()
            )
        )
    }
}