package com.quickben22.bitcoinlotto.data

data class SearchCheckpoint(
    val keysChecked: Long,
    val nextKeyHex: String,
    val elapsedMillis: Long,
    val startKeyHex: String,
    val limit: Long?,
)
