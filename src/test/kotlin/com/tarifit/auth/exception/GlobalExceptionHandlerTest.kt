package com.tarifit.auth.exception

import com.tarifit.auth.dto.ErrorResponseDto
import com.tarifit.auth.enums.ErrorMessages
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import kotlin.test.assertEquals

class GlobalExceptionHandlerTest {
    
    private lateinit var globalExceptionHandler: GlobalExceptionHandler
    
    @BeforeEach
    fun setUp() {
        globalExceptionHandler = GlobalExceptionHandler()
    }
    
    @Test
    fun `handleUserAlreadyExists should return 400 with correct error response`() {
        val exception = UserAlreadyExistsException("User already exists")
        
        val response = globalExceptionHandler.handleUserAlreadyExists(exception)
        
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("USER_ALREADY_EXISTS", response.body?.error)
        assertEquals("User already exists", response.body?.message)
    }
    
    @Test
    fun `handleUserNotFound should return 400 with correct error response`() {
        val exception = UserNotFoundException("User not found")
        
        val response = globalExceptionHandler.handleUserNotFound(exception)
        
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("USER_NOT_FOUND", response.body?.error)
        assertEquals("User not found", response.body?.message)
    }
    
    @Test
    fun `handleInvalidPassword should return 400 with credentials error`() {
        val exception = InvalidPasswordException("Invalid password")
        
        val response = globalExceptionHandler.handleInvalidPassword(exception)
        
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("INVALID_CREDENTIALS", response.body?.error)
        assertEquals(ErrorMessages.INVALID_CREDENTIALS.message, response.body?.message)
    }
    
    @Test
    fun `handleTokenValidation should return 400 with token error`() {
        val exception = TokenValidationException("Token validation failed")
        
        val response = globalExceptionHandler.handleTokenValidation(exception)
        
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("TOKEN_VALIDATION_FAILED", response.body?.error)
        assertEquals("Token validation failed", response.body?.message)
    }
    
    @Test
    fun `handleValidationErrors should return 400 with validation details`() {
        val bindingResult = BeanPropertyBindingResult(Any(), "testObject")
        bindingResult.addError(FieldError("testObject", "email", "Email is required"))
        bindingResult.addError(FieldError("testObject", "password", "Password is too short"))
        
        val exception = MethodArgumentNotValidException(null, bindingResult)
        
        val response = globalExceptionHandler.handleValidationErrors(exception)
        
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("VALIDATION_FAILED", response.body?.error)
        assertEquals("Validation failed: email: Email is required, password: Password is too short", response.body?.message)
    }
    
    @Test
    fun `handleGenericException should return 500 with generic error`() {
        val exception = RuntimeException("Unexpected error")
        
        val response = globalExceptionHandler.handleGenericException(exception)
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("INTERNAL_SERVER_ERROR", response.body?.error)
        assertEquals("An unexpected error occurred", response.body?.message)
    }
}
