    
    @Test
    fun `register should throw exception when email already exists`() {
        val registerDto = RegisterDto(
            email = "existing@test.com",
            username = "newuser",
            password = "Password123"
        )
        
        `when`(userRepository.existsByEmail(registerDto.email)).thenReturn(true)
        
        val exception = assertThrows<UserAlreadyExistsException> {
            authService.register(registerDto)
        }
        
        assertEquals(ErrorMessages.EMAIL_ALREADY_EXISTS.message, exception.message)
        verify(userRepository).existsByEmail(registerDto.email)
        verify(userRepository, never()).save(any(User::class.java))
    }
    
    @Test
    fun `register should throw exception when username already exists`() {
        val registerDto = RegisterDto(
            email = "new@test.com",
            username = "existinguser",
            password = "Password123"
        )
        
        `when`(userRepository.existsByEmail(registerDto.email)).thenReturn(false)
        `when`(userRepository.existsByUsername(registerDto.username)).thenReturn(true)
        
        val exception = assertThrows<UserAlreadyExistsException> {
            authService.register(registerDto)
        }
        
        assertEquals(ErrorMessages.USERNAME_ALREADY_EXISTS.message, exception.message)
        verify(userRepository).existsByEmail(registerDto.email)
        verify(userRepository).existsByUsername(registerDto.username)
        verify(userRepository, never()).save(any(User::class.java))
    }
    
    @Test
    fun `login should authenticate user successfully`() {
        val loginDto = LoginDto(
            email = "test@test.com",
            password = "password123"
        )
        
        `when`(userRepository.findByEmail(loginDto.email)).thenReturn(testUser)
        `when`(passwordEncoder.matches(loginDto.password, testUser.passwordHash)).thenReturn(true)
        
        val result = authService.login(loginDto)
        
        assertNotNull(result.token)
        assertEquals(testUser.id, result.userId)
        assertEquals(testUser.email, result.email)
        assertEquals(testUser.username, result.username)
        assertEquals(ErrorMessages.LOGIN_SUCCESSFUL.message, result.message)
        
        verify(userRepository).findByEmail(loginDto.email)
        verify(passwordEncoder).matches(loginDto.password, testUser.passwordHash)
    }
    
    @Test
    fun `login should throw exception when user not found`() {
        val loginDto = LoginDto(
            email = "nonexistent@test.com",
            password = "password123"
        )
        
        `when`(userRepository.findByEmail(loginDto.email)).thenReturn(null)
        
        val exception = assertThrows<UserNotFoundException> {
            authService.login(loginDto)
        }
        
        assertEquals(ErrorMessages.INVALID_CREDENTIALS.message, exception.message)
        verify(userRepository).findByEmail(loginDto.email)
        verify(passwordEncoder, never()).matches(anyString(), anyString())
    }
    
    @Test
    fun `login should throw exception when password is invalid`() {
        val loginDto = LoginDto(
            email = "test@test.com",
            password = "wrongpassword"
        )
        
        `when`(userRepository.findByEmail(loginDto.email)).thenReturn(testUser)
        `when`(passwordEncoder.matches(loginDto.password, testUser.passwordHash)).thenReturn(false)
        
        val exception = assertThrows<InvalidPasswordException> {
            authService.login(loginDto)
        }
        
        assertEquals(ErrorMessages.INVALID_CREDENTIALS.message, exception.message)
        verify(userRepository).findByEmail(loginDto.email)
        verify(passwordEncoder).matches(loginDto.password, testUser.passwordHash)
    }
    
    @Test
    fun `login should throw exception when user is inactive`() {
        val inactiveUser = testUser.copy(isActive = false)
        val loginDto = LoginDto(
            email = "test@test.com",
            password = "password123"
        )
        
        `when`(userRepository.findByEmail(loginDto.email)).thenReturn(inactiveUser)
        `when`(passwordEncoder.matches(loginDto.password, inactiveUser.passwordHash)).thenReturn(true)
        
        val exception = assertThrows<UserNotFoundException> {
            authService.login(loginDto)
        }
        
        assertEquals("User account is inactive", exception.message)
        verify(userRepository).findByEmail(loginDto.email)
        verify(passwordEncoder).matches(loginDto.password, inactiveUser.passwordHash)
    }
    
    @Test
    fun `validateToken should return valid for correct token`() {
        // Generate a token first
        val registerDto = RegisterDto("test@test.com", "testuser", "Password123")
        `when`(userRepository.existsByEmail(anyString())).thenReturn(false)
        `when`(userRepository.existsByUsername(anyString())).thenReturn(false)
        `when`(passwordEncoder.encode(anyString())).thenReturn("encoded")
        `when`(userRepository.save(any(User::class.java))).thenReturn(testUser)
        
        val authResponse = authService.register(registerDto)
        val token = "Bearer ${authResponse.token}"
        
        val result = authService.validateToken(token)
        
        assertTrue(result["valid"] as Boolean)
        assertEquals(ErrorMessages.TOKEN_VALID.message, result["message"])
    }
    
    @Test
    fun `validateToken should return invalid for malformed token`() {
        val result = authService.validateToken("Bearer invalid-token")
        
        assertFalse(result["valid"] as Boolean)
        assertEquals(ErrorMessages.TOKEN_INVALID.message, result["message"])
    }
    
    @Test
    fun `getCurrentUser should return userId for valid token`() {
        // Generate a token first
        val registerDto = RegisterDto("test@test.com", "testuser", "Password123")
        `when`(userRepository.existsByEmail(anyString())).thenReturn(false)
        `when`(userRepository.existsByUsername(anyString())).thenReturn(false)
        `when`(passwordEncoder.encode(anyString())).thenReturn("encoded")
        `when`(userRepository.save(any(User::class.java))).thenReturn(testUser)
        
        val authResponse = authService.register(registerDto)
        val token = "Bearer ${authResponse.token}"
        
        val result = authService.getCurrentUser(token)
        
        assertEquals(testUser.id, result["userId"])
    }
    
    @Test
    fun `getCurrentUser should throw exception for invalid token`() {
        assertThrows<TokenValidationException> {
            authService.getCurrentUser("Bearer invalid-token")
        }
    }
}
