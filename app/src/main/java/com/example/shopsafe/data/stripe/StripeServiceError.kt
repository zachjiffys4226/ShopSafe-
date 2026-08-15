package com.example.shopsafe.data.stripe

/**
 * Structured taxonomy of errors that can occur during Stripe SDK and backend API operations.
 */
sealed class StripeServiceError(
    open val message: String,
    open val cause: Throwable? = null,
    val isRecoverable: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Occurs when the backend endpoint or Stripe API exceeds the timeout threshold.
     */
    data class ApiTimeout(
        override val message: String = "Stripe API request timed out. Card operations will retry in background.",
        val timeoutMs: Long = 6000L,
        override val cause: Throwable? = null
    ) : StripeServiceError(message, cause, isRecoverable = true)

    /**
     * Occurs when network connectivity is lost or unavailable prior to/during API execution.
     */
    data class ConnectivityLoss(
        override val message: String = "No network connectivity. Active navigation is unaffected; card operations paused.",
        override val cause: Throwable? = null
    ) : StripeServiceError(message, cause, isRecoverable = true)

    /**
     * Occurs when backend returns malformed, empty, expired, or invalid ephemeral key responses.
     */
    data class InvalidEphemeralKey(
        override val message: String = "Invalid ephemeral key response from backend. Payload verification failed.",
        val rawResponseSnippet: String? = null,
        override val cause: Throwable? = null
    ) : StripeServiceError(message, cause, isRecoverable = false)

    /**
     * Occurs when the backend Stripe integration endpoint returns 5xx or server errors.
     */
    data class BackendUnavailable(
        val statusCode: Int = 503,
        override val message: String = "Stripe backend service is temporarily unavailable (HTTP $statusCode).",
        override val cause: Throwable? = null
    ) : StripeServiceError(message, cause, isRecoverable = true)

    /**
     * Unclassified exception during Stripe SDK operations.
     */
    data class Unknown(
        override val message: String,
        override val cause: Throwable? = null
    ) : StripeServiceError(message, cause, isRecoverable = false)
}
