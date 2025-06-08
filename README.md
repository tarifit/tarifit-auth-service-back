# Tarifit Auth Service Backend

Authentication microservice for the Tarifit Translator application built with Spring Boot and Kotlin.

## Description
Handles user authentication, registration, and JWT token management for the Tarifit Translator platform.

## Technology Stack
- **Framework**: Spring Boot 3.x
- **Language**: Kotlin
- **Database**: MongoDB
- **Security**: JWT, BCrypt
- **Build Tool**: Maven

## Features
- User registration
- User authentication  
- JWT token generation and validation
- Password hashing with BCrypt
- Basic user management

## API Endpoints
```
POST /api/v1/auth/register - User registration
POST /api/v1/auth/login    - User login
GET  /api/v1/auth/validate - Token validation
```

## Configuration
- Port: 8081
- MongoDB Connection: `tarifit_auth` database
- JWT Secret: Configured via environment variables

## CI/CD Status
✅ Repository configured with GitHub Actions and SonarCloud integration

## Repository
https://github.com/tarifit/tarifit-auth-service-back
