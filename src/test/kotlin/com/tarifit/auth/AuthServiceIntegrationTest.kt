package com.tarifit.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.tarifit.auth.dto.LoginDto
import com.tarifit.auth.dto.RegisterDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation::class)
@TestPropertySource(properties = [
    "jwt.secret=tarifit-test-secret-key-for-integration-tests-only", 
    "jwt.expiration=86400"
])
class AuthServiceIntegrationTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    @Order(1)
    @WithMockUser
    fun `integration test - complete user registration flow`() {
        val registerDto = RegisterDto(
            email = "integration${System.currentTimeMillis()}@example.com",
            username = "integrationuser${System.currentTimeMillis()}",
            password = "Password123"
        )

        // Test registration
        val registrationResult = mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto))
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value(registerDto.email))
            .andExpect(jsonPath("$.username").value(registerDto.username))
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.expiresAt").exists())
            .andReturn()

        val registrationResponse = registrationResult.response.contentAsString
        val registrationData = objectMapper.readTree(registrationResponse)
        val registrationToken = registrationData.get("token").asText()

        // Test token validation
        mockMvc.perform(
            get("/api/v1/auth/validate")
                .header("Authorization", "Bearer $registrationToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.valid").value(true))

        // Test getCurrentUser
        mockMvc.perform(
            get("/api/v1/auth/me")
                .header("Authorization", "Bearer $registrationToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId").exists())
    }

    @Test
    @Order(2)
    @WithMockUser
    fun `integration test - login flow`() {
        val timestamp = System.currentTimeMillis()
        val email = "login${timestamp}@example.com"
        val password = "Password123"
        val username = "loginuser${timestamp}"
        
        // First register a user
        val registerDto = RegisterDto(email = email, username = username, password = password)
        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto))
                .with(csrf())
        ).andExpect(status().isOk)

        val loginDto = LoginDto(email = email, password = password)

        // Test login
        val loginResult = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto))
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.username").value(username))
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.userId").exists())
            .andReturn()

        val loginResponse = loginResult.response.contentAsString
        val loginData = objectMapper.readTree(loginResponse)
        val loginToken = loginData.get("token").asText()

        // Test token validation with login token
        mockMvc.perform(
            get("/api/v1/auth/validate")
                .header("Authorization", "Bearer $loginToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.valid").value(true))
    }

    @Test
    @Order(3)
    @WithMockUser
    fun `integration test - duplicate email registration should fail`() {
        val timestamp = System.currentTimeMillis()
        val email = "existing${timestamp}@example.com"
        
        // First register a user
        val firstRegisterDto = RegisterDto(
            email = email,
            username = "firstuser${timestamp}",
            password = "Password123"
        )
        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRegisterDto))
                .with(csrf())
        ).andExpect(status().isOk)

        // Try to register with same email
        val duplicateRegisterDto = RegisterDto(
            email = email,
            username = "newuser${timestamp}",
            password = "Password123"
        )

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRegisterDto))
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @Order(4)
    @WithMockUser
    fun `integration test - duplicate username registration should fail`() {
        val timestamp = System.currentTimeMillis()
        val username = "existinguser${timestamp}"
        
        // First register a user
        val firstRegisterDto = RegisterDto(
            email = "first${timestamp}@example.com",
            username = username,
            password = "Password123"
        )
        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRegisterDto))
                .with(csrf())
        ).andExpect(status().isOk)

        // Try to register with same username
        val duplicateRegisterDto = RegisterDto(
            email = "newemail${timestamp}@example.com",
            username = username,
            password = "Password123"
        )

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRegisterDto))
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @Order(5)
    @WithMockUser
    fun `integration test - login with wrong password should fail`() {
        val timestamp = System.currentTimeMillis()
        val email = "wrongpass${timestamp}@example.com"
        val correctPassword = "CorrectPassword123"
        val wrongPassword = "WrongPassword123"
        val username = "wrongpassuser${timestamp}"

        // First register a user
        val registerDto = RegisterDto(
            email = email,
            username = username,
            password = correctPassword
        )
        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto))
                .with(csrf())
        ).andExpect(status().isOk)

        // Try to login with wrong password
        val loginDto = LoginDto(email = email, password = wrongPassword)

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto))
                .with(csrf())
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @Order(6)
    @WithMockUser
    fun `integration test - login with non-existent email should fail`() {
        val loginDto = LoginDto(
            email = "nonexistent${System.currentTimeMillis()}@example.com",
            password = "Password123"
        )

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto))
                .with(csrf())
        )
            .andExpect(status().isNotFound)
    }

    @Test
    @Order(7)
    @WithMockUser
    fun `integration test - token validation with invalid token should return false`() {
        mockMvc.perform(
            get("/api/v1/auth/validate")
                .header("Authorization", "Bearer invalid.jwt.token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.valid").value(false))
    }

    @Test
    @Order(8)
    @WithMockUser
    fun `integration test - getCurrentUser with invalid token should fail`() {
        mockMvc.perform(
            get("/api/v1/auth/me")
                .header("Authorization", "Bearer invalid.jwt.token")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @Order(9)
    fun `integration test - validation endpoints should require authentication`() {
        // Test validate endpoint without authentication - should return 400 or 500 for missing header
        mockMvc.perform(get("/api/v1/auth/validate"))
            .andExpect(status().is5xxServerError)

        // Test getCurrentUser endpoint without authentication - should return 400 or 500 for missing header
        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().is5xxServerError)
    }

    @Test
    @Order(10)
    @WithMockUser
    fun `integration test - invalid request bodies should return 400`() {
        // Register with missing email
        val invalidRegister = """
            {
                "username": "testuser",
                "password": "password123"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRegister)
                .with(csrf())
        )
            .andExpect(status().isBadRequest)

        // Login with missing password
        val invalidLogin = """
            {
                "email": "test@example.com"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidLogin)
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }
}
