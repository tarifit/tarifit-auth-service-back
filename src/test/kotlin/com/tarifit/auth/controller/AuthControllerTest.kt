package com.tarifit.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.tarifit.auth.dto.AuthResponseDto
import com.tarifit.auth.dto.LoginDto
import com.tarifit.auth.dto.RegisterDto
import com.tarifit.auth.exception.InvalidPasswordException
import com.tarifit.auth.exception.TokenValidationException
import com.tarifit.auth.exception.UserAlreadyExistsException
import com.tarifit.auth.exception.UserNotFoundException
import com.tarifit.auth.service.AuthService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(AuthController::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var authService: AuthService

    @Test
    @WithMockUser
    fun `register should return 200 and AuthResponse when successful`() {
        // Given
        val registerDto = RegisterDto(
            email = "test@example.com",
            username = "testuser",
            password = "Password123"
        )
        val authResponse = AuthResponseDto(
            token = "jwt.token.here",
            userId = "userId123",
            email = registerDto.email,
            username = registerDto.username,
            message = "User registered successfully",
            expiresAt = System.currentTimeMillis() + 86400000
        )

        whenever(authService.register(registerDto)).thenReturn(authResponse)

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto))
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").value(authResponse.token))
            .andExpect(jsonPath("$.userId").value(authResponse.userId))
            .andExpect(jsonPath("$.email").value(authResponse.email))
            .andExpect(jsonPath("$.username").value(authResponse.username))
            .andExpect(jsonPath("$.message").value(authResponse.message))
            .andExpect(jsonPath("$.expiresAt").value(authResponse.expiresAt))
    }

    @Test
    @WithMockUser
    fun `register should return 400 when email already exists`() {
        // Given
        val registerDto = RegisterDto(
            email = "existing@example.com",
            username = "testuser",
            password = "Password123"
        )

        whenever(authService.register(registerDto)).thenThrow(
            UserAlreadyExistsException("Email already exists")
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto))
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `register should return 400 when username already exists`() {
        // Given
        val registerDto = RegisterDto(
            email = "test@example.com",
            username = "existinguser",
            password = "Password123"
        )

        whenever(authService.register(registerDto)).thenThrow(
            UserAlreadyExistsException("Username already exists")
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto))
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `register should return 400 when request body is invalid`() {
        // Given - Invalid request with missing email
        val invalidRequest = """
            {
                "username": "testuser",
                "password": "password123"
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `login should return 200 and AuthResponse when successful`() {
        // Given
        val loginDto = LoginDto(
            email = "test@example.com",
            password = "Password123"
        )
        val authResponse = AuthResponseDto(
            token = "jwt.token.here",
            userId = "userId123",
            email = loginDto.email,
            username = "testuser",
            message = "Login successful",
            expiresAt = System.currentTimeMillis() + 86400000
        )

        whenever(authService.login(loginDto)).thenReturn(authResponse)

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto))
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").value(authResponse.token))
            .andExpect(jsonPath("$.userId").value(authResponse.userId))
            .andExpect(jsonPath("$.email").value(authResponse.email))
            .andExpect(jsonPath("$.username").value(authResponse.username))
            .andExpect(jsonPath("$.message").value(authResponse.message))
            .andExpect(jsonPath("$.expiresAt").value(authResponse.expiresAt))
    }

    @Test
    @WithMockUser
    fun `login should return 404 when user not found`() {
        // Given
        val loginDto = LoginDto(
            email = "nonexistent@example.com",
            password = "Password123"
        )

        whenever(authService.login(loginDto)).thenThrow(
            UserNotFoundException("Invalid credentials")
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto))
                .with(csrf())
        )
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `login should return 401 when password is invalid`() {
        // Given
        val loginDto = LoginDto(
            email = "test@example.com",
            password = "wrongPassword123"
        )

        whenever(authService.login(loginDto)).thenThrow(
            InvalidPasswordException("Invalid credentials")
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto))
                .with(csrf())
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `login should return 400 when request body is invalid`() {
        // Given - Invalid request with missing password
        val invalidRequest = """
            {
                "email": "test@example.com"
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `validateToken should return 200 when token is valid`() {
        // Given
        val token = "Bearer valid.jwt.token"
        val validationResponse = mapOf(
            "valid" to true,
            "message" to "Token is valid"
        )

        whenever(authService.validateToken(token)).thenReturn(validationResponse)

        // When & Then
        mockMvc.perform(
            get("/api/v1/auth/validate")
                .header("Authorization", token)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.message").value("Token is valid"))
    }

    @Test
    @WithMockUser
    fun `validateToken should return 200 with invalid status when token is invalid`() {
        // Given
        val token = "Bearer invalid.jwt.token"
        val validationResponse = mapOf(
            "valid" to false,
            "message" to "Token is invalid"
        )

        whenever(authService.validateToken(token)).thenReturn(validationResponse)

        // When & Then
        mockMvc.perform(
            get("/api/v1/auth/validate")
                .header("Authorization", token)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.valid").value(false))
            .andExpect(jsonPath("$.message").value("Token is invalid"))
    }

    @Test
    fun `validateToken should return 401 when Authorization header is missing`() {
        // When & Then - Spring Security should block this
        mockMvc.perform(get("/api/v1/auth/validate"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `getCurrentUser should return 200 with user data when token is valid`() {
        // Given
        val token = "Bearer valid.jwt.token"
        val userResponse = mapOf("userId" to "userId123")

        whenever(authService.getCurrentUser(token)).thenReturn(userResponse)

        // When & Then
        mockMvc.perform(
            get("/api/v1/auth/me")
                .header("Authorization", token)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.userId").value("userId123"))
    }

    @Test
    @WithMockUser
    fun `getCurrentUser should return 401 when token is invalid`() {
        // Given
        val token = "Bearer invalid.jwt.token"

        whenever(authService.getCurrentUser(token)).thenThrow(
            TokenValidationException("Invalid token")
        )

        // When & Then
        mockMvc.perform(
            get("/api/v1/auth/me")
                .header("Authorization", token)
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `getCurrentUser should return 401 when Authorization header is missing`() {
        // When & Then - Spring Security should block this
        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().isUnauthorized)
    }
}
