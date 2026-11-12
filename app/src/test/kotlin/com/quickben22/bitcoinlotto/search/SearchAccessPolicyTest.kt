package com.quickben22.bitcoinlotto.search

import com.google.common.truth.Truth.assertThat
import com.quickben22.bitcoinlotto.data.SearchAccess
import org.junit.Test

class SearchAccessPolicyTest {
    @Test
    fun `first reward is required at six million keys`() {
        assertThat(SearchAccess(totalKeysChecked = 5_999_999L).keysUntilNextLock()).isEqualTo(1L)
        assertThat(SearchAccess(totalKeysChecked = 6_000_000L).isSearchLocked()).isTrue()
    }

    @Test
    fun `second reward is required at sixty million keys`() {
        val access = SearchAccess(
            totalKeysChecked = 60_000_000L,
            firstRewardUsed = true,
        )

        assertThat(access.isSearchLocked()).isTrue()
    }

    @Test
    fun `permanent reward removes future search limits`() {
        val access = SearchAccess(
            totalKeysChecked = 60_000_000L,
            firstRewardUsed = true,
            permanentlyUnlocked = true,
        )

        assertThat(access.isSearchLocked()).isFalse()
        assertThat(access.keysUntilNextLock()).isNull()
    }
}
