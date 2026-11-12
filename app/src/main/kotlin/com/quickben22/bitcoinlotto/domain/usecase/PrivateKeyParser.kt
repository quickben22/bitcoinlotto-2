package com.quickben22.bitcoinlotto.domain.usecase

import androidx.annotation.StringRes
import com.quickben22.bitcoinlotto.R
import com.quickben22.bitcoinlotto.crypto.BitcoinKey
import com.quickben22.bitcoinlotto.crypto.Hex
import com.quickben22.bitcoinlotto.crypto.Secp256k1

sealed interface PrivateKeyParseResult {
    data class Valid(val key: BitcoinKey) : PrivateKeyParseResult

    data class Invalid(@StringRes val reason: Int) : PrivateKeyParseResult
}

object PrivateKeyParser {
    fun parse(input: String): PrivateKeyParseResult {
        val normalized = input.filterNot { it.isWhitespace() }.lowercase()
        if (normalized.length != 64) {
            return PrivateKeyParseResult.Invalid(
                R.string.private_key_length_error,
            )
        }
        val bytes = try {
            Hex.decode(normalized)
        } catch (_: IllegalArgumentException) {
            return PrivateKeyParseResult.Invalid(
                R.string.private_key_hex_error,
            )
        }
        if (!Secp256k1.isValidPrivateKey(bytes)) {
            return PrivateKeyParseResult.Invalid(
                R.string.private_key_range_error,
            )
        }
        return PrivateKeyParseResult.Valid(BitcoinKey.fromPrivateKey(bytes))
    }
}
