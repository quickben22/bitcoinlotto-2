package com.quickben22.bitcoinlotto.data

data class SearchAccess(
    val totalKeysChecked: Long = 0L,
    val firstRewardUsed: Boolean = false,
    val permanentlyUnlocked: Boolean = false,
)
