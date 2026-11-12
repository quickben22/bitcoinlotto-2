package com.quickben22.bitcoinlotto.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.quickben22.bitcoinlotto.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdsConsentManager(context: Context) {
    private val appContext = context.applicationContext
    private val mutableCanRequestAds = MutableStateFlow(false)
    val adRequestsAllowed = mutableCanRequestAds.asStateFlow()
    private var adsInitialized = false
    private var consentRequested = false

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context.applicationContext)

    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun requestConsent(activity: Activity) {
        if (consentRequested) return
        consentRequested = true
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
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                    if (error != null) {
                        consentRequested = false
                        Log.w("AdsConsentManager", "Consent form failed: ${error.message}")
                    }
                    updateConsent()
                }
            },
            { error ->
                consentRequested = false
                Log.w("AdsConsentManager", "Consent update failed: ${error.message}")
                updateConsent()
            },
        )
    }

    fun showPrivacyOptions(activity: Activity, onDismiss: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            if (error != null) Log.w("AdsConsentManager", "Privacy options failed: ${error.message}")
            updateConsent()
            onDismiss()
        }
    }

    private fun updateConsent() {
        val allowed = consentInformation.canRequestAds()
        if (allowed && !adsInitialized) {
            MobileAds.initialize(appContext)
            adsInitialized = true
        }
        mutableCanRequestAds.value = allowed
    }
}
