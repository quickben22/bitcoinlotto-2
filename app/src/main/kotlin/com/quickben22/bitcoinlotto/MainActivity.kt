package com.quickben22.bitcoinlotto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.quickben22.bitcoinlotto.ads.AdsConsentManager
import com.quickben22.bitcoinlotto.ui.BitcoinLottoRoot
import com.quickben22.bitcoinlotto.ui.theme.BitcoinLottoTheme

class MainActivity : ComponentActivity() {
    private lateinit var consentManager: AdsConsentManager
    private var consentRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        consentManager = AdsConsentManager(this)
        enableEdgeToEdge()
        setContent {
            BitcoinLottoTheme {
                BitcoinLottoRoot()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!consentRequested) {
            consentRequested = true
            consentManager.requestConsent(this) { }
        }
    }
}
