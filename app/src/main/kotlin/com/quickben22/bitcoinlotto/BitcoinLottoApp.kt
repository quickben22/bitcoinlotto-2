package com.quickben22.bitcoinlotto

import android.app.Application

class BitcoinLottoApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
