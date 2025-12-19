package com.example.cargotracking.common.security

import java.security.KeyPair
import java.security.KeyPairGenerator

object KeyGenerator {
    fun generateRSAKey(): KeyPair {
        try {
            val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048)
            return keyPairGenerator.genKeyPair()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}