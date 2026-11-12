package com.quickben22.bitcoinlotto.search

import com.google.common.truth.Truth.assertThat
import com.quickben22.bitcoinlotto.FakeAddressRepository
import com.quickben22.bitcoinlotto.crypto.BitcoinKey
import com.quickben22.bitcoinlotto.crypto.Hex
import com.quickben22.bitcoinlotto.data.FixtureAddresses
import java.math.BigInteger
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchEngineTest {

    private val fixture = BitcoinKey.fromHex(
        "d9eb4e2909d646dd931bb306a2b492475cff04097a2456416ec9b7dba5ac98b8",
    )
    private val bothVariants = setOf(
        FixtureAddresses.KEY_1_COMPRESSED,
        FixtureAddresses.KEY_1_UNCOMPRESSED,
    )
    private val fixedTime = ElapsedTimeSource { 0L }

    @Test
    fun `starting at a fixture key reports a hit without incrementing`() = runTest {
        val events = engine(bothVariants).search(fixture.privateKey).toList()

        val found = events.single() as SearchEvent.Found
        assertThat(found.privateKeyHex).isEqualTo(fixture.privateKeyHex)
        assertThat(found.address).isEqualTo(FixtureAddresses.KEY_1_COMPRESSED)
        assertThat(found.compressed).isTrue()
        assertThat(found.keysChecked).isEqualTo(1L)
        assertThat(found.increments).isEqualTo(0L)
    }

    @Test
    fun `starting three keys before a fixture finds it after three increments`() = runTest {
        val startKey = subtract(fixture.privateKey, 3)

        val events = engine(bothVariants).search(startKey).toList()

        val found = events.last() as SearchEvent.Found
        assertThat(found.privateKeyHex).isEqualTo(fixture.privateKeyHex)
        assertThat(found.keysChecked).isEqualTo(4L)
        assertThat(found.increments).isEqualTo(3L)
    }

    @Test
    fun `both address variants present produce a single hit`() = runTest {
        val events = engine(bothVariants).search(fixture.privateKey).toList()

        assertThat(events.filterIsInstance<SearchEvent.Found>()).hasSize(1)
    }

    @Test
    fun `uncompressed-only match is detected`() = runTest {
        val events = engine(setOf(FixtureAddresses.KEY_1_UNCOMPRESSED))
            .search(fixture.privateKey)
            .toList()

        val found = events.single() as SearchEvent.Found
        assertThat(found.address).isEqualTo(FixtureAddresses.KEY_1_UNCOMPRESSED)
        assertThat(found.compressed).isFalse()
    }

    @Test
    fun `absent address stops at the limit`() = runTest {
        val events = engine(emptySet())
            .search(Hex.decode("0".repeat(63) + "1"), limit = 5)
            .toList()

        assertThat(events.none { it is SearchEvent.Found }).isTrue()
        val limit = events.last() as SearchEvent.LimitReached
        assertThat(limit.keysChecked).isEqualTo(5L)
    }

    @Test
    fun `checkpoint restart resumes at the next key without gaps`() = runTest {
        val startKey = subtract(fixture.privateKey, 5)
        val firstRun = engine(bothVariants)
            .search(startKey, limit = 2)
            .toList()
        val limit = firstRun.last() as SearchEvent.LimitReached
        assertThat(limit.lastKeyHex).isEqualTo(Hex.encode(subtract(fixture.privateKey, 4)))

        val nextKey = Hex.decode(limit.lastKeyHex)
        assertThat(incrementKey(nextKey)).isTrue()
        val secondRun = engine(bothVariants)
            .search(
                startKey = nextKey,
                limit = limit.keysChecked + 4,
                initialKeysChecked = limit.keysChecked,
            )
            .toList()

        val found = secondRun.last() as SearchEvent.Found
        assertThat(found.privateKeyHex).isEqualTo(fixture.privateKeyHex)
        assertThat(found.keysChecked).isEqualTo(6L)
        assertThat(found.increments).isEqualTo(5L)
    }

    @Test
    fun `increment carries across bytes and reports overflow`() {
        val key = Hex.decode("00000000000000000000000000000000000000000000000000000000000000ff")

        assertThat(incrementKey(key)).isTrue()
        assertThat(Hex.encode(key))
            .isEqualTo("0000000000000000000000000000000000000000000000000000000000000100")

        val allOnes = ByteArray(32) { 0xFF.toByte() }
        assertThat(incrementKey(allOnes)).isFalse()
        assertThat(allOnes).isEqualTo(ByteArray(32))
    }

    private fun engine(addresses: Set<String>): SearchEngine =
        SearchEngine(
            addressChecker = RepositoryAddressChecker(FakeAddressRepository(addresses)),
            timeSource = fixedTime,
        )

    private fun subtract(bytes: ByteArray, delta: Int): ByteArray {
        val value = BigInteger(1, bytes).subtract(BigInteger.valueOf(delta.toLong()))
        val raw = value.toByteArray()
        val copy = if (raw.size > 32) raw.copyOfRange(raw.size - 32, raw.size) else raw
        val out = ByteArray(32)
        copy.copyInto(out, 32 - copy.size)
        return out
    }
}
