package com.tarifit.auth.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class StartupConfig : ApplicationRunner {
    
    private val logger = LoggerFactory.getLogger(StartupConfig::class.java)
    
    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String
    
    override fun run(args: ApplicationArguments?) {
        // Validate critical configuration on startup
        if (jwtSecret.isBlank() || jwtSecret == "tarifit-default-secret-key-for-development-only") {
            if (System.getenv("SPRING_PROFILES_ACTIVE") == "production") {
                logger.error("JWT secret must be configured for production environment!")
                throw IllegalStateException("JWT secret not properly configured for production")
            } else {
                logger.warn("Using default JWT secret - this should only be used in development!")
            }
        }
        
        logger.info("Authentication service started successfully")
    }
}