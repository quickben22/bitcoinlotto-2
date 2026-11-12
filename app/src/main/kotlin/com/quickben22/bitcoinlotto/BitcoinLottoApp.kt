package com.quickben22.bitcoinlotto

import android.app.Application
import com.google.android.gms.ads.MobileAds

class BitcoinLottoApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        runCatching { MobileAds.initialize(this) }
    }
}
