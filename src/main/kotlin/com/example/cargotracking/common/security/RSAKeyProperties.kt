package com.example.cargotracking.common.security

import org.springframework.stereotype.Component
import java.security.KeyPair
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@Component
class RSAKeyProperties(
    val accessTokenExpiration: Long = 3600000,
    val refreshTokenExpiration: Long = 86400000
) {
    private final val _publicKey: RSAPublicKey
    val publicKey: RSAPublicKey
        get() = _publicKey
    private final val _privateKey: RSAPrivateKey
    val privateKey: RSAPrivateKey
        get() = _privateKey

    init {
        val keyPair: KeyPair = KeyGenerator.generateRSAKey()
        this._publicKey = keyPair.public as RSAPublicKey
        this._privateKey = keyPair.private as RSAPrivateKey
    }
}