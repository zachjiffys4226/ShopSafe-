package com.example.shopsafe.data.stripe

import com.example.shopsafe.data.models.StripeCardTokenizedDetails
import com.example.shopsafe.data.models.StripeIssuingCard
import com.example.shopsafe.data.models.StripeIssuingEphemeralKey
import com.example.shopsafe.data.models.StripePushProvisioningPayload
import com.stripe.android.EphemeralKeyProvider
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface contract for Stripe backend-mediated services.
 * Defines operations to fetch scoped ephemeral keys, tokenized card details,
 * and push provisioning payloads from secured backend endpoints without exposing
 * secret keys to the client.
 */
interface IStripeService : EphemeralKeyProvider {
    val serviceState: StateFlow<StripeServiceState>

    /**
     * Fetches an ephemeral key from the secure backend API for customer/payment session initialization.
     */
    suspend fun getEphemeralKey(
        customerId: String = "cus_default_driver",
        apiVersion: String = "2023-10-16"
    ): Result<StripeIssuingEphemeralKey>

    /**
     * Fetches a scoped ephemeral key from the backend API for a specific Issuing cardholder/card.
     */
    suspend fun getIssuingEphemeralKey(
        cardId: String,
        cardholderId: String
    ): Result<StripeIssuingEphemeralKey>

    /**
     * Fetches tokenized card details securely in-memory with automatic cleanup timers.
     */
    suspend fun fetchSecureTokenizedCardDetails(
        card: StripeIssuingCard
    ): Result<StripeCardTokenizedDetails>

    /**
     * Prepares encrypted OPCS payload for Google Pay push provisioning.
     */
    suspend fun pushProvisionToGooglePay(
        card: StripeIssuingCard
    ): Result<StripePushProvisioningPayload>

    /**
     * Purges any sensitive card details from client memory.
     */
    fun wipeInMemoryCardDetails()
}
