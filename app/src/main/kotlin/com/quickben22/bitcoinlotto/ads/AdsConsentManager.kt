package com.quickben22.bitcoinlotto.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.quickben22.bitcoinlotto.BuildConfig

class AdsConsentManager(context: Context) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context.applicationContext)

    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun requestConsent(activity: Activity, onComplete: () -> Unit) {
        val parameters = ConsentRequestParameters.Builder().apply {
            if (BuildConfig.DEBUG) {
                setConsentDebugSettings(
                    ConsentDebugSettings.Builder(activity)
                        .setDebugGeography(
                            ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA,
                        )
                        .build(),
                )
            }
        }.build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            parameters,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                    onComplete()
                }
            },
            { _ -> onComplete() },
        )
    }

    fun showPrivacyOptions(activity: Activity, onDismiss: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { _ -> onDismiss() }
    }
}
