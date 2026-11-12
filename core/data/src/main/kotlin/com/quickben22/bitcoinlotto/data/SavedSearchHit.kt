package com.quickben22.bitcoinlotto.data

data class SavedSearchHit(
    val privateKeyHex: String,
    val address: String,
    val compressed: Boolean,
    val keysChecked: Long,
    val elapsedMillis: Long,
) {
    override fun toString(): String =
        "SavedSearchHit(privateKeyHex=[REDACTED], address=$address, compressed=$compressed, keysChecked=$keysChecked, elapsedMillis=$elapsedMillis)"
}
