package com.tarifit.auth.exception

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GlobalExceptionHandlerTest {

    @Test
    fun `should create UserNotFoundException with message`() {
        val exception = UserNotFoundException("User not found")
        
        assertNotNull(exception)
        assertEquals("User not found", exception.message)
    }

    @Test
    fun `should create UserAlreadyExistsException with message`() {
        val exception = UserAlreadyExistsException("User already exists")
        
        assertNotNull(exception)
        assertEquals("User already exists", exception.message)
    }

    @Test
    fun `should create InvalidPasswordException with message`() {
        val exception = InvalidPasswordException("Invalid password")
        
        assertNotNull(exception)
        assertEquals("Invalid password", exception.message)
    }

    @Test
    fun `should create TokenValidationException with message`() {
        val exception = TokenValidationException("Invalid token")
        
        assertNotNull(exception)
        assertEquals("Invalid token", exception.message)
    }
}
