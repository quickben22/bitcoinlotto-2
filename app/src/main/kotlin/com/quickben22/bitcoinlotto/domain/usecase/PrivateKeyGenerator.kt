package com.quickben22.bitcoinlotto.domain.usecase

import com.quickben22.bitcoinlotto.crypto.Secp256k1
import java.security.SecureRandom

fun interface PrivateKeyGenerator {
    fun generate(): ByteArray
}

class SecureRandomPrivateKeyGenerator(
    private val random: SecureRandom = SecureRandom(),
) : PrivateKeyGenerator {
    override fun generate(): ByteArray {
        val bytes = ByteArray(32)
        do {
            random.nextBytes(bytes)
        } while (!Secp256k1.isValidPrivateKey(bytes))
        return bytes
    }
}
