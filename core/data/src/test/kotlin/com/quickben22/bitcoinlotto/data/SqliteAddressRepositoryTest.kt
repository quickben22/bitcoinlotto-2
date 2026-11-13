package com.quickben22.bitcoinlotto.data

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * The bundled asset ships only the addresses that are still dormant, as defined
 * by `docs/zombie-bitcoins.md`: status `still_funded` with a current balance at
 * or above the snapshot balance. Every other cohort is deliberately excluded.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SqliteAddressRepositoryTest {

    @Test
    fun `public key lookup finds matching hash without encoding address`() {
        val key = com.quickben22.bitcoinlotto.crypto.BitcoinKey.fromHex(
            "d9eb4e2909d646dd931bb306a2b492475cff04097a2456416ec9b7dba5ac98b8",
        )
        val database = android.database.sqlite.SQLiteDatabase.create(null)
        try {
            database.execSQL("CREATE TABLE bitcoin (_id TEXT PRIMARY KEY, value INTEGER)")
            database.execSQL(
                "INSERT INTO bitcoin (_id, value) VALUES (?, ?)",
                arrayOf<Any>(key.addressCompressed, 0L),
            )
            val repository = SqliteAddressRepository(database)
            assertThat(repository.containsPublicKey(key.publicKeyCompressed)).isTrue()
            val absent = com.quickben22.bitcoinlotto.crypto.BitcoinKey.fromHex("0".repeat(63) + "1")
            assertThat(repository.containsPublicKey(absent.publicKeyCompressed)).isFalse()
        } finally {
            database.close()
        }
    }

    private val context: Context = RuntimeEnvironment.getApplication()

    @Test
    fun `bundled database holds only the still-dormant addresses`() {
        SqliteAddressRepository.open(context).use { repository ->
            assertThat(repository.count()).isEqualTo(82_086)
        }
    }

    @Test
    fun `still-dormant address is present`() {
        SqliteAddressRepository.open(context).use { repository ->
            assertThat(repository.contains(DORMANT)).isTrue()
        }
    }

    @Test
    fun `addresses that are no longer dormant are absent`() {
        SqliteAddressRepository.open(context).use { repository ->
            assertWithMessage("partially spent, so proved alive")
                .that(repository.contains(PARTIALLY_SPENT)).isFalse()
            assertWithMessage("drained to dust, a ghost wallet")
                .that(repository.contains(GHOST)).isFalse()
            assertWithMessage("fully emptied")
                .that(repository.contains(EMPTIED)).isFalse()
            assertWithMessage("never funded, zero at snapshot")
                .that(repository.contains(FixtureAddresses.KEY_1_COMPRESSED)).isFalse()
        }
    }

    @Test
    fun `lookup is exact`() {
        SqliteAddressRepository.open(context).use { repository ->
            assertThat(repository.contains(DORMANT)).isTrue()
            assertThat(repository.contains("1BgGZ9tcN4rm9KBzDn7KprQz87SZ26SAMH")).isFalse()
            assertThat(repository.contains("")).isFalse()
        }
    }

    @Test
    fun `database is installed once and reused`() {
        SqliteAddressRepository.open(context).use { first ->
            assertThat(first.count()).isEqualTo(82_086)
        }
        SqliteAddressRepository.open(context).use { second ->
            assertThat(second.contains(DORMANT)).isTrue()
        }
    }

    private companion object {
        /** Never spent since the snapshot: 7,995,717,423,394 sats at snapshot, slightly more today. */
        const val DORMANT = "1FeexV6bAHb8ybZjqQMjJrcCrHGW9sb6uF"

        /** Still funded, but spent most of its 3,962.62 BTC. */
        const val PARTIALLY_SPENT = "15MZvKjqeNz4AVz2QrHumQcRJq2JVHjFUz"

        /** Snapshot 10,009.26 BTC, drained to 17,835 sats, below the 0.001 BTC ghost threshold. */
        const val GHOST = "1f1miYFQWTzdLiCBxtHHnNiW7WAWPUccr"

        /** Emptied: 4,154.25 BTC at the snapshot, 0 today. */
        const val EMPTIED = "1FdjFtrBwf9Jc9fsGN2GtHmG2vs5ZcEuWH"
    }
}
