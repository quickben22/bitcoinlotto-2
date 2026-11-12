package com.quickben22.bitcoinlotto.crypto

class WifKey(
    val privateKey: ByteArray,
    val compressed: Boolean,
) {
    val privateKeyHex: String get() = Hex.encode(privateKey)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WifKey) return false
        return compressed == other.compressed && privateKey.contentEquals(other.privateKey)
    }

    override fun hashCode(): Int = 31 * privateKey.contentHashCode() + compressed.hashCode()

    override fun toString(): String = "WifKey(privateKey=$privateKeyHex, compressed=$compressed)"
}

object Wif {
    private const val MAINNET_PREFIX = 0x80.toByte()
    private const val COMPRESSED_SUFFIX = 0x01.toByte()

    fun decode(wif: String): WifKey {
        val payload = Base58.decodeChecked(wif)
        require(payload.size == 33 || payload.size == 34) {
            "WIF payload must be 33 or 34 bytes, was ${payload.size}"
        }
        require(payload[0] == MAINNET_PREFIX) { "Only mainnet WIF keys are supported" }
        val compressed = payload.size == 34
        if (compressed) {
            require(payload[33] == COMPRESSED_SUFFIX) { "Invalid compressed WIF marker" }
        }
        val privateKey = payload.copyOfRange(1, 33)
        require(Secp256k1.isValidPrivateKey(privateKey)) { "WIF private key is out of range" }
        return WifKey(privateKey, compressed)
    }

    fun encode(privateKey: ByteArray, compressed: Boolean): String {
        require(Secp256k1.isValidPrivateKey(privateKey)) { "Private key must be a 32-byte value in [1, n-1]" }
        val payload = if (compressed) {
            byteArrayOf(MAINNET_PREFIX) + privateKey + COMPRESSED_SUFFIX
        } else {
            byteArrayOf(MAINNET_PREFIX) + privateKey
        }
        return Base58.encodeChecked(payload)
    }
}
