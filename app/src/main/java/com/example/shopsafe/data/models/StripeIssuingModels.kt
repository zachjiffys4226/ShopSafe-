package com.example.shopsafe.data.models

import androidx.compose.ui.graphics.Color
import java.util.UUID

/**
 * Stripe Environment Mode indicator
 */
enum class StripeEnvironmentMode(val label: String, val badgeText: String, val color: Long) {
    TEST("Test Mode", "STRIPE MODE: TEST", 0xFFF59E0B),
    LIVE("Live Mode", "STRIPE MODE: LIVE", 0xFF10B981)
}

/**
 * Stripe Issuing Card Type
 */
enum class StripeCardType(val label: String) {
    VIRTUAL("Virtual Card"),
    PHYSICAL("Physical Card")
}

/**
 * Stripe Issuing Card Status
 */
enum class StripeCardStatus(val label: String, val color: Long) {
    ACTIVE("Active", 0xFF10B981),
    INACTIVE("Inactive", 0xFF94A3B8),
    FROZEN("Frozen", 0xFF38BDF8),
    CANCELED("Canceled", 0xFFEF4444),
    LOST_STOLEN("Reported Lost", 0xFFF43F5E)
}

/**
 * Stripe Issuing Authorization Status
 */
enum class StripeAuthStatus(val label: String, val color: Long) {
    APPROVED("Approved", 0xFF10B981),
    DECLINED("Declined", 0xFFEF4444),
    PENDING("Pending", 0xFFF59E0B),
    REVERSED("Reversed", 0xFF8B5CF6)
}

/**
 * Stripe Issuing Reconciliation Status
 */
enum class IssuingReconciliationStatus(val label: String, val color: Long) {
    VERIFIED("Verified", 0xFF10B981),
    REVIEW_REQUIRED("Review Required", 0xFFF59E0B),
    PENDING_RECEIPT("Pending Receipt", 0xFF38BDF8),
    DISCREPANCY_FLAGGED("Discrepancy Flagged", 0xFFEF4444)
}

/**
 * ShopSafe Stripe Program Configuration
 */
data class StripeIssuingConfig(
    val environment: StripeEnvironmentMode = StripeEnvironmentMode.TEST,
    val publishableKeyMasked: String = "pk_test_51Oz...ShopSafeIssuing",
    val restrictedKeyName: String = "rk_issuing_orders_readonly_server",
    val webhookEndpointUrl: String = "https://api.shopsafe.app/v1/stripe/webhooks/issuing",
    val isWebhookActive: Boolean = true,
    val webhookSecretMasked: String = "whsec_99a8b...verified",
    val lastKeyRotationDate: String = "Aug 12, 2026, 09:30 AM",
    val programName: String = "ShopSafe Courier Commercial Card Program",
    val issuingCountry: String = "US",
    val primaryCurrency: String = "USD",
    val totalProgramFundingAvailable: Double = 85400.00,
    val activeCardsCount: Int = 48,
    val dailySpendLimitDefault: Double = 1500.00
)

/**
 * Stripe Cardholder Model
 */
data class StripeCardholder(
    val id: String = "ich_" + UUID.randomUUID().toString().take(14),
    val shopSafeUserId: String,
    val driverName: String,
    val email: String,
    val phoneNumber: String,
    val status: String = "ACTIVE", // ACTIVE, INACTIVE, BLOCKED
    val spendingLimitDaily: Double = 500.00,
    val spendingLimitPerOrder: Double = 250.00,
    val billingAddressFormatted: String = "1200 Market St, San Francisco, CA 94102",
    val createdAt: String = "2026-08-01",
    val activeVirtualCardsCount: Int = 1,
    val activePhysicalCardsCount: Int = 0,
    val totalSettledSpend: Double = 1420.50
)

/**
 * Stripe Issuing Card Model
 */
data class StripeIssuingCard(
    val id: String = "ic_" + UUID.randomUUID().toString().take(14),
    val cardholderId: String,
    val shopSafeUserId: String,
    val driverName: String,
    val last4: String = "4242",
    val expMonth: Int = 12,
    val expYear: Int = 2029,
    val brand: String = "Visa Commercial",
    val type: StripeCardType = StripeCardType.VIRTUAL,
    val status: StripeCardStatus = StripeCardStatus.ACTIVE,
    val currency: String = "USD",
    val currentOrderBoundId: String? = null,
    val maxApprovedLimitForOrder: Double = 0.0,
    val activeSpendingAllowance: Double = 0.0,
    val allowedMerchantCategories: List<String> = listOf("grocery_stores", "supermarkets", "convenience_stores", "restaurants", "pharmacies"),
    val isFrozenByDriver: Boolean = false,
    val isSuspendedByAdmin: Boolean = false,
    val freezeReason: String? = null,
    val digitalWalletEligible: Boolean = true,
    val createdAt: String = "Aug 05, 2026"
)

/**
 * Real-Time Stripe Authorization Log Record
 */
data class StripeIssuingAuthorization(
    val id: String = "iauth_" + UUID.randomUUID().toString().take(14),
    val cardId: String,
    val cardLast4: String = "4242",
    val cardholderName: String,
    val orderId: String,
    val merchantName: String,
    val merchantCategory: String,
    val amountRequested: Double,
    val amountApproved: Double,
    val status: StripeAuthStatus,
    val declineReason: String? = null,
    val timestampFormatted: String,
    val decisionDetails: String,
    val cardholderId: String
)

/**
 * Stripe Settled Transaction
 */
data class StripeIssuingTransaction(
    val id: String = "ipi_" + UUID.randomUUID().toString().take(14),
    val authorizationId: String,
    val cardId: String,
    val cardLast4: String,
    val orderId: String,
    val driverId: String,
    val merchantName: String,
    val amount: Double,
    val currency: String = "USD",
    val timestampFormatted: String,
    val receiptMatched: Boolean = false,
    val reconciliationStatus: IssuingReconciliationStatus = IssuingReconciliationStatus.PENDING_RECEIPT
)

/**
 * Complete Connected Reconciliation Record
 * Connecting: Stripe Transaction + ShopSafe Order + Register Photo + Receipt Photo + Discrepancy Checks
 */
data class IssuingReconciliationRecord(
    val id: String = "rec_" + UUID.randomUUID().toString().take(12),
    val orderId: String,
    val driverId: String,
    val driverName: String,
    val cardholderId: String,
    val cardId: String,
    val cardLast4: String = "4242",
    val stripeAuthId: String,
    val stripeTxId: String,
    val merchant: String,
    val expectedOrderTotal: Double,
    val amountAuthorized: Double,
    val amountCaptured: Double,
    val receiptScannedTotal: Double,
    val purchaseDateTime: String,
    val registerPhotoUrl: String? = null,
    val originalReceiptPhotoUrl: String? = null,
    val status: IssuingReconciliationStatus,
    val discrepancyNotes: String? = null,
    val quickbooksSynced: Boolean = false,
    val quickbooksTxId: String? = null
)

/**
 * Administrative Audit Trail Log
 */
data class StripeIssuingAuditLog(
    val id: String = "aud_" + UUID.randomUUID().toString().take(10),
    val adminName: String = "ShopSafe Admin",
    val adminEmail: String = "admin@shopsafe.app",
    val action: String,
    val resourceType: String, // CARD, CARDHOLDER, SPENDING_CONTROL, CONFIG, RECONCILIATION
    val resourceId: String,
    val timestampFormatted: String,
    val details: String,
    val ipAddressMasked: String = "192.168.1.***"
)

/**
 * Webhook Health and Event Model
 */
data class StripeWebhookEventRecord(
    val id: String = "evt_" + UUID.randomUUID().toString().take(14),
    val eventType: String,
    val createdFormatted: String,
    val isVerified: Boolean = true,
    val description: String,
    val payloadPreview: String
)

/**
 * Ephemeral Key Model for Secure Tokenized Communication with Stripe Issuing
 */
data class StripeIssuingEphemeralKey(
    val id: String = "ephkey_" + UUID.randomUUID().toString().take(14),
    val cardId: String,
    val cardholderId: String,
    val secret: String,
    val created: Long = System.currentTimeMillis() / 1000,
    val expires: Long = (System.currentTimeMillis() / 1000) + 300, // 5 minutes TTL
    val rawJson: String = "{}"
)

/**
 * Decrypted in-memory card representation securely obtained via single-use backend tokenization
 */
data class StripeCardTokenizedDetails(
    val cardId: String,
    val fullPan: String,
    val cvc: String,
    val expMonth: Int,
    val expYear: Int,
    val brand: String = "Visa Commercial",
    val tokenizedSessionId: String = "issuing_sess_" + UUID.randomUUID().toString().take(12),
    val retrievedAt: Long = System.currentTimeMillis(),
    val expiresAtTimestamp: Long = System.currentTimeMillis() + 15_000 // 15 seconds ephemeral in-memory retention
)

/**
 * Digital Wallet / Google Pay Push Provisioning Payload
 */
data class StripePushProvisioningPayload(
    val cardId: String,
    val cardholderName: String,
    val primaryAccountNumberSuffix: String,
    val pushToken: String = "pushtok_" + UUID.randomUUID().toString().take(16),
    val ephemeralKeySecret: String,
    val opcsBlob: String,
    val isGooglePayReady: Boolean = true,
    val provisionedTimestamp: Long = System.currentTimeMillis()
)

/**
 * UI State for Stripe Issuing Tokenization
 */
data class StripeTokenizationState(
    val isTokenizing: Boolean = false,
    val activeEphemeralKey: StripeIssuingEphemeralKey? = null,
    val tokenizedDetails: StripeCardTokenizedDetails? = null,
    val pushProvisioningPayload: StripePushProvisioningPayload? = null,
    val isPushProvisioned: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
