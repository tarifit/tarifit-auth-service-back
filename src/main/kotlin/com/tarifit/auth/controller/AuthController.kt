package com.tarifit.auth.controller

import com.tarifit.auth.dto.AuthResponse
import com.tarifit.auth.dto.ErrorResponse
import com.tarifit.auth.dto.LoginRequest
import com.tarifit.auth.dto.RegisterRequest
import com.tarifit.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = ["*"])
class AuthController(private val authService: AuthService) {
    
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<Any> {
        return try {
            val response = authService.register(request)
            ResponseEntity.ok(response)
        } catch (e: RuntimeException) {
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "REGISTRATION_FAILED",
                    message = e.message ?: "Registration failed"
                )
            )
        }
    }
    
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<Any> {
        return try {
            val response = authService.login(request)
            ResponseEntity.ok(response)
        } catch (e: RuntimeException) {
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "LOGIN_FAILED",
                    message = e.message ?: "Login failed"
                )
            )
        }
    }
    
    @GetMapping("/validate")
    fun validate(@RequestHeader("Authorization") authorization: String): ResponseEntity<Map<String, Any>> {
        return try {
            val token = authorization.removePrefix("Bearer ")
            val isValid = authService.validateToken(token)
            
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
}
