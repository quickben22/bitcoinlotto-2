package com.quickben22.bitcoinlotto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.quickben22.bitcoinlotto.ads.AdsConsentManager
import com.quickben22.bitcoinlotto.ui.BitcoinLottoRoot
import com.quickben22.bitcoinlotto.ui.theme.BitcoinLottoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val consentManager: AdsConsentManager = (application as BitcoinLottoApp).container.adsConsentManager
        consentManager.requestConsent(this)
        enableEdgeToEdge()
        setContent {
            BitcoinLottoTheme {
                BitcoinLottoRoot()
            }
        }
    }
}
