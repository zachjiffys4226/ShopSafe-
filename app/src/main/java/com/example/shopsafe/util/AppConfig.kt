package com.example.shopsafe.util

import android.util.Log
import com.example.BuildConfig

/**
 * AppConfig singleton object that safely reads sensitive API keys (Google Maps, Stripe API Key, etc.)
 * via generated BuildConfig fields at runtime like BuildConfig.MAPS_API_KEY and BuildConfig.STRIPE_API_KEY.
 * Prevents hardcoding secrets in source code.
 */
object AppConfig {
    private const val TAG = "AppConfig"

    /**
     * Retrieves the Google Maps API key via BuildConfig.MAPS_API_KEY at runtime.
     */
    val mapsApiKey: String
        get() {
            return try {
                val field = BuildConfig::class.java.getField("MAPS_API_KEY")
                val key = field.get(null) as? String ?: ""
                if (key.isBlank() || key == "DUMMY_TEST_KEY") {
                    Log.w(TAG, "Maps API key is missing or default in BuildConfig.")
                }
                key
            } catch (e: Exception) {
                try {
                    val key = BuildConfig.MAPS_API_KEY
                    if (key.isBlank() || key == "DUMMY_TEST_KEY") {
                        Log.w(TAG, "Maps API key is missing or default in BuildConfig.")
                    }
                    key
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to read MAPS_API_KEY from BuildConfig: ${e.message}")
                    ""
                }
            }
        }

    var isStripeLiveMode: Boolean = false

    /**
     * Retrieves the Stripe API key via BuildConfig dynamically based on the current mode (Test vs Live).
     */
    val stripeApiKey: String
        get() {
            val keyName = if (isStripeLiveMode) "STRIPE_LIVE_PUBLISHABLE_KEY" else "STRIPE_TEST_PUBLISHABLE_KEY"
            return try {
                val field = BuildConfig::class.java.getField(keyName)
                field.get(null) as? String ?: ""
            } catch (e: Exception) {
                try {
                    val fallbackField = BuildConfig::class.java.getField("STRIPE_API_KEY")
                    fallbackField.get(null) as? String ?: ""
                } catch (e2: Exception) {
                    try {
                        val fallbackField2 = BuildConfig::class.java.getField("STRIPE_PUBLISHABLE_KEY")
                        fallbackField2.get(null) as? String ?: ""
                    } catch (e3: Exception) {
                        Log.e(TAG, "Failed to read Stripe API key from BuildConfig: ${e.message}")
                        ""
                    }
                }
            }
        }

    @Deprecated("Use stripeApiKey instead", ReplaceWith("stripeApiKey"))
    val stripePublishableKey: String
        get() = stripeApiKey

    /**
     * Validates that all required runtime API keys are properly configured.
     */
    fun validateConfiguration(): Boolean {
        val mapsValid = mapsApiKey.isNotBlank() && mapsApiKey != "DUMMY_TEST_KEY"
        val stripeValid = stripeApiKey.isNotBlank()
        
        if (!mapsValid) {
            Log.w(TAG, "Configuration Warning: Google Maps API key is not set or invalid.")
        }
        if (!stripeValid) {
            Log.w(TAG, "Configuration Warning: Stripe API key is not set or invalid.")
        }
        return mapsValid && stripeValid
    }
}
