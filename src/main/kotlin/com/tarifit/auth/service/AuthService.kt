package com.tarifit.auth.service

import com.tarifit.auth.domain.User
import com.tarifit.auth.domain.UserRepository
import com.tarifit.auth.dto.AuthResponse
import com.tarifit.auth.dto.ErrorResponse
import com.tarifit.auth.dto.LoginRequest
import com.tarifit.auth.dto.RegisterRequest
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder = BCryptPasswordEncoder()
) {
    
    @Value("\${jwt.secret:tarifit-secret-key-2024}")
    private lateinit var jwtSecret: String
    
    @Value("\${jwt.expiration:86400}")
    private var jwtExpiration: Long = 86400 // 24 hours in seconds
    
    fun handleRegister(request: RegisterRequest): ResponseEntity<Any> {
        return try {
            val response = register(request)
            ResponseEntity.ok(response)
        } catch (e: RuntimeException) {
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "REGISTRATION_FAILED",
                    message = e.message ?: "Registration failed",
                    timestamp = Instant.now().toString()
                )
            )
        }
    }
    
    fun handleLogin(request: LoginRequest): ResponseEntity<Any> {
        return try {
            val response = login(request)
            ResponseEntity.ok(response)
        } catch (e: RuntimeException) {
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "LOGIN_FAILED", 
                    message = e.message ?: "Login failed",
                    timestamp = Instant.now().toString()
                )
            )
        }
    }
    
    fun handleValidate(authorization: String): ResponseEntity<Map<String, Any>> {
        return try {
            val token = authorization.removePrefix("Bearer ")
            val isValid = validateToken(token)
            
            ResponseEntity.ok(
                mapOf(
                    "valid" to isValid,
                    "message" to if (isValid) "Token is valid" else "Token is invalid"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                mapOf(
                    "valid" to false,
                    "message" to "Token validation failed"
                )
            )
        }
    }
    
    private fun register(request: RegisterRequest): AuthResponse {
        // Check if user already exists
        if (userRepository.existsByEmail(request.email)) {
            throw RuntimeException("Email already exists")
        }
        
        if (userRepository.existsByUsername(request.username)) {
            throw RuntimeException("Username already exists")
        }
        
        // Create new user
        val user = User(
            email = request.email,
            username = request.username,
            passwordHash = passwordEncoder.encode(request.password)
        )
        
        val savedUser = userRepository.save(user)
        val token = generateToken(savedUser.id!!)
        
        return AuthResponse(
            token = token,
            userId = savedUser.id!!,
            email = savedUser.email,
            username = savedUser.username,
            message = "User registered successfully"
        )
    }
    
    private fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { RuntimeException("Invalid email or password") }
        
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw RuntimeException("Invalid email or password")
        }
        
        val token = generateToken(user.id!!)
        
        return AuthResponse(
            token = token,
            userId = user.id!!,
            email = user.email,
            username = user.username,
            message = "Login successful"
        )
    }
    
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun generateToken(userId: String): String {
        val now = Date()
        val expiry = Date(now.time + jwtExpiration * 1000)
        
        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact()
    }
}
