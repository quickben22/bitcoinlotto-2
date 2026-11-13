package com.quickben22.bitcoinlotto.crypto

import java.math.BigInteger
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.math.ec.ECPoint

object Secp256k1 {
    private val params = SECNamedCurves.getByName("secp256k1")
    val order: BigInteger = params.n
    private val generator = params.g

    fun isValidPrivateKey(privateKey: ByteArray): Boolean {
        if (privateKey.size != 32) return false
        val value = BigInteger(1, privateKey)
        return value.signum() > 0 && value < order
    }

    fun publicKey(privateKey: ByteArray, compressed: Boolean): ByteArray {
        require(isValidPrivateKey(privateKey)) { "Private key must be a 32-byte value in [1, n-1]" }
        val point = generator.multiply(BigInteger(1, privateKey)).normalize()
        return point.getEncoded(compressed)
    }

    fun sequentialPublicKeys(privateKey: ByteArray): SequentialPublicKeys {
        require(isValidPrivateKey(privateKey)) { "Private key must be a 32-byte value in [1, n-1]" }
        return SequentialPublicKeys(generator.multiply(BigInteger(1, privateKey)), generator)
    }
}

class SequentialPublicKeys internal constructor(
    private var point: ECPoint,
    private val generator: ECPoint,
) {
    fun encoded(compressed: Boolean): ByteArray {
        point = point.normalize()
        return point.getEncoded(compressed)
    }

    fun advance(): Boolean {
        val next = point.add(generator)
        if (next.isInfinity) return false
        point = next
        return true
    }
}
