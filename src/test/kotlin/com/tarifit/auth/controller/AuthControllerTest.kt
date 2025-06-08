    
    @Test
    fun `login should return 200 when successful`() {
        val loginDto = LoginDto(
            email = "test@test.com",
            password = "password123"
        )
        
        val authResponse = AuthResponseDto(
            token = "jwt-token",
            userId = "user-id",
            email = "test@test.com",
            username = "testuser",
            message = ErrorMessages.LOGIN_SUCCESSFUL.message,
            expiresAt = System.currentTimeMillis() + 3600000
        )
        
        `when`(authService.login(loginDto)).thenReturn(authResponse)
        
        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.userId").value("user-id"))
            .andExpect(jsonPath("$.email").value("test@test.com"))
        
        verify(authService).login(loginDto)
    }
    
    @Test
    fun `login should return 400 when user not found`() {
        val loginDto = LoginDto(
            email = "nonexistent@test.com",
            password = "password123"
        )
        
        `when`(authService.login(loginDto))
            .thenThrow(UserNotFoundException(ErrorMessages.INVALID_CREDENTIALS.message))
        
        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
        
        verify(authService).login(loginDto)
    }
    
    @Test
    fun `login should return 400 when password is invalid`() {
        val loginDto = LoginDto(
            email = "test@test.com",
            password = "wrongpassword"
        )
        
        `when`(authService.login(loginDto))
            .thenThrow(InvalidPasswordException(ErrorMessages.INVALID_CREDENTIALS.message))
        
        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"))
        
        verify(authService).login(loginDto)
    }
    
    @Test
    fun `validateToken should return 200 when token is valid`() {
        val token = "Bearer valid-jwt-token"
        val response = mapOf("valid" to true, "message" to ErrorMessages.TOKEN_VALID.message)
        
        `when`(authService.validateToken(token)).thenReturn(response)
        
        mockMvc.perform(
            get("/api/v1/auth/validate")
                .header("Authorization", token)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.message").value(ErrorMessages.TOKEN_VALID.message))
        
        verify(authService).validateToken(token)
    }
    
    @Test
    fun `validateToken should return 200 when token is invalid`() {
        val token = "Bearer invalid-jwt-token"
        val response = mapOf("valid" to false, "message" to ErrorMessages.TOKEN_INVALID.message)
        
        `when`(authService.validateToken(token)).thenReturn(response)
        
        mockMvc.perform(
            get("/api/v1/auth/validate")
                .header("Authorization", token)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.valid").value(false))
            .andExpect(jsonPath("$.message").value(ErrorMessages.TOKEN_INVALID.message))
        
        verify(authService).validateToken(token)
    }
    
    @Test
    fun `getCurrentUser should return 200 when token is valid`() {
        val token = "Bearer valid-jwt-token"
        val response = mapOf("userId" to "user-id")
        
        `when`(authService.getCurrentUser(token)).thenReturn(response)
        
        mockMvc.perform(
            get("/api/v1/auth/me")
                .header("Authorization", token)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId").value("user-id"))
        
        verify(authService).getCurrentUser(token)
    }
}
