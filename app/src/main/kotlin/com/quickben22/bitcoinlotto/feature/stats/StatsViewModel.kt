package com.quickben22.bitcoinlotto.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickben22.bitcoinlotto.crypto.Hex
import com.quickben22.bitcoinlotto.data.SearchCheckpoint
import com.quickben22.bitcoinlotto.data.SearchCheckpointStore
import com.quickben22.bitcoinlotto.domain.usecase.PrivateKeyGenerator
import com.quickben22.bitcoinlotto.search.SearchController
import com.quickben22.bitcoinlotto.search.SearchState
import com.quickben22.bitcoinlotto.search.SearchStateHolder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StatsViewModel(
    searchStateHolder: SearchStateHolder,
    private val checkpointStore: SearchCheckpointStore,
    private val searchController: SearchController,
    private val privateKeyGenerator: PrivateKeyGenerator,
) : ViewModel() {

    val state: StateFlow<SearchState> = searchStateHolder.state

    val checkpoint: StateFlow<SearchCheckpoint?> = checkpointStore.checkpoint.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    fun startOrStop() {
        if (state.value.isRunning) {
            searchController.stop()
            return
        }
        viewModelScope.launch {
            val checkpoint = runCatching { checkpointStore.checkpoint.first() }.getOrNull()
            if (checkpoint != null) {
                searchController.start(checkpoint.nextKeyHex, checkpoint.limit, resume = true)
            } else {
                searchController.start(Hex.encode(privateKeyGenerator.generate()))
            }
        }
    }

    fun stop() {
        searchController.stop()
    }
}
