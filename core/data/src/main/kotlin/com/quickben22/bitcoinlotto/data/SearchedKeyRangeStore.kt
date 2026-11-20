package com.quickben22.bitcoinlotto.data

/**
 * Persists the private-key ranges that have already been checked.
 *
 * Ranges are half-open: `from` is included, `to` is the next unchecked key.
 */
data class SearchedKeyRange(
    val id: Long,
    val fromHex: String,
    val toHex: String,
)

interface SearchedKeyRangeStore {
    /** Adds [fromHex, toHex) and merges it with any adjacent or overlapping ranges. */
    suspend fun record(fromHex: String, toHex: String)

    /** Returns the end of the range that contains [keyHex], or null when it is unchecked. */
    suspend fun endOfContainingRange(keyHex: String): String?

    suspend fun ranges(): List<SearchedKeyRange>
}

object NoOpSearchedKeyRangeStore : SearchedKeyRangeStore {
    override suspend fun record(fromHex: String, toHex: String) = Unit

    override suspend fun endOfContainingRange(keyHex: String): String? = null

    override suspend fun ranges(): List<SearchedKeyRange> = emptyList()
}
