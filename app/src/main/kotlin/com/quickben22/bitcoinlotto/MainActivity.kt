package com.quickben22.bitcoinlotto

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.quickben22.bitcoinlotto.ads.AdsConsentManager
import com.quickben22.bitcoinlotto.search.SearchStateHolder
import com.quickben22.bitcoinlotto.ui.BitcoinLottoRoot
import com.quickben22.bitcoinlotto.ui.theme.BitcoinLottoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as BitcoinLottoApp).container
        val consentManager: AdsConsentManager = container.adsConsentManager
        consentManager.requestConsent(this)
        enableEdgeToEdge()
        setContent {
            BitcoinLottoTheme {
                BitcoinLottoRoot()
            }
        }
        keepScreenOnWhileSearching(container.searchState)
    }

    /**
     * Keep the device awake only while a search is actually running. The app stops searching
     * whenever it leaves the foreground, so the flag is also cleared on the way to STOPPED —
     * otherwise the screen could stay on indefinitely after backgrounding.
     */
    private fun keepScreenOnWhileSearching(searchState: SearchStateHolder) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    searchState.state.collect { state ->
                        if (state.isRunning) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        }
                    }
                } finally {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }
    }
}
