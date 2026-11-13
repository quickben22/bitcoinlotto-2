package com.quickben22.bitcoinlotto.search

import com.google.common.truth.Truth.assertThat
import com.quickben22.bitcoinlotto.crypto.BitcoinKey
import com.quickben22.bitcoinlotto.data.SqliteAddressRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SearchBenchmarkTest {
    @Test
    fun `compare sequential search with scalar and database lookups`() = runBlocking {
        SqliteAddressRepository.open(RuntimeEnvironment.getApplication()).use { repository ->
            val start = ByteArray(32).apply { this[30] = 1 }
            val checker = RepositoryAddressChecker(repository)
            repository.containsPublicKey(BitcoinKey.fromPrivateKey(start).publicKeyCompressed)

            val baselineKey = start.copyOf()
            val baselineStart = System.nanoTime()
            repeat(COUNT) {
                assertThat(checker.match(BitcoinKey.fromPrivateKey(baselineKey))).isNull()
                incrementKey(baselineKey)
            }
            val baselineNanos = System.nanoTime() - baselineStart

            val optimizedStart = System.nanoTime()
            val events = SearchEngine(checker).search(start, limit = COUNT.toLong()).toList()
            val optimizedNanos = System.nanoTime() - optimizedStart
            assertThat((events.last() as SearchEvent.LimitReached).keysChecked).isEqualTo(COUNT.toLong())
            println("Search benchmark ($COUNT keys): baseline ${baselineNanos / 1_000_000} ms; optimized ${optimizedNanos / 1_000_000} ms")
        }
    }

    private companion object {
        const val COUNT = 200
    }
}
