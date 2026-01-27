package com.example.cargotracking.common.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.security.Key
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
    val publicKey: RSAPublicKey = loadPublicKey(publicKeyResource)
    val privateKey: RSAPrivateKey = loadPrivateKey(privateKeyResource)

    private fun loadPublicKey(resource: Resource): RSAPublicKey =
        loadKey(resource, "PUBLIC") {
            KeyFactory.getInstance("RSA")
                .generatePublic(X509EncodedKeySpec(it)) as RSAPublicKey
        }

    private fun loadPrivateKey(resource: Resource): RSAPrivateKey =
        loadKey(resource, "PRIVATE") {
            KeyFactory.getInstance("RSA")
                .generatePrivate(PKCS8EncodedKeySpec(it)) as RSAPrivateKey
        }


    private fun <T : Key> loadKey(
        resource: Resource,
        type: String,
        keyProvider: (ByteArray) -> T
    ): T {
        val content = resource.inputStream.bufferedReader().use { it.readText() }
            .replace("-----BEGIN $type KEY-----", "")
            .replace("-----END $type KEY-----", "")
            .replace("\\s".toRegex(), "")

        val decoded = Base64.getDecoder().decode(content)
        return keyProvider(decoded)
    }
}