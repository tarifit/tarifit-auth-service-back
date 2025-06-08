package com.tarifit.auth.infrastructure

import com.tarifit.auth.domain.User
import com.tarifit.auth.domain.UserRepository
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional

interface MongoUserRepository : MongoRepository<User, String> {
    fun findByEmail(email: String): Optional<User>
    fun findByUsername(username: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
}

@Repository
class MongoDbUserRepository(
    private val mongoUserRepository: MongoUserRepository
) : UserRepository {
    
    override fun save(user: User): User {
        return mongoUserRepository.save(user)
    }
    
    override fun findByEmail(email: String): User? {
        return mongoUserRepository.findByEmail(email).orElse(null)
    }
    
    override fun findByUsername(username: String): User? {
        return mongoUserRepository.findByUsername(username).orElse(null)
    }
    
    override fun existsByEmail(email: String): Boolean {
        return mongoUserRepository.existsByEmail(email)
    }
    
    override fun existsByUsername(username: String): Boolean {
        return mongoUserRepository.existsByUsername(username)
    }
    
    override fun findById(id: String): User? {
        return mongoUserRepository.findById(id).orElse(null)
    }
}