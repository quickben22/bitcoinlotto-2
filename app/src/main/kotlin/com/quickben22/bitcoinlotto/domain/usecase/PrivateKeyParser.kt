package com.quickben22.bitcoinlotto.domain.usecase

import com.quickben22.bitcoinlotto.crypto.BitcoinKey
import com.quickben22.bitcoinlotto.crypto.Hex
import com.quickben22.bitcoinlotto.crypto.Secp256k1

sealed interface PrivateKeyParseResult {
    data class Valid(val key: BitcoinKey) : PrivateKeyParseResult

    data class Invalid(val reason: String) : PrivateKeyParseResult
}

object PrivateKeyParser {
    fun parse(input: String): PrivateKeyParseResult {
        val normalized = input.filterNot { it.isWhitespace() }.lowercase()
        if (normalized.length != 64) {
            return PrivateKeyParseResult.Invalid(
                "Private key must be exactly 64 hexadecimal characters",
            )
        }
        val bytes = try {
            Hex.decode(normalized)
        } catch (_: IllegalArgumentException) {
            return PrivateKeyParseResult.Invalid(
                "Private key must contain only hexadecimal characters",
            )
        }
        if (!Secp256k1.isValidPrivateKey(bytes)) {
            return PrivateKeyParseResult.Invalid(
                "Private key is outside the valid secp256k1 range",
            )
        }
        return PrivateKeyParseResult.Valid(BitcoinKey.fromPrivateKey(bytes))
    }
}
