package com.tarifit.auth.service

import com.tarifit.auth.domain.User
import com.tarifit.auth.domain.UserRepository
import com.tarifit.auth.dto.LoginDto
import com.tarifit.auth.dto.RegisterDto
import com.tarifit.auth.exception.InvalidPasswordException
import com.tarifit.auth.exception.TokenValidationException
import com.tarifit.auth.exception.UserAlreadyExistsException
import com.tarifit.auth.exception.UserNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.util.ReflectionTestUtils
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthServiceTest {

    private lateinit var authService: AuthService
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: BCryptPasswordEncoder

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        passwordEncoder = mock()
        authService = AuthService(userRepository, passwordEncoder)
        
        // Set JWT properties using reflection
        ReflectionTestUtils.setField(authService, "jwtSecret", "mySecretKeymySecretKeymySecretKeymySecretKey")
        ReflectionTestUtils.setField(authService, "jwtExpiration", 86400L)
    }

    @Test
    fun `register should create new user successfully`() {
        // Given
        val registerDto = RegisterDto(
            email = "test@example.com",
            username = "testuser",
            password = "password123"
        )
        val encodedPassword = "encodedPassword"
        val savedUser = User(
            id = "userId123",
            email = registerDto.email,
            username = registerDto.username,
            passwordHash = encodedPassword,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isActive = true
        )

        whenever(userRepository.existsByEmail(registerDto.email)).thenReturn(false)
        whenever(userRepository.existsByUsername(registerDto.username)).thenReturn(false)
        whenever(passwordEncoder.encode(registerDto.password)).thenReturn(encodedPassword)
        whenever(userRepository.save(any<User>())).thenReturn(savedUser)

        // When
        val result = authService.register(registerDto)

        // Then
        assertNotNull(result)
        assertEquals(savedUser.id, result.userId)
        assertEquals(savedUser.email, result.email)
        assertEquals(savedUser.username, result.username)
        assertNotNull(result.token)
        assertNotNull(result.expiresAt)
        verify(userRepository).existsByEmail(registerDto.email)
        verify(userRepository).existsByUsername(registerDto.username)
        verify(passwordEncoder).encode(registerDto.password)
        verify(userRepository).save(any<User>())
    }

    @Test
    fun `register should throw exception when email already exists`() {
        // Given
        val registerDto = RegisterDto(
            email = "test@example.com",
            username = "testuser",
            password = "password123"
        )

        whenever(userRepository.existsByEmail(registerDto.email)).thenReturn(true)

        // When & Then
        assertThrows<UserAlreadyExistsException> {
            authService.register(registerDto)
        }
        verify(userRepository).existsByEmail(registerDto.email)
    }

    @Test
    fun `register should throw exception when username already exists`() {
        // Given
        val registerDto = RegisterDto(
            email = "test@example.com",
            username = "testuser",
            password = "password123"
        )

        whenever(userRepository.existsByEmail(registerDto.email)).thenReturn(false)
        whenever(userRepository.existsByUsername(registerDto.username)).thenReturn(true)

        // When & Then
        assertThrows<UserAlreadyExistsException> {
            authService.register(registerDto)
        }
        verify(userRepository).existsByEmail(registerDto.email)
        verify(userRepository).existsByUsername(registerDto.username)
    }

    @Test
    fun `login should authenticate user successfully`() {
        // Given
        val loginDto = LoginDto(
            email = "test@example.com",
            password = "password123"
        )
        val user = User(
            id = "userId123",
            email = loginDto.email,
            username = "testuser",
            passwordHash = "encodedPassword",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isActive = true
        )

        whenever(userRepository.findByEmail(loginDto.email)).thenReturn(user)
        whenever(passwordEncoder.matches(loginDto.password, user.passwordHash)).thenReturn(true)

        // When
        val result = authService.login(loginDto)

        // Then
        assertNotNull(result)
        assertEquals(user.id, result.userId)
        assertEquals(user.email, result.email)
        assertEquals(user.username, result.username)
        assertNotNull(result.token)
        assertNotNull(result.expiresAt)
        verify(userRepository).findByEmail(loginDto.email)
        verify(passwordEncoder).matches(loginDto.password, user.passwordHash)
    }

    @Test
    fun `login should throw exception when user not found`() {
        // Given
        val loginDto = LoginDto(
            email = "nonexistent@example.com",
            password = "password123"
        )

        whenever(userRepository.findByEmail(loginDto.email)).thenReturn(null)

        // When & Then
        assertThrows<UserNotFoundException> {
            authService.login(loginDto)
        }
        verify(userRepository).findByEmail(loginDto.email)
    }

    @Test
    fun `login should throw exception when password is invalid`() {
        // Given
        val loginDto = LoginDto(
            email = "test@example.com",
            password = "wrongpassword"
        )
        val user = User(
            id = "userId123",
            email = loginDto.email,
            username = "testuser",
            passwordHash = "encodedPassword",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isActive = true
        )

        whenever(userRepository.findByEmail(loginDto.email)).thenReturn(user)
        whenever(passwordEncoder.matches(loginDto.password, user.passwordHash)).thenReturn(false)

        // When & Then
        assertThrows<InvalidPasswordException> {
            authService.login(loginDto)
        }
        verify(userRepository).findByEmail(loginDto.email)
        verify(passwordEncoder).matches(loginDto.password, user.passwordHash)
    }

    @Test
    fun `login should throw exception when user is inactive`() {
        // Given
        val loginDto = LoginDto(
            email = "test@example.com",
            password = "password123"
        )
        val user = User(
            id = "userId123",
            email = loginDto.email,
            username = "testuser",
            passwordHash = "encodedPassword",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isActive = false // User is inactive
        )

        whenever(userRepository.findByEmail(loginDto.email)).thenReturn(user)
        whenever(passwordEncoder.matches(loginDto.password, user.passwordHash)).thenReturn(true)

        // When & Then
        assertThrows<UserNotFoundException> {
            authService.login(loginDto)
        }
        verify(userRepository).findByEmail(loginDto.email)
        verify(passwordEncoder).matches(loginDto.password, user.passwordHash)
    }

    @Test
    fun `validateToken should return valid for correct token`() {
        // Given
        val registerDto = RegisterDto(
            email = "test@example.com",
            username = "testuser",
            password = "password123"
        )
        val savedUser = User(
            id = "userId123",
            email = registerDto.email,
            username = registerDto.username,
            passwordHash = "encodedPassword",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isActive = true
        )

        whenever(userRepository.existsByEmail(registerDto.email)).thenReturn(false)
        whenever(userRepository.existsByUsername(registerDto.username)).thenReturn(false)
        whenever(passwordEncoder.encode(registerDto.password)).thenReturn("encodedPassword")
        whenever(userRepository.save(any<User>())).thenReturn(savedUser)

        // First register to get a valid token
        val authResponse = authService.register(registerDto)
        val token = "Bearer ${authResponse.token}"

        // When
        val result = authService.validateToken(token)

        // Then
        assertTrue(result["valid"] as Boolean)
        assertNotNull(result["message"])
    }

    @Test
    fun `validateToken should return invalid for malformed token`() {
        // Given
        val invalidToken = "Bearer invalid.jwt.token"

        // When
        val result = authService.validateToken(invalidToken)

        // Then
        assertFalse(result["valid"] as Boolean)
        assertNotNull(result["message"])
    }

    @Test
    fun `getCurrentUser should throw exception for invalid token`() {
        // Given
        val invalidToken = "Bearer invalid.jwt.token"

        // When & Then
        assertThrows<TokenValidationException> {
            authService.getCurrentUser(invalidToken)
        }
    }

    @Test
    fun `getCurrentUser should return user ID for valid token`() {
        // Given
        val registerDto = RegisterDto(
            email = "test@example.com",
            username = "testuser",
            password = "password123"
        )
        val savedUser = User(
            id = "userId123",
            email = registerDto.email,
            username = registerDto.username,
            passwordHash = "encodedPassword",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isActive = true
        )

        whenever(userRepository.existsByEmail(registerDto.email)).thenReturn(false)
        whenever(userRepository.existsByUsername(registerDto.username)).thenReturn(false)
        whenever(passwordEncoder.encode(registerDto.password)).thenReturn("encodedPassword")
        whenever(userRepository.save(any<User>())).thenReturn(savedUser)

        // First register to get a valid token
        val authResponse = authService.register(registerDto)
        val token = "Bearer ${authResponse.token}"

        // When
        val result = authService.getCurrentUser(token)

        // Then
        assertEquals(savedUser.id, result["userId"])
    }
}
