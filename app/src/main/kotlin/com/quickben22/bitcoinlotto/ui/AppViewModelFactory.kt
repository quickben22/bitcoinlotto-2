package com.quickben22.bitcoinlotto.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quickben22.bitcoinlotto.AppContainer
import com.quickben22.bitcoinlotto.feature.solver.SolverViewModel
import com.quickben22.bitcoinlotto.feature.stats.StatsViewModel

class AppViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(StatsViewModel::class.java) -> StatsViewModel(
            searchStateHolder = container.searchState,
            checkpointStore = container.checkpointStore,
            searchController = container.searchController,
            privateKeyGenerator = container.privateKeyGenerator,
        ) as T

        modelClass.isAssignableFrom(SolverViewModel::class.java) -> SolverViewModel(
            checkPrivateKey = container.checkPrivateKey,
            privateKeyGenerator = container.privateKeyGenerator,
        ) as T

        else -> error("Unknown ViewModel class: ${modelClass.name}")
    }
}
