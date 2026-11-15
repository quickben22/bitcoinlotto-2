package com.quickben22.bitcoinlotto

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner

class BitcoinLottoApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(container.searchController)
    }
}
