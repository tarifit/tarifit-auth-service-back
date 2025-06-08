package com.tarifit.auth.domain

interface UserRepository {
    fun save(user: User): User
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
}
