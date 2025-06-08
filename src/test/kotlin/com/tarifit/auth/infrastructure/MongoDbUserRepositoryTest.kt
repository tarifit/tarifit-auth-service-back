package com.tarifit.auth.infrastructure

import com.tarifit.auth.domain.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MongoDbUserRepositoryTest {

    private lateinit var mongoDbUserRepository: MongoDbUserRepository
    private lateinit var mongoUserRepository: MongoUserRepository

    @BeforeEach
    fun setUp() {
        mongoUserRepository = mock()
        mongoDbUserRepository = MongoDbUserRepository(mongoUserRepository)
    }

    @Test
    fun `save should delegate to MongoUserRepository`() {
        // Given
        val user = User(
            id = null,
            email = "test@example.com",
            username = "testuser",
            passwordHash = "hashedPassword",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isActive = true
        )

        val savedUser = user.copy(id = "userId123")

        whenever(mongoUserRepository.save(user)).thenReturn(savedUser)

        // When
        val result = mongoDbUserRepository.save(user)

        // Then
        assertEquals(savedUser, result)
        verify(mongoUserRepository).save(user)
    }

    @Test
    fun `findByEmail should delegate to MongoUserRepository`() {
        // Given
        val email = "test@example.com"
        val user = User(
            id = "userId123",
            email = email,
            username = "testuser",
            passwordHash = "hashedPassword",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isActive = true
        )

        whenever(mongoUserRepository.findByEmail(email)).thenReturn(user)

        // When
        val result = mongoDbUserRepository.findByEmail(email)

        // Then
        assertEquals(user, result)
        verify(mongoUserRepository).findByEmail(email)
    }

    @Test
    fun `findByEmail should return null when user not found`() {
        // Given
        val email = "nonexistent@example.com"

        whenever(mongoUserRepository.findByEmail(email)).thenReturn(null)

        // When
        val result = mongoDbUserRepository.findByEmail(email)

        // Then
        assertNull(result)
        verify(mongoUserRepository).findByEmail(email)
    }

    @Test
    fun `existsByEmail should delegate to MongoUserRepository and return true`() {
        // Given
        val email = "test@example.com"

        whenever(mongoUserRepository.existsByEmail(email)).thenReturn(true)

        // When
        val result = mongoDbUserRepository.existsByEmail(email)

        // Then
        assertTrue(result)
        verify(mongoUserRepository).existsByEmail(email)
    }

    @Test
    fun `existsByEmail should delegate to MongoUserRepository and return false`() {
        // Given
        val email = "nonexistent@example.com"

        whenever(mongoUserRepository.existsByEmail(email)).thenReturn(false)

        // When
        val result = mongoDbUserRepository.existsByEmail(email)

        // Then
        assertFalse(result)
        verify(mongoUserRepository).existsByEmail(email)
    }

    @Test
    fun `existsByUsername should delegate to MongoUserRepository and return true`() {
        // Given
        val username = "testuser"

        whenever(mongoUserRepository.existsByUsername(username)).thenReturn(true)

        // When
        val result = mongoDbUserRepository.existsByUsername(username)

        // Then
        assertTrue(result)
        verify(mongoUserRepository).existsByUsername(username)
    }

    @Test
    fun `existsByUsername should delegate to MongoUserRepository and return false`() {
        // Given
        val username = "nonexistentuser"

        whenever(mongoUserRepository.existsByUsername(username)).thenReturn(false)

        // When
        val result = mongoDbUserRepository.existsByUsername(username)

        // Then
        assertFalse(result)
        verify(mongoUserRepository).existsByUsername(username)
    }
}
