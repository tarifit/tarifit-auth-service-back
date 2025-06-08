package com.tarifit.auth.domain

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByEmail(email: String): Optional<User>
    fun findByUsername(username: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
}
