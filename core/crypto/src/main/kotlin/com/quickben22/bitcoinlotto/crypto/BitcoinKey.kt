package com.quickben22.bitcoinlotto.crypto

class BitcoinKey private constructor(
    private val keyBytes: ByteArray,
    val compressed: Boolean,
) {
    val privateKey: ByteArray get() = keyBytes.copyOf()
    val privateKeyHex: String get() = Hex.encode(privateKey)

    val publicKeyCompressed: ByteArray by lazy { Secp256k1.publicKey(privateKey, compressed = true) }
    val publicKeyUncompressed: ByteArray by lazy { Secp256k1.publicKey(privateKey, compressed = false) }

    val addressCompressed: String by lazy { BitcoinAddress.fromPublicKey(publicKeyCompressed) }
    val addressUncompressed: String by lazy { BitcoinAddress.fromPublicKey(publicKeyUncompressed) }

    val wifCompressed: String by lazy { Wif.encode(privateKey, compressed = true) }
    val wifUncompressed: String by lazy { Wif.encode(privateKey, compressed = false) }

    fun addresses(): Set<String> = linkedSetOf(addressCompressed, addressUncompressed)

    override fun toString(): String =
        "BitcoinKey(privateKey=[REDACTED], compressed=$compressed)"

    companion object {
        fun fromPrivateKey(privateKey: ByteArray, compressed: Boolean = true): BitcoinKey {
            require(Secp256k1.isValidPrivateKey(privateKey)) {
                "Private key must be a 32-byte value in [1, n-1]"
            }
            return BitcoinKey(privateKey.copyOf(), compressed)
        }

        fun fromHex(privateKeyHex: String, compressed: Boolean = true): BitcoinKey =
            fromPrivateKey(Hex.decode(privateKeyHex), compressed)

        fun fromWif(wif: String): BitcoinKey {
            val decoded = Wif.decode(wif)
            return BitcoinKey(decoded.privateKey, decoded.compressed)
        }
    }
}
