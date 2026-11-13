package com.quickben22.bitcoinlotto.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RewardedAdState {
    Idle,
    Loading,
    Ready,
    Showing,
    Failed,
}

class RewardedAdManager(
    context: Context,
    private val adUnitId: String,
) {
    private val appContext = context.applicationContext
    private val mutableState = MutableStateFlow(RewardedAdState.Idle)
    private var rewardedAd: RewardedAd? = null

    val state = mutableState.asStateFlow()

    fun load() {
        if (adUnitId.isBlank()) {
            mutableState.value = RewardedAdState.Failed
            return
        }
        if (mutableState.value != RewardedAdState.Idle &&
            mutableState.value != RewardedAdState.Failed
        ) {
            return
        }
        mutableState.value = RewardedAdState.Loading
        RewardedAd.load(
            appContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    mutableState.value = RewardedAdState.Ready
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    Log.w(TAG, "Rewarded ad failed to load: ${error.message}")
                    mutableState.value = RewardedAdState.Failed
                }
            },
        )
    }

    fun show(activity: Activity, onRewardEarned: () -> Unit) {
        val ad = rewardedAd ?: return
        rewardedAd = null
        mutableState.value = RewardedAdState.Showing
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                mutableState.value = RewardedAdState.Idle
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                Log.w(TAG, "Rewarded ad failed to show: ${error.message}")
                mutableState.value = RewardedAdState.Failed
            }
        }
        ad.show(activity) { onRewardEarned() }
    }

    private companion object {
        const val TAG = "RewardedAdManager"
    }
}
