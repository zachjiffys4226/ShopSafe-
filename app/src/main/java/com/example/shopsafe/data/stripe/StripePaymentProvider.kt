package com.example.shopsafe.data.stripe

import android.content.Context
import android.util.Log
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * StripePaymentProvider initializes the Stripe SDK using PaymentConfiguration with the public key,
 * manages the Stripe SDK object instance, and coordinates ephemeral key injection for secure payment UI flows.
 */
class StripePaymentProvider(
    private val context: Context,
    val publishableKey: String = StripeService.DEFAULT_PUBLISHABLE_KEY,
    val stripeAccountId: String? = null
) {
    companion object {
        private const val TAG = "StripePaymentProvider"
    }

    // Stripe SDK instance
    val stripe: Stripe

    private val _providerState = MutableStateFlow(StripePaymentProviderState())
    val providerState: StateFlow<StripePaymentProviderState> = _providerState.asStateFlow()

    init {
        // 1. Initialize PaymentConfiguration with publishable key
        PaymentConfiguration.init(
            context = context.applicationContext,
            publishableKey = publishableKey,
            stripeAccountId = stripeAccountId
        )
        Log.d(TAG, "PaymentConfiguration initialized with publishable key: ${publishableKey.take(12)}...")

        // 2. Instantiate Stripe SDK instance
        stripe = Stripe(
            context = context.applicationContext,
            publishableKey = publishableKey,
            stripeAccountId = stripeAccountId
        )
        Log.d(TAG, "Stripe SDK instance successfully created.")

        _providerState.update {
            it.copy(
                isInitialized = true,
                publishableKey = publishableKey
            )
        }
    }

    /**
     * Attaches an ephemeral key retrieved from the backend to the provider state for payment UI initialization.
     */
    fun attachEphemeralKey(ephemeralKeySecret: String, cardId: String? = null) {
        _providerState.update {
            it.copy(
                activeEphemeralKeySecret = ephemeralKeySecret,
                associatedCardId = cardId,
                lastHandshakeTimestamp = System.currentTimeMillis(),
                errorMessage = null
            )
        }
        Log.d(TAG, "Ephemeral key attached to StripePaymentProvider successfully.")
    }

    /**
     * Records an error during payment or tokenization setup
     */
    fun recordError(message: String) {
        _providerState.update {
            it.copy(errorMessage = message)
        }
        Log.e(TAG, "StripePaymentProvider error: $message")
    }

    /**
     * Clears sensitive transient credentials
     */
    fun clearEphemeralState() {
        _providerState.update {
            it.copy(
                activeEphemeralKeySecret = null,
                errorMessage = null
            )
        }
        Log.d(TAG, "StripePaymentProvider ephemeral state cleared.")
    }
}

/**
 * State representing StripePaymentProvider readiness and ephemeral token configuration
 */
data class StripePaymentProviderState(
    val isInitialized: Boolean = false,
    val publishableKey: String? = null,
    val activeEphemeralKeySecret: String? = null,
    val associatedCardId: String? = null,
    val lastHandshakeTimestamp: Long? = null,
    val errorMessage: String? = null
)
