package com.tarifit.auth.controller

import com.tarifit.auth.constants.ErrorMessages
import com.tarifit.auth.dto.AuthResponseDto
import com.tarifit.auth.dto.LoginDto
import com.tarifit.auth.dto.RegisterDto
import com.tarifit.auth.service.AuthService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = ["http://localhost:3000"]) // Configure as needed
class AuthController(
    private val authService: AuthService
) {
    
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
    
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterDto): ResponseEntity<AuthResponseDto> {
        logger.info("Registration request received for email: ${request.email}")
        val response = authService.register(request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginDto): ResponseEntity<AuthResponseDto> {
        logger.info("Login request received for email: ${request.email}")
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/validate")
    fun validateToken(@RequestHeader("Authorization") authorization: String): ResponseEntity<Map<String, Any>> {
        logger.debug("Token validation request received")
        val isValid = authService.validateToken(authorization)
        
        return ResponseEntity.ok(
            mapOf(
                "valid" to isValid,
                "message" to if (isValid) ErrorMessages.TOKEN_VALID.message else ErrorMessages.TOKEN_INVALID.message
            )
        )
    }
    
    @GetMapping("/me")
    fun getCurrentUser(@RequestHeader("Authorization") authorization: String): ResponseEntity<Map<String, Any>> {
        val userId = authService.getUserIdFromToken(authorization)
            ?: return ResponseEntity.badRequest().body(
                mapOf("error" to "Invalid token")
            )
        
        return ResponseEntity.ok(
            mapOf("userId" to userId)
        )
    }
}