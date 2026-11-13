package com.quickben22.bitcoinlotto.search

import androidx.annotation.StringRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class SearchStatus {
    Idle,
    Running,
    Stopped,
    Found,
    LimitReached,
    InvalidKey,
    Error,
}

data class SearchHit(
    val privateKeyHex: String,
    val address: String,
    val compressed: Boolean,
    val keysChecked: Long,
    val elapsedMillis: Long,
) {
    override fun toString(): String =
        "SearchHit(privateKeyHex=[REDACTED], address=$address, compressed=$compressed, keysChecked=$keysChecked, elapsedMillis=$elapsedMillis)"
}

data class SearchState(
    val status: SearchStatus = SearchStatus.Idle,
    val keysChecked: Long = 0L,
    val elapsedMillis: Long = 0L,
    val keysPerSecond: Double = 0.0,
    val lastKeyHex: String = "",
    val hit: SearchHit? = null,
    val error: String? = null,
    @StringRes val errorMessage: Int? = null,
) {
    val isRunning: Boolean get() = status == SearchStatus.Running
}

class SearchStateHolder {
    private val mutableState = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = mutableState.asStateFlow()

    fun update(transform: (SearchState) -> SearchState) {
        mutableState.update(transform)
    }

    fun reset() {
        mutableState.value = SearchState()
    }
}
