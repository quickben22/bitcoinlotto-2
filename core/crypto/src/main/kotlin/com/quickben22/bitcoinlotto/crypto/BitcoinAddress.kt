package com.quickben22.bitcoinlotto.crypto

object BitcoinAddress {
    private const val P2PKH_VERSION = 0x00.toByte()

    fun fromPublicKey(publicKey: ByteArray): String {
        require(publicKey.size == 33 || publicKey.size == 65) { "Unsupported public key length: ${publicKey.size}" }
        val payload = byteArrayOf(P2PKH_VERSION) + hash160(publicKey)
        return Base58.encodeChecked(payload)
    }

    fun decodeP2pkh(address: String): ByteArray? = try {
        val payload = Base58.decodeChecked(address)
        if (payload.size == 21 && payload[0] == P2PKH_VERSION) {
            payload.copyOfRange(1, 21)
        } else {
            null
        }
    } catch (_: IllegalArgumentException) {
        null
    }

    fun isP2pkh(address: String): Boolean = decodeP2pkh(address) != null
}
