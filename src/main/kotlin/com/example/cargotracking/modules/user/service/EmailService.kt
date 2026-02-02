package com.example.cargotracking.modules.user.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EmailService {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    fun sendPasswordEmail(email: String, username: String, password: String) {
        logger.info("Sending password email to: $email")
        logger.info("Username: $username")
        logger.info("Temporary password: $password")

        logger.info("""
            |Email would be sent with:
            |To: $email
            |Subject: Your Account Credentials
            |Body: 
            |Hello $username,
            |
            |Your account has been created. Please use the following credentials to login:
            |Username: $username
            |Password: $password
            |
            |Please change your password after first login.
        """.trimMargin())
    }

    fun sendPasswordResetEmail(email: String, resetToken: String) {
        logger.info("Sending password reset email to: $email")
        logger.info("Reset token: $resetToken")

        logger.info("""
            |Email would be sent with:
            |To: $email
            |Subject: Password Reset Request
            |Body: 
            |Please use the following token to reset your password: $resetToken
        """.trimMargin())
    }
}
