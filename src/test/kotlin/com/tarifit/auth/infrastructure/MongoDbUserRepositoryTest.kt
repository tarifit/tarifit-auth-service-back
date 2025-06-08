package com.tarifit.auth.infrastructure

import com.tarifit.auth.domain.User
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.Instant
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MongoDbUserRepositoryTest {
    
    @Mock
    private lateinit var mongoUserRepository: MongoUserRepository
    
    private lateinit var userRepository: MongoDbUserRepository
    
    private val testUser = User(
        id = "test-id",
        email = "test@test.com",
        username = "testuser",
        passwordHash = "hashedPassword",
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
        isActive = true
    )
    
    @BeforeTest
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        userRepository = MongoDbUserRepository(mongoUserRepository)
    }
    
    @Test
    fun `save should delegate to mongo repository`() {
        `when`(mongoUserRepository.save(testUser)).thenReturn(testUser)
        
        val result = userRepository.save(testUser)
        
        assertEquals(testUser, result)
        verify(mongoUserRepository).save(testUser)
    }
    
    @Test
    fun `findByEmail should return user when found`() {
        `when`(mongoUserRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser))
        
        val result = userRepository.findByEmail("test@test.com")
        
        assertEquals(testUser, result)
        verify(mongoUserRepository).findByEmail("test@test.com")
    }
    
    @Test
    fun `findByEmail should return null when not found`() {
        `when`(mongoUserRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty())
        
        val result = userRepository.findByEmail("nonexistent@test.com")
        
        assertNull(result)
        verify(mongoUserRepository).findByEmail("nonexistent@test.com")
    }
    
    @Test
    fun `findByUsername should return user when found`() {
        `when`(mongoUserRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser))
        
        val result = userRepository.findByUsername("testuser")
        
        assertEquals(testUser, result)
        verify(mongoUserRepository).findByUsername("testuser")
    }
    
    @Test
    fun `findByUsername should return null when not found`() {
        `when`(mongoUserRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty())
        
        val result = userRepository.findByUsername("nonexistentuser")
        
        assertNull(result)
        verify(mongoUserRepository).findByUsername("nonexistentuser")
    }
    
    @Test
    fun `existsByEmail should delegate to mongo repository`() {
        `when`(mongoUserRepository.existsByEmail("test@test.com")).thenReturn(true)
        
        val result = userRepository.existsByEmail("test@test.com")
        
        assertTrue(result)
        verify(mongoUserRepository).existsByEmail("test@test.com")
    }
    
    @Test
    fun `existsByUsername should delegate to mongo repository`() {
        `when`(mongoUserRepository.existsByUsername("testuser")).thenReturn(true)
        
        val result = userRepository.existsByUsername("testuser")
        
        assertTrue(result)
        verify(mongoUserRepository).existsByUsername("testuser")
    }
    
    @Test
    fun `findById should return user when found`() {
        `when`(mongoUserRepository.findById("test-id")).thenReturn(Optional.of(testUser))
        
        val result = userRepository.findById("test-id")
        
        assertEquals(testUser, result)
        verify(mongoUserRepository).findById("test-id")
    }
    
    @Test
    fun `findById should return null when not found`() {
        `when`(mongoUserRepository.findById("nonexistent-id")).thenReturn(Optional.empty())
        
        val result = userRepository.findById("nonexistent-id")
        
        assertNull(result)
        verify(mongoUserRepository).findById("nonexistent-id")
    }
}
