package com.tarifit.auth.controller

import com.tarifit.auth.dto.AuthResponseDto
import com.tarifit.auth.dto.LoginDto
import com.tarifit.auth.dto.RegisterDto
import com.tarifit.auth.service.AuthService
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = ["http://localhost:3000"])
class AuthController(
    private val authService: AuthService
) {
    
    private val logger = KotlinLogging.logger {}
    
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterDto): ResponseEntity<AuthResponseDto> {
        logger.info { "Registration request received for email: ${request.email}" }
        val response = authService.register(request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginDto): ResponseEntity<AuthResponseDto> {
        logger.info { "Login request received for email: ${request.email}" }
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/validate")
    fun validateToken(@RequestHeader("Authorization") authorization: String): ResponseEntity<Map<String, Any>> {
        logger.debug { "Token validation request received" }
        val response = authService.validateToken(authorization)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/me")
    fun getCurrentUser(@RequestHeader("Authorization") authorization: String): ResponseEntity<Map<String, Any>> {
        val response = authService.getCurrentUser(authorization)
        return ResponseEntity.ok(response)
    }
}