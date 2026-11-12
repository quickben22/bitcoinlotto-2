package com.quickben22.bitcoinlotto.data

import android.content.Context
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SqliteAddressRepositoryTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    @Test
    fun `bundled database contains every fixture address`() {
        SqliteAddressRepository.open(context).use { repository ->
            assertThat(repository.count()).isEqualTo(92_083)
            FixtureAddresses.all.forEach { address ->
                assertThat(repository.contains(address)).isTrue()
            }
        }
    }

    @Test
    fun `lookup is exact`() {
        SqliteAddressRepository.open(context).use { repository ->
            assertThat(repository.contains(FixtureAddresses.KEY_1_COMPRESSED)).isTrue()
            assertThat(repository.contains("1BgGZ9tcN4rm9KBzDn7KprQz87SZ26SAMH")).isFalse()
            assertThat(repository.contains("")).isFalse()
        }
    }

    @Test
    fun `database is installed once and reused`() {
        SqliteAddressRepository.open(context).use { first ->
            assertThat(first.count()).isEqualTo(92_083)
        }
        SqliteAddressRepository.open(context).use { second ->
            assertThat(second.contains(FixtureAddresses.KEY_3_UNCOMPRESSED)).isTrue()
        }
    }
}
