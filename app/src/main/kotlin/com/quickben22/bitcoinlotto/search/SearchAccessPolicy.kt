package com.quickben22.bitcoinlotto.search

import com.quickben22.bitcoinlotto.data.SearchAccess

internal const val FIRST_REWARDED_UNLOCK_THRESHOLD = 6_000_000L
internal const val PERMANENT_UNLOCK_THRESHOLD = 60_000_000L

internal fun SearchAccess.keysUntilNextLock(): Long? = when {
    permanentlyUnlocked -> null
    firstRewardUsed -> (PERMANENT_UNLOCK_THRESHOLD - totalKeysChecked).coerceAtLeast(0L)
    else -> (FIRST_REWARDED_UNLOCK_THRESHOLD - totalKeysChecked).coerceAtLeast(0L)
}

internal fun SearchAccess.isSearchLocked(): Boolean =
    keysUntilNextLock() == 0L
