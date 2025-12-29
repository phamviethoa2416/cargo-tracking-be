package com.example.cargotracking.common.security

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

@Component
class RSAKeyProperties(
    @Value($$"${jwt.public-key-path:classpath:keys/public.pem}")
    private val publicKeyResource: Resource,

    @Value($$"${jwt.private-key-path:classpath:keys/private.pem}")
    private val privateKeyResource: Resource,

    @Value($$"${jwt.access-token-expiration:3600000}")
    val accessTokenExpiration: Long,

    @Value($$"${jwt.refresh-token-expiration:86400000}")
    val refreshTokenExpiration: Long
) {
    //TODO: enable issuer & audience when scaling

    lateinit var publicKey: RSAPublicKey
    lateinit var privateKey: RSAPrivateKey

    @PostConstruct
    fun initKeys() {
        try {
            this.publicKey = loadPublicKey()
            this.privateKey = loadPrivateKey()
        } catch (e: Exception) {
            throw IllegalStateException("Cannot initialize RSA keys. Check file PEM in folder keys/", e)
        }
    }

    private fun loadPublicKey(): RSAPublicKey {
        val content = publicKeyResource.inputStream.bufferedReader().use { it.readText() }
        val cleanKey = content
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")

        val decoded = Base64.getDecoder().decode(cleanKey)
        val spec = X509EncodedKeySpec(decoded)
        return KeyFactory.getInstance("RSA").generatePublic(spec) as RSAPublicKey
    }

    private fun loadPrivateKey(): RSAPrivateKey {
        val content = privateKeyResource.inputStream.bufferedReader().use { it.readText() }
        val cleanKey = content
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val decoded = Base64.getDecoder().decode(cleanKey)
        val spec = PKCS8EncodedKeySpec(decoded)
        return KeyFactory.getInstance("RSA").generatePrivate(spec) as RSAPrivateKey
    }
}