package com.quickben22.bitcoinlotto.search

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.quickben22.bitcoinlotto.R
import com.quickben22.bitcoinlotto.crypto.Hex
import com.quickben22.bitcoinlotto.data.NoOpSearchedKeyRangeStore
import com.quickben22.bitcoinlotto.data.SavedSearchHit
import com.quickben22.bitcoinlotto.data.SearchCheckpoint
import com.quickben22.bitcoinlotto.data.SearchCheckpointStore
import com.quickben22.bitcoinlotto.data.SearchedKeyRangeStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class VisibleSearchController(
    private val engine: SearchEngine,
    private val store: SearchCheckpointStore,
    private val state: SearchStateHolder,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    private val searchedRanges: SearchedKeyRangeStore = NoOpSearchedKeyRangeStore,
) : SearchController, DefaultLifecycleObserver {
    private var visible = false
    private var visibilityGeneration = 0L
    private var searchJob: Job? = null

    override fun onStart(owner: LifecycleOwner) {
        visible = true
    }

    override fun onStop(owner: LifecycleOwner) {
        visible = false
        visibilityGeneration++
        stop()
    }

    override fun start(
        startKeyHex: String,
        limit: Long?,
        resume: Boolean,
        skipSearchedRanges: Boolean,
    ) {
        if (!visible) {
            state.update {
                it.copy(status = SearchStatus.Stopped, errorMessage = R.string.search_visible_only)
            }
            return
        }
        val generation = visibilityGeneration
        val previous = searchJob
        previous?.cancel()
        val job = scope.launch(start = CoroutineStart.LAZY) {
            // The previous job owns its final save. Read a resume checkpoint only after it finishes.
            previous?.join()
            ensureActive()
            if (!visible || generation != visibilityGeneration) return@launch
            runSearch(startKeyHex, limit, resume, skipSearchedRanges)
        }
        searchJob = job
        job.start()
    }

    override fun stop() {
        searchJob?.cancel()
    }

    private suspend fun runSearch(
        startKeyHex: String,
        limit: Long?,
        resume: Boolean,
        skipSearchedRanges: Boolean,
    ) {
        var session: Session? = null
        var checkpointNeeded = false
        state.update { it.copy(status = SearchStatus.Running, error = null, errorMessage = null) }
        try {
            if (store.hit.first() != null) {
                state.update { it.copy(status = SearchStatus.Found) }
                return
            }
            val checkpoint = if (resume) store.checkpoint.first() else null
            if (resume && checkpoint == null) {
                state.update {
                    it.copy(status = SearchStatus.Error, errorMessage = R.string.search_checkpoint_missing)
                }
                return
            }
            val access = store.searchAccess.first()
            if (access.isSearchLocked()) {
                state.update {
                    it.copy(status = SearchStatus.LimitReached, errorMessage = R.string.search_unlock_required)
                }
                return
            }
            val requestedKeyHex = checkpoint?.nextKeyHex ?: startKeyHex
            val keyHex = if (checkpoint == null && skipSearchedRanges) {
                skipSearchedRange(requestedKeyHex)
            } else {
                requestedKeyHex
            }
            val key = try {
                Hex.decode(keyHex)
            } catch (_: IllegalArgumentException) {
                state.update {
                    it.copy(status = SearchStatus.InvalidKey, errorMessage = R.string.search_invalid_start_key)
                }
                return
            }
            if (key.size != 32) {
                state.update {
                    it.copy(status = SearchStatus.InvalidKey, errorMessage = R.string.search_start_key_size)
                }
                return
            }
            // A fresh search replaces any previous resume point, including one that may still be
            // saved by the job we just joined.
            if (!resume) store.clear()
            val initialCount = checkpoint?.keysChecked ?: 0L
            val gateLimit = access.keysUntilNextLock()?.let { Math.addExact(initialCount, it) }
            val effectiveLimit = listOfNotNull(limit, gateLimit).minOrNull()
            val active = Session(
                startKeyHex = checkpoint?.startKeyHex ?: keyHex,
                nextKeyHex = keyHex,
                limit = effectiveLimit,
                keysChecked = initialCount,
                recordedKeysChecked = initialCount,
                elapsedMillis = checkpoint?.elapsedMillis ?: 0L,
            )
            session = active
            checkpointNeeded = true
            state.update {
                SearchState(
                    status = SearchStatus.Running,
                    keysChecked = active.keysChecked,
                    elapsedMillis = active.elapsedMillis,
                    lastKeyHex = keyHex,
                )
            }
            engine.search(key, effectiveLimit, initialCount, active.elapsedMillis)
                .flowOn(Dispatchers.Default)
                .collect { event ->
                    when (event) {
                        is SearchEvent.Progress -> {
                            active.update(event.keysChecked, event.lastKeyHex, event.elapsedMillis)
                            state.update {
                                SearchState(
                                    status = SearchStatus.Running,
                                    keysChecked = event.keysChecked,
                                    elapsedMillis = event.elapsedMillis,
                                    keysPerSecond = event.keysPerSecond,
                                    lastKeyHex = event.lastKeyHex,
                                )
                            }
                            val now = SystemClock.elapsedRealtime()
                            if (now - active.lastSavedAt >= CHECKPOINT_INTERVAL_MILLIS) {
                                save(active)
                                active.lastSavedAt = now
                            }
                        }
                        is SearchEvent.Found -> {
                            active.update(event.keysChecked, event.privateKeyHex, event.elapsedMillis)
                            val hit = SearchHit(
                                event.privateKeyHex, event.address, event.compressed,
                                event.keysChecked, event.elapsedMillis,
                            )
                            withContext(NonCancellable) {
                                recordRange(active)
                                try {
                                    withTimeout(SAVE_DEADLINE_MILLIS) {
                                        store.saveHit(
                                            SavedSearchHit(
                                                event.privateKeyHex, event.address, event.compressed,
                                                event.keysChecked, event.elapsedMillis,
                                            ),
                                            active.keysChecked - active.recordedKeysChecked,
                                        )
                                    }
                                    checkpointNeeded = false
                                    state.update {
                                        SearchState(
                                            status = SearchStatus.Found,
                                            keysChecked = event.keysChecked,
                                            elapsedMillis = event.elapsedMillis,
                                            keysPerSecond = keysPerSecond(event.keysChecked, event.elapsedMillis),
                                            lastKeyHex = event.privateKeyHex,
                                            hit = hit,
                                        )
                                    }
                                } catch (error: Exception) {
                                    checkpointNeeded = false
                                    Log.e(TAG, "Failed to save found key", error)
                                    state.update {
                                        it.copy(
                                            status = SearchStatus.Error,
                                            hit = hit,
                                            errorMessage = R.string.search_hit_save_error,
                                        )
                                    }
                                }
                            }
                        }
                        is SearchEvent.LimitReached -> {
                            active.update(event.keysChecked, event.lastKeyHex, event.elapsedMillis)
                            save(active)
                            checkpointNeeded = false
                            val locked = store.searchAccess.first().isSearchLocked()
                            state.update {
                                it.copy(
                                    status = SearchStatus.LimitReached,
                                    keysChecked = event.keysChecked,
                                    elapsedMillis = event.elapsedMillis,
                                    keysPerSecond = keysPerSecond(event.keysChecked, event.elapsedMillis),
                                    lastKeyHex = event.lastKeyHex,
                                    errorMessage = if (locked) R.string.search_unlock_required else null,
                                )
                            }
                        }
                        is SearchEvent.InvalidKey -> {
                            store.clear()
                            checkpointNeeded = false
                            state.update {
                                it.copy(status = SearchStatus.InvalidKey, errorMessage = event.reason)
                            }
                        }
                    }
                }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.e(TAG, "Search failed", error)
            state.update {
                it.copy(status = SearchStatus.Error, errorMessage = R.string.search_saved_data_error)
            }
        } finally {
            withContext(NonCancellable) {
                if (checkpointNeeded) {
                    session?.let {
                        try {
                            withTimeout(SAVE_DEADLINE_MILLIS) { save(it) }
                        } catch (error: Exception) {
                            Log.e(TAG, "Failed to save checkpoint", error)
                            state.update {
                                it.copy(status = SearchStatus.Error, errorMessage = R.string.search_saved_data_error)
                            }
                        }
                    }
                }
                state.update {
                    if (it.isRunning) it.copy(
                        status = SearchStatus.Stopped,
                        keysPerSecond = 0.0,
                        errorMessage = if (!visible) R.string.search_background_paused else null,
                    ) else it
                }
            }
        }
    }

    private suspend fun save(session: Session) {
        store.save(
            SearchCheckpoint(
                keysChecked = session.keysChecked,
                nextKeyHex = session.nextKeyHex,
                elapsedMillis = session.elapsedMillis,
                startKeyHex = session.startKeyHex,
                limit = session.limit,
            ),
            searchedKeysDelta = session.keysChecked - session.recordedKeysChecked,
        )
        session.recordedKeysChecked = session.keysChecked
        recordRange(session)
    }

    private suspend fun recordRange(session: Session) {
        if (session.nextKeyHex == session.startKeyHex) return
        try {
            searchedRanges.record(session.startKeyHex, session.nextKeyHex)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.e(TAG, "Failed to save searched key range", error)
        }
    }

    private suspend fun skipSearchedRange(keyHex: String): String = try {
        searchedRanges.endOfContainingRange(keyHex) ?: keyHex
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Log.e(TAG, "Failed to read searched key ranges", error)
        keyHex
    }

    private fun keysPerSecond(keysChecked: Long, elapsedMillis: Long): Double =
        if (elapsedMillis <= 0L) 0.0 else keysChecked * 1_000.0 / elapsedMillis

    private class Session(
        val startKeyHex: String,
        var nextKeyHex: String,
        val limit: Long?,
        var keysChecked: Long,
        var recordedKeysChecked: Long,
        var elapsedMillis: Long,
        var lastSavedAt: Long = SystemClock.elapsedRealtime(),
    ) {
        fun update(count: Long, lastKeyHex: String, elapsed: Long) {
            keysChecked = count
            elapsedMillis = elapsed
            val next = Hex.decode(lastKeyHex)
            check(incrementKey(next)) { "Key space exhausted while saving progress" }
            nextKeyHex = Hex.encode(next)
        }
    }

    private companion object {
        const val TAG = "VisibleSearchController"
        const val CHECKPOINT_INTERVAL_MILLIS = 2_000L
        const val SAVE_DEADLINE_MILLIS = 5_000L
    }
}
