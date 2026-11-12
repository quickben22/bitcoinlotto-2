package com.quickben22.bitcoinlotto.search

import com.quickben22.bitcoinlotto.crypto.BitcoinKey
import com.quickben22.bitcoinlotto.crypto.Hex
import com.quickben22.bitcoinlotto.crypto.Secp256k1
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed interface SearchEvent {
    data class Progress(
        val keysChecked: Long,
        val increments: Long,
        val lastKeyHex: String,
        val elapsedMillis: Long,
        val keysPerSecond: Double,
    ) : SearchEvent

    data class Found(
        val privateKeyHex: String,
        val address: String,
        val compressed: Boolean,
        val keysChecked: Long,
        val increments: Long,
        val elapsedMillis: Long,
    ) : SearchEvent

    data class LimitReached(
        val keysChecked: Long,
        val lastKeyHex: String,
        val elapsedMillis: Long,
    ) : SearchEvent

    data class InvalidKey(val reason: String) : SearchEvent
}

fun interface ElapsedTimeSource {
    fun elapsedMillis(): Long
}

class SearchEngine(
    private val addressChecker: AddressChecker,
    private val timeSource: ElapsedTimeSource = ElapsedTimeSource { System.nanoTime() / 1_000_000L },
    private val progressIntervalMillis: Long = 250L,
) {
    fun search(
        startKey: ByteArray,
        limit: Long? = null,
        initialKeysChecked: Long = 0L,
        initialElapsedMillis: Long = 0L,
    ): Flow<SearchEvent> = flow {
        require(startKey.size == 32) { "Start key must be 32 bytes" }
        if (!Secp256k1.isValidPrivateKey(startKey)) {
            emit(SearchEvent.InvalidKey("Start key is outside the valid secp256k1 range"))
            return@flow
        }

        val key = startKey.copyOf()
        var keysChecked = initialKeysChecked
        var lastKeyHex = Hex.encode(key)
        val startedAt = timeSource.elapsedMillis()
        var lastProgressAt = startedAt

        while (true) {
            if (limit != null && keysChecked >= limit) {
                emit(
                    SearchEvent.LimitReached(
                        keysChecked = keysChecked,
                        lastKeyHex = lastKeyHex,
                        elapsedMillis = elapsed(initialElapsedMillis, startedAt),
                    ),
                )
                return@flow
            }

            val bitcoinKey = BitcoinKey.fromPrivateKey(key)
            keysChecked++
            lastKeyHex = bitcoinKey.privateKeyHex
            val match = addressChecker.match(bitcoinKey)
            if (match != null) {
                emit(
                    SearchEvent.Found(
                        privateKeyHex = bitcoinKey.privateKeyHex,
                        address = match.address,
                        compressed = match.compressed,
                        keysChecked = keysChecked,
                        increments = keysChecked - 1,
                        elapsedMillis = elapsed(initialElapsedMillis, startedAt),
                    ),
                )
                return@flow
            }

            val now = timeSource.elapsedMillis()
            if (now - lastProgressAt >= progressIntervalMillis) {
                val elapsedMillis = elapsed(initialElapsedMillis, startedAt)
                emit(
                    SearchEvent.Progress(
                        keysChecked = keysChecked,
                        increments = keysChecked - 1,
                        lastKeyHex = lastKeyHex,
                        elapsedMillis = elapsedMillis,
                        keysPerSecond = keysPerSecond(keysChecked, elapsedMillis),
                    ),
                )
                lastProgressAt = now
            }

            if (!incrementKey(key)) {
                emit(SearchEvent.InvalidKey("Key space exhausted"))
                return@flow
            }
            if (!Secp256k1.isValidPrivateKey(key)) {
                emit(SearchEvent.InvalidKey("Next key is outside the valid secp256k1 range"))
                return@flow
            }
            currentCoroutineContext().ensureActive()
        }
    }

    private fun elapsed(initialElapsedMillis: Long, startedAt: Long): Long =
        initialElapsedMillis + (timeSource.elapsedMillis() - startedAt).coerceAtLeast(0L)

    private fun keysPerSecond(keysChecked: Long, elapsedMillis: Long): Double =
        if (elapsedMillis <= 0L) 0.0 else keysChecked * 1_000.0 / elapsedMillis
}

fun incrementKey(key: ByteArray): Boolean {
    for (index in key.indices.reversed()) {
        val value = (key[index].toInt() and 0xFF) + 1
        key[index] = value.toByte()
        if (value <= 0xFF) return true
    }
    return false
}
