package com.quickben22.bitcoinlotto.feature.stats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickben22.bitcoinlotto.crypto.Hex
import com.quickben22.bitcoinlotto.R
import com.quickben22.bitcoinlotto.data.SearchCheckpoint
import com.quickben22.bitcoinlotto.data.SearchCheckpointStore
import com.quickben22.bitcoinlotto.data.SearchAccess
import com.quickben22.bitcoinlotto.domain.usecase.PrivateKeyGenerator
import com.quickben22.bitcoinlotto.search.SearchController
import com.quickben22.bitcoinlotto.search.SearchState
import com.quickben22.bitcoinlotto.search.SearchStateHolder
import com.quickben22.bitcoinlotto.search.SearchHit
import com.quickben22.bitcoinlotto.search.SearchStatus
import com.quickben22.bitcoinlotto.search.isSearchLocked
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StatsViewModel(
    private val searchStateHolder: SearchStateHolder,
    private val checkpointStore: SearchCheckpointStore,
    private val searchController: SearchController,
    private val privateKeyGenerator: PrivateKeyGenerator,
) : ViewModel() {

    val state: StateFlow<SearchState> = searchStateHolder.state

    val access: StateFlow<SearchAccess> = checkpointStore.searchAccess.catch { error ->
        reportStorageError(error)
        emit(SearchAccess(totalKeysChecked = 60_000_000L, firstRewardUsed = true))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchAccess())

    val checkpoint: StateFlow<SearchCheckpoint?> = checkpointStore.checkpoint.catch { error ->
        reportStorageError(error)
        emit(null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )
    val hit: StateFlow<SearchHit?> = checkpointStore.hit.catch { error ->
        reportStorageError(error)
        emit(null)
    }.map { saved ->
        saved?.let {
            SearchHit(it.privateKeyHex, it.address, it.compressed, it.keysChecked, it.elapsedMillis)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun dismissHit() {
        viewModelScope.launch {
            try {
                checkpointStore.clearHit()
                searchStateHolder.update { it.copy(hit = null) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                reportStorageError(error)
            }
        }
    }

    fun onRewardEarned() {
        viewModelScope.launch {
            try {
                checkpointStore.grantRewardedUnlock()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                reportStorageError(error)
            }
        }
    }

    fun startOrStop() {
        if (state.value.isRunning) {
            searchController.stop()
            return
        }
        viewModelScope.launch {
            try {
                if (state.value.hit != null || checkpointStore.hit.first() != null) return@launch
                if (checkpointStore.searchAccess.first().isSearchLocked()) return@launch
                val checkpoint = checkpointStore.checkpoint.first()
                if (checkpoint != null) {
                    searchController.start(checkpoint.nextKeyHex, resume = true)
                } else {
                    searchController.start(Hex.encode(privateKeyGenerator.generate()))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                reportStorageError(error)
            }
        }
    }

    private fun reportStorageError(error: Throwable) {
        Log.e("StatsViewModel", "Search data is unavailable", error)
        searchStateHolder.update {
            it.copy(status = SearchStatus.Error, errorMessage = R.string.search_saved_data_error)
        }
    }

    fun stop() {
        searchController.stop()
    }
}
