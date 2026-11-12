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

    @Test
    fun `public key lookup finds matching hash without encoding address`() {
        val context = RuntimeEnvironment.getApplication()
        SqliteAddressRepository.open(context).use { repository ->
            val key = com.quickben22.bitcoinlotto.crypto.BitcoinKey.fromHex(
                "d9eb4e2909d646dd931bb306a2b492475cff04097a2456416ec9b7dba5ac98b8",
            )
            assertThat(repository.containsPublicKey(key.publicKeyCompressed)).isTrue()
            val absent = com.quickben22.bitcoinlotto.crypto.BitcoinKey.fromHex("0".repeat(63) + "1")
            assertThat(repository.containsPublicKey(absent.publicKeyCompressed)).isFalse()
        }
    }

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
