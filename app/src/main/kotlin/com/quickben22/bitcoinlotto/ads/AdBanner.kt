package com.quickben22.bitcoinlotto.ads

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.quickben22.bitcoinlotto.BuildConfig
import com.quickben22.bitcoinlotto.R

@Composable
fun AdBanner(canRequestAds: Boolean, modifier: Modifier = Modifier) {
    val adUnitId = BuildConfig.ADMOB_BANNER_UNIT_ID
    if (adUnitId.isBlank() || !canRequestAds) {
        AdPlaceholder(modifier)
        return
    }

    val context = LocalContext.current
    val widthDp = LocalConfiguration.current.screenWidthDp.coerceAtLeast(320)

    val adView = remember(adUnitId, widthDp) {
        AdView(context).apply {
            this.adUnitId = adUnitId
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp))
        }
    }

    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }

    LaunchedEffect(adView) {
        adView.loadAd(AdRequest.Builder().build())
    }

    AndroidView(
        factory = { adView },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp),
    )
}

@Composable
private fun AdPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.advertisement),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        )
    }
}
