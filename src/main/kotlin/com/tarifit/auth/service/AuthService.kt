package com.tarifit.auth.service

import com.tarifit.auth.constants.ErrorMessages
import com.tarifit.auth.domain.User
import com.tarifit.auth.domain.UserRepository
import com.tarifit.auth.dto.AuthResponseDto
import com.tarifit.auth.dto.LoginDto
import com.tarifit.auth.dto.RegisterDto
import com.tarifit.auth.exception.InvalidPasswordException
import com.tarifit.auth.exception.TokenValidationException
import com.tarifit.auth.exception.UserAlreadyExistsException
import com.tarifit.auth.exception.UserNotFoundException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder
) {
    
    private val logger = LoggerFactory.getLogger(AuthService::class.java)
    
    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String
    
    @Value("\${jwt.expiration}")
    private var jwtExpiration: Long = 86400 // 24 hours in seconds
    
    private fun getSigningKey(): SecretKey {
        return Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }
    
    fun register(request: RegisterDto): AuthResponseDto {
        logger.info("Attempting to register user with email: ${request.email}")
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.email)) {
            logger.warn("Registration failed - email already exists: ${request.email}")
            throw UserAlreadyExistsException(ErrorMessages.EMAIL_ALREADY_EXISTS.message)
        }
        
        if (userRepository.existsByUsername(request.username)) {
            logger.warn("Registration failed - username already exists: ${request.username}")
            throw UserAlreadyExistsException(ErrorMessages.USERNAME_ALREADY_EXISTS.message)
        }
        
        // Create new user
        val user = User(
            email = request.email,
            username = request.username,
            passwordHash = passwordEncoder.encode(request.password)
        )
        
        val savedUser = userRepository.save(user)
        logger.info("User registered successfully with ID: ${savedUser.id}")
        
        val (token, expiresAt) = generateToken(savedUser.id!!)
        
        return AuthResponseDto(
            token = token,
            userId = savedUser.id,
            email = savedUser.email,
            username = savedUser.username,
            message = ErrorMessages.USER_REGISTERED_SUCCESSFULLY.message,
            expiresAt = expiresAt
        )
    }
    
    fun login(request: LoginDto): AuthResponseDto {
        logger.info("Attempting to login user with email: ${request.email}")
        
        val user = userRepository.findByEmail(request.email)
            ?: run {
                logger.warn("Login failed - user not found: ${request.email}")
                throw UserNotFoundException(ErrorMessages.INVALID_CREDENTIALS.message)
            }
        
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            logger.warn("Login failed - invalid password for user: ${request.email}")
            throw InvalidPasswordException(ErrorMessages.INVALID_CREDENTIALS.message)
        }
        
        if (!user.isActive) {
            logger.warn("Login failed - user account is inactive: ${request.email}")
            throw UserNotFoundException("User account is inactive")
        }
        
        logger.info("User logged in successfully: ${user.id}")
        val (token, expiresAt) = generateToken(user.id!!)
        
        return AuthResponseDto(
            token = token,
            userId = user.id,
            email = user.email,
            username = user.username,
            message = ErrorMessages.LOGIN_SUCCESSFUL.message,
            expiresAt = expiresAt
        )
    }
    
    fun validateToken(token: String): Boolean {
        return try {
            val cleanToken = token.removePrefix("Bearer ").trim()
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(cleanToken)
            
            logger.debug("Token validation successful")
            true
        } catch (e: Exception) {
            logger.warn("Token validation failed: ${e.message}")
            false
        }
    }
    
    fun getUserIdFromToken(token: String): String? {
        return try {
            val cleanToken = token.removePrefix("Bearer ").trim()
            val claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(cleanToken)
                .payload
            
            claims.subject
        } catch (e: Exception) {
            logger.warn("Failed to extract user ID from token: ${e.message}")
            null
        }
    }
    
    private fun generateToken(userId: String): Pair<String, Long> {
        val now = Date()
        val expiry = Date(now.time + jwtExpiration * 1000)
        
        val token = Jwts.builder()
            .subject(userId)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(getSigningKey())
            .compact()
        
        return Pair(token, expiry.time)
    }
}