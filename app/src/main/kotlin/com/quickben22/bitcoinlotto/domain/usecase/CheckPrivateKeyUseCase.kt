package com.quickben22.bitcoinlotto.domain.usecase

import androidx.annotation.StringRes
import com.quickben22.bitcoinlotto.crypto.BitcoinKey
import com.quickben22.bitcoinlotto.data.AddressRepository

sealed interface KeyCheckResult {
    data class Guessed(
        val key: BitcoinKey,
        val address: String,
        val compressed: Boolean,
    ) : KeyCheckResult

    data class NotGuessed(val key: BitcoinKey) : KeyCheckResult

    data class Invalid(@StringRes val reason: Int) : KeyCheckResult
}

class CheckPrivateKeyUseCase(
    private val repositoryProvider: () -> AddressRepository,
) {
    constructor(repository: AddressRepository) : this({ repository })

    operator fun invoke(input: String): KeyCheckResult =
        when (val parsed = PrivateKeyParser.parse(input)) {
            is PrivateKeyParseResult.Invalid -> KeyCheckResult.Invalid(parsed.reason)
            is PrivateKeyParseResult.Valid -> {
                val key = parsed.key
                val repository = repositoryProvider()
                when {
                    repository.contains(key.addressCompressed) -> KeyCheckResult.Guessed(
                        key = key,
                        address = key.addressCompressed,
                        compressed = true,
                    )

                    repository.contains(key.addressUncompressed) -> KeyCheckResult.Guessed(
                        key = key,
                        address = key.addressUncompressed,
                        compressed = false,
                    )

                    else -> KeyCheckResult.NotGuessed(key)
                }
            }
        }
}
