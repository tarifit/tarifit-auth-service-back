package com.tarifit.auth.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "users")
data class User(
    @Id
    val id: String? = null,
    
    @Indexed(unique = true)
    val email: String,
    
    @Indexed(unique = true)
    val username: String,
    
    val passwordHash: String,
    
    val createdAt: Instant = Instant.now(),
    
    val updatedAt: Instant = Instant.now(),
    
    val isActive: Boolean = true
)
