package com.example.shopsafe.util

import android.util.Log

/**
 * Environment management utility that securely delegates to AppConfig
 * for fetching API keys for Stripe and Maps via BuildConfig at runtime.
 */
object EnvManager {
    private const val TAG = "EnvManager"

    val mapsApiKey: String
        get() = AppConfig.mapsApiKey

    val stripeApiKey: String
        get() = AppConfig.stripePublishableKey

    fun validateKeys(): Boolean = AppConfig.validateConfiguration()
}
