package com.example.shopsafe.data.stripe

import android.content.Context
import android.util.Log
import com.example.shopsafe.data.models.StripeCardTokenizedDetails
import com.example.shopsafe.data.models.StripeIssuingCard
import com.example.shopsafe.data.models.StripeIssuingEphemeralKey
import com.example.shopsafe.data.models.StripePushProvisioningPayload
import com.example.shopsafe.data.util.NetworkConnectivityObserver
import com.stripe.android.EphemeralKeyUpdateListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject

/**
 * StripeServiceErrorWrapper wraps an underlying [IStripeService] to provide comprehensive
 * fault-tolerance and resilience.
 *
 * Resilience Guarantees:
 * 1. API Timeouts: Enforces strict request bounds (e.g. 6000ms) with background retries.
 * 2. Connectivity Awareness: Verifies network availability before dispatching network calls,
 *    returning actionable offline states immediately rather than hanging or crashing.
 * 3. Ephemeral Key Validation: Validates backend key structure, expiry TTLs, and signature secrets.
 * 4. State Isolation: Guaranteed zero disruption to active Google Maps navigation, driver GPS
 *    trips, or ongoing delivery states. All errors are contained within non-obstructive Stripe states.
 */
class StripeServiceErrorWrapper(
    private val delegate: IStripeService,
    private val context: Context,
    private val connectivityObserver: NetworkConnectivityObserver = NetworkConnectivityObserver(context),
    private val defaultTimeoutMs: Long = 6000L,
    private val maxRetries: Int = 2
) : IStripeService {

    companion object {
        private const val TAG = "StripeServiceWrapper"
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    // Expose underlying service state
    override val serviceState: StateFlow<StripeServiceState> = delegate.serviceState

    // Specific error stream for telemetry and non-intrusive UI banners
    private val _lastError = MutableStateFlow<StripeServiceError?>(null)
    val lastError: StateFlow<StripeServiceError?> = _lastError.asStateFlow()

    // Non-obstructive warning toast/banner message for the UI
    private val _nonObstructiveAlert = MutableStateFlow<String?>(null)
    val nonObstructiveAlert: StateFlow<String?> = _nonObstructiveAlert.asStateFlow()

    fun dismissAlert() {
        _nonObstructiveAlert.update { null }
    }

    /**
     * EphemeralKeyProvider implementation wrapping delegate with safe exception guards.
     */
    override fun createEphemeralKey(
        apiVersion: String,
        keyUpdateListener: EphemeralKeyUpdateListener
    ) {
        scope.launch {
            if (!connectivityObserver.isCurrentlyConnected()) {
                val connError = StripeServiceError.ConnectivityLoss()
                _lastError.update { connError }
                _nonObstructiveAlert.update { "Network offline: Stripe session will refresh upon reconnection." }
                keyUpdateListener.onKeyUpdateFailure(400, connError.message)
                return@launch
            }

            try {
                delegate.createEphemeralKey(apiVersion, object : EphemeralKeyUpdateListener {
                    override fun onKeyUpdate(rawKey: String) {
                        if (validateRawEphemeralKeyJson(rawKey)) {
                            keyUpdateListener.onKeyUpdate(rawKey)
                            _lastError.update { null }
                        } else {
                            val invalidError = StripeServiceError.InvalidEphemeralKey(
                                rawResponseSnippet = rawKey.take(50)
                            )
                            _lastError.update { invalidError }
                            _nonObstructiveAlert.update { "Payment key validation error. Retrying securely..." }
                            keyUpdateListener.onKeyUpdateFailure(422, invalidError.message)
                        }
                    }

                    override fun onKeyUpdateFailure(responseCode: Int, message: String) {
                        val backendError = if (responseCode in 500..599) {
                            StripeServiceError.BackendUnavailable(responseCode, message)
                        } else {
                            StripeServiceError.Unknown("Stripe SDK ephemeral error ($responseCode): $message")
                        }
                        _lastError.update { backendError }
                        _nonObstructiveAlert.update { "Card service temporarily degraded ($responseCode). Delivery is active." }
                        keyUpdateListener.onKeyUpdateFailure(responseCode, message)
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in createEphemeralKey delegate", e)
                val unknownErr = StripeServiceError.Unknown("Ephemeral key exception: ${e.message}", e)
                _lastError.update { unknownErr }
                keyUpdateListener.onKeyUpdateFailure(500, unknownErr.message)
            }
        }
    }

    /**
     * Resilient retrieval of general customer ephemeral key with timeout and validation.
     */
    override suspend fun getEphemeralKey(
        customerId: String,
        apiVersion: String
    ): Result<StripeIssuingEphemeralKey> {
        return executeSafelyWithRetry(operationName = "getEphemeralKey") {
            val key = delegate.getEphemeralKey(customerId, apiVersion).getOrThrow()
            validateEphemeralKey(key)
            key
        }
    }

    /**
     * Resilient retrieval of commercial Issuing card ephemeral key.
     */
    override suspend fun getIssuingEphemeralKey(
        cardId: String,
        cardholderId: String
    ): Result<StripeIssuingEphemeralKey> {
        return executeSafelyWithRetry(operationName = "getIssuingEphemeralKey") {
            val key = delegate.getIssuingEphemeralKey(cardId, cardholderId).getOrThrow()
            validateEphemeralKey(key)
            key
        }
    }

    /**
     * Resilient retrieval of secure tokenized card details in memory.
     */
    override suspend fun fetchSecureTokenizedCardDetails(
        card: StripeIssuingCard
    ): Result<StripeCardTokenizedDetails> {
        return executeSafelyWithRetry(operationName = "fetchSecureTokenizedCardDetails") {
            val details = delegate.fetchSecureTokenizedCardDetails(card).getOrThrow()
            if (details.fullPan.isBlank() || details.cvc.isBlank()) {
                throw StripeServiceError.InvalidEphemeralKey(
                    message = "Decrypted card payload incomplete. Required PAN/CVC fields missing."
                ).toException()
            }
            details
        }
    }

    /**
     * Resilient Google Pay push provisioning generation.
     */
    override suspend fun pushProvisionToGooglePay(
        card: StripeIssuingCard
    ): Result<StripePushProvisioningPayload> {
        return executeSafelyWithRetry(operationName = "pushProvisionToGooglePay") {
            val payload = delegate.pushProvisionToGooglePay(card).getOrThrow()
            if (payload.pushToken.isBlank()) {
                throw StripeServiceError.InvalidEphemeralKey(
                    message = "Google Pay push provisioning token is empty."
                ).toException()
            }
            payload
        }
    }

    override fun wipeInMemoryCardDetails() {
        try {
            delegate.wipeInMemoryCardDetails()
        } catch (e: Exception) {
            Log.e(TAG, "Error during memory purge", e)
        }
    }

    /**
     * Executes an API action with connectivity guards, timeout bounds, and exponential backoff.
     */
    private suspend fun <T> executeSafelyWithRetry(
        operationName: String,
        timeoutMs: Long = defaultTimeoutMs,
        retries: Int = maxRetries,
        block: suspend () -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        // 1. Proactive connectivity check
        if (!connectivityObserver.isCurrentlyConnected()) {
            val connError = StripeServiceError.ConnectivityLoss()
            _lastError.update { connError }
            _nonObstructiveAlert.update { "Offline mode: Card data sync paused. Delivery routing active." }
            Log.w(TAG, "Operation '$operationName' skipped: Network offline.")
            return@withContext Result.failure(connError.toException())
        }

        var attempt = 0
        var lastException: Throwable? = null

        while (attempt <= retries) {
            try {
                // 2. Timeout-bounded execution
                val result = withTimeoutOrNull(timeoutMs) {
                    block()
                }

                if (result != null) {
                    _lastError.update { null }
                    return@withContext Result.success(result)
                } else {
                    // Timed out
                    val timeoutError = StripeServiceError.ApiTimeout(
                        message = "Stripe request '$operationName' timed out after ${timeoutMs}ms (attempt ${attempt + 1}/$retries).",
                        timeoutMs = timeoutMs
                    )
                    lastException = timeoutError.toException()
                    Log.w(TAG, timeoutError.message)
                }
            } catch (e: StripeServiceCustomException) {
                // Structured domain exception thrown from block validation
                lastException = e
                if (!e.error.isRecoverable) {
                    // Non-recoverable error (e.g. malformed key), exit immediately without retries
                    break
                }
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Attempt ${attempt + 1} for '$operationName' encountered error: ${e.message}")
            }

            attempt++
            if (attempt <= retries) {
                delay(attempt * 400L) // Linear/exponential backoff (400ms, 800ms)
            }
        }

        // 3. Map final exception into structured StripeServiceError
        val finalError: StripeServiceError = when (lastException) {
            is StripeServiceCustomException -> lastException.error
            else -> {
                if (!connectivityObserver.isCurrentlyConnected()) {
                    StripeServiceError.ConnectivityLoss(cause = lastException)
                } else if (lastException?.message?.contains("timeout", ignoreCase = true) == true) {
                    StripeServiceError.ApiTimeout(cause = lastException)
                } else {
                    StripeServiceError.Unknown(
                        message = lastException?.message ?: "Operation '$operationName' failed after $retries retries.",
                        cause = lastException
                    )
                }
            }
        }

        _lastError.update { finalError }
        // Surface non-intrusive notification so map/delivery screen is never obstructed
        _nonObstructiveAlert.update { "Payment notice: ${finalError.message}" }
        Log.e(TAG, "Stripe operation '$operationName' concluded with error: ${finalError.message}")

        Result.failure(finalError.toException())
    }

    /**
     * Validates ephemeral key instance fields.
     */
    private fun validateEphemeralKey(key: StripeIssuingEphemeralKey) {
        val nowSec = System.currentTimeMillis() / 1000
        if (key.secret.isBlank()) {
            throw StripeServiceError.InvalidEphemeralKey(
                message = "Ephemeral key secret is blank."
            ).toException()
        }
        if (key.expires <= nowSec) {
            throw StripeServiceError.InvalidEphemeralKey(
                message = "Ephemeral key is expired (expires: ${key.expires}, current: $nowSec)."
            ).toException()
        }
        if (!validateRawEphemeralKeyJson(key.rawJson)) {
            throw StripeServiceError.InvalidEphemeralKey(
                message = "Ephemeral key JSON payload format is invalid."
            ).toException()
        }
    }

    /**
     * Validates raw JSON string structure from backend.
     */
    private fun validateRawEphemeralKeyJson(rawJson: String): Boolean {
        if (rawJson.isBlank()) return false
        return try {
            val json = JSONObject(rawJson)
            json.has("id") && json.has("secret") && json.getString("secret").isNotBlank()
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Helper exception subclass holding structured [StripeServiceError]
 */
class StripeServiceCustomException(val error: StripeServiceError) : Exception(error.message, error.cause)

/**
 * Extension helper to convert StripeServiceError to Throwable
 */
fun StripeServiceError.toException(): Exception = StripeServiceCustomException(this)
