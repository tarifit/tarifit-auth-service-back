package com.tarifit.auth.controller

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
        return authService.handleRegister(request)
    }
    
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<Any> {
        return authService.handleLogin(request)
    }
    
    @GetMapping("/validate")
    fun validate(@RequestHeader("Authorization") authorization: String): ResponseEntity<Map<String, Any>> {
        return authService.handleValidate(authorization)
    }
}
