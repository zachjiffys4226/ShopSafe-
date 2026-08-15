package com.example.shopsafe.data.stripe

import android.util.Log
import com.example.shopsafe.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * ShopSafeStripeIssuingService handles secure communication with the ShopSafe
 * backend API for Stripe Issuing card management, dynamic spending limits,
 * real-time authorizations, and transaction reconciliation.
 *
 * CRITICAL SECURITY ARCHITECTURE:
 * SHOPSAFE APP -> SECURE SHOPSAFE BACKEND/API -> STRIPE ISSUING API
 *
 * No raw Stripe secret keys (sk_live_*, sk_test_*, rk_*) or webhook secrets (whsec_*)
 * are ever stored in or transmitted to the client application.
 */
class ShopSafeStripeIssuingService {

    companion object {
        private const val TAG = "ShopSafeStripeIssuing"
        private val DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.US)
        private val SHORT_DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    }

    // Program Configuration State
    private val _issuingConfig = MutableStateFlow(
        StripeIssuingConfig(
            environment = StripeEnvironmentMode.TEST,
            publishableKeyMasked = "pk_test_51OzShopSafeIssuingSandbox",
            restrictedKeyName = "rk_issuing_orders_readonly_server",
            webhookEndpointUrl = "https://api.shopsafe.app/v1/stripe/webhooks/issuing",
            isWebhookActive = true,
            webhookSecretMasked = "whsec_99a8b...verified",
            lastKeyRotationDate = "Aug 12, 2026, 09:30 AM",
            programName = "ShopSafe Courier Commercial Card Program",
            issuingCountry = "US",
            primaryCurrency = "USD",
            totalProgramFundingAvailable = 85400.00,
            activeCardsCount = 48,
            dailySpendLimitDefault = 1500.00
        )
    )
    val issuingConfig: StateFlow<StripeIssuingConfig> = _issuingConfig.asStateFlow()

    // Cardholders State
    private val _cardholders = MutableStateFlow<List<StripeCardholder>>(emptyList())
    val cardholders: StateFlow<List<StripeCardholder>> = _cardholders.asStateFlow()

    // Issuing Cards State
    private val _issuingCards = MutableStateFlow<List<StripeIssuingCard>>(emptyList())
    val issuingCards: StateFlow<List<StripeIssuingCard>> = _issuingCards.asStateFlow()

    // Real-Time Authorizations Log
    private val _authorizations = MutableStateFlow<List<StripeIssuingAuthorization>>(emptyList())
    val authorizations: StateFlow<List<StripeIssuingAuthorization>> = _authorizations.asStateFlow()

    // Settled Transactions
    private val _transactions = MutableStateFlow<List<StripeIssuingTransaction>>(emptyList())
    val transactions: StateFlow<List<StripeIssuingTransaction>> = _transactions.asStateFlow()

    // Connected Reconciliation Records (Order + Stripe + Photos + Totals)
    private val _reconciliationRecords = MutableStateFlow<List<IssuingReconciliationRecord>>(emptyList())
    val reconciliationRecords: StateFlow<List<IssuingReconciliationRecord>> = _reconciliationRecords.asStateFlow()

    // Administrative Audit Trail
    private val _auditLogs = MutableStateFlow<List<StripeIssuingAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<StripeIssuingAuditLog>> = _auditLogs.asStateFlow()

    // Webhook Events Health
    private val _webhookEvents = MutableStateFlow<List<StripeWebhookEventRecord>>(emptyList())
    val webhookEvents: StateFlow<List<StripeWebhookEventRecord>> = _webhookEvents.asStateFlow()

    init {
        seedInitialIssuingData()
    }

    /**
     * Seeds initial production simulation data
     */
    private fun seedInitialIssuingData() {
        val defaultCardholder = StripeCardholder(
            id = "ich_1Nzk4hDriverDavid",
            shopSafeUserId = "driver_1",
            driverName = "David Chen",
            email = "david.chen@driver.shopsafe.app",
            phoneNumber = "+1 (415) 890-4421",
            status = "ACTIVE",
            spendingLimitDaily = 850.00,
            spendingLimitPerOrder = 300.00,
            billingAddressFormatted = "1200 Market St, San Francisco, CA 94102",
            createdAt = "Aug 01, 2026",
            activeVirtualCardsCount = 1,
            activePhysicalCardsCount = 0,
            totalSettledSpend = 1684.20
        )

        val secondCardholder = StripeCardholder(
            id = "ich_1Ozp9mDriverElena",
            shopSafeUserId = "driver_2",
            driverName = "Elena Rostova",
            email = "elena.r@driver.shopsafe.app",
            phoneNumber = "+1 (415) 555-0198",
            status = "ACTIVE",
            spendingLimitDaily = 1200.00,
            spendingLimitPerOrder = 400.00,
            billingAddressFormatted = "450 Sutter St, San Francisco, CA 94108",
            createdAt = "Aug 03, 2026",
            activeVirtualCardsCount = 1,
            activePhysicalCardsCount = 1,
            totalSettledSpend = 3120.00
        )

        _cardholders.value = listOf(defaultCardholder, secondCardholder)

        val defaultDriverCard = StripeIssuingCard(
            id = "ic_1Nzk4hVisaVirtual4242",
            cardholderId = defaultCardholder.id,
            shopSafeUserId = "driver_1",
            driverName = "David Chen",
            last4 = "4242",
            expMonth = 12,
            expYear = 2029,
            brand = "Visa Commercial",
            type = StripeCardType.VIRTUAL,
            status = StripeCardStatus.ACTIVE,
            currency = "USD",
            currentOrderBoundId = "SS-ORD-9021",
            maxApprovedLimitForOrder = 95.00,
            activeSpendingAllowance = 95.00,
            allowedMerchantCategories = listOf("grocery_stores", "supermarkets", "convenience_stores", "restaurants", "pharmacies"),
            isFrozenByDriver = false,
            isSuspendedByAdmin = false,
            createdAt = "Aug 05, 2026"
        )

        val secondCard = StripeIssuingCard(
            id = "ic_1Ozp9mVisaVirtual8831",
            cardholderId = secondCardholder.id,
            shopSafeUserId = "driver_2",
            driverName = "Elena Rostova",
            last4 = "8831",
            expMonth = 10,
            expYear = 2028,
            brand = "Visa Commercial",
            type = StripeCardType.VIRTUAL,
            status = StripeCardStatus.ACTIVE,
            currency = "USD",
            currentOrderBoundId = null,
            maxApprovedLimitForOrder = 0.0,
            activeSpendingAllowance = 0.0,
            allowedMerchantCategories = listOf("grocery_stores", "supermarkets", "convenience_stores"),
            isFrozenByDriver = false,
            isSuspendedByAdmin = false,
            createdAt = "Aug 07, 2026"
        )

        _issuingCards.value = listOf(defaultDriverCard, secondCard)

        // Seed initial authorizations
        _authorizations.value = listOf(
            StripeIssuingAuthorization(
                id = "iauth_1Nzk4hSafewayApproved",
                cardId = defaultDriverCard.id,
                cardLast4 = "4242",
                cardholderName = "David Chen",
                orderId = "SS-ORD-8812",
                merchantName = "Safeway #1492 San Francisco",
                merchantCategory = "grocery_stores",
                amountRequested = 64.30,
                amountApproved = 64.30,
                status = StripeAuthStatus.APPROVED,
                timestampFormatted = "Aug 14, 2026, 03:22 PM",
                decisionDetails = "Approved via ShopSafe active order SS-ORD-8812 (Allowed: $75.00, Cushion: +$10.70)",
                cardholderId = defaultCardholder.id
            ),
            StripeIssuingAuthorization(
                id = "iauth_1Nzk4hTargetApproved",
                cardId = defaultDriverCard.id,
                cardLast4 = "4242",
                cardholderName = "David Chen",
                orderId = "SS-ORD-8799",
                merchantName = "Target Store #0321 SF",
                merchantCategory = "department_stores",
                amountRequested = 41.50,
                amountApproved = 41.50,
                status = StripeAuthStatus.APPROVED,
                timestampFormatted = "Aug 13, 2026, 11:15 AM",
                decisionDetails = "Approved via ShopSafe active order SS-ORD-8799",
                cardholderId = defaultCardholder.id
            ),
            StripeIssuingAuthorization(
                id = "iauth_1Nzk4hDeclinedExcessive",
                cardId = defaultDriverCard.id,
                cardLast4 = "4242",
                cardholderName = "David Chen",
                orderId = "SS-ORD-8720",
                merchantName = "Best Buy Electronics SF",
                merchantCategory = "electronics_stores",
                amountRequested = 499.00,
                amountApproved = 0.0,
                status = StripeAuthStatus.DECLINED,
                declineReason = "merchant_category_restricted",
                timestampFormatted = "Aug 11, 2026, 06:40 PM",
                decisionDetails = "Declined: Category 'electronics_stores' is not authorized for grocery/pharmacy courier run.",
                cardholderId = defaultCardholder.id
            )
        )

        // Seed reconciliation records
        _reconciliationRecords.value = listOf(
            IssuingReconciliationRecord(
                id = "rec_001_safeway",
                orderId = "SS-ORD-8812",
                driverId = "driver_1",
                driverName = "David Chen",
                cardholderId = defaultCardholder.id,
                cardId = defaultDriverCard.id,
                cardLast4 = "4242",
                stripeAuthId = "iauth_1Nzk4hSafewayApproved",
                stripeTxId = "ipi_8812_safeway",
                merchant = "Safeway #1492 SF",
                expectedOrderTotal = 62.50,
                amountAuthorized = 75.00,
                amountCaptured = 64.30,
                receiptScannedTotal = 64.30,
                purchaseDateTime = "Aug 14, 2026, 03:25 PM",
                registerPhotoUrl = "https://images.unsplash.com/photo-1556742049-0a67e557b6f6?w=600",
                originalReceiptPhotoUrl = "https://images.unsplash.com/photo-1556742049-0a67e557b6f6?w=600",
                status = IssuingReconciliationStatus.VERIFIED,
                discrepancyNotes = "Exact Match: Stripe $64.30 = Receipt $64.30 (Customer approved substitution +$1.80)",
                quickbooksSynced = true,
                quickbooksTxId = "QB-EXP-9402"
            ),
            IssuingReconciliationRecord(
                id = "rec_002_target",
                orderId = "SS-ORD-8799",
                driverId = "driver_1",
                driverName = "David Chen",
                cardholderId = defaultCardholder.id,
                cardId = defaultDriverCard.id,
                cardLast4 = "4242",
                stripeAuthId = "iauth_1Nzk4hTargetApproved",
                stripeTxId = "ipi_8799_target",
                merchant = "Target Store #0321 SF",
                expectedOrderTotal = 41.50,
                amountAuthorized = 50.00,
                amountCaptured = 41.50,
                receiptScannedTotal = 41.50,
                purchaseDateTime = "Aug 13, 2026, 11:20 AM",
                registerPhotoUrl = "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=600",
                originalReceiptPhotoUrl = "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=600",
                status = IssuingReconciliationStatus.VERIFIED,
                discrepancyNotes = "Exact Match: Stripe $41.50 = Receipt $41.50",
                quickbooksSynced = true,
                quickbooksTxId = "QB-EXP-9390"
            )
        )

        // Seed audit logs
        _auditLogs.value = listOf(
            StripeIssuingAuditLog(
                id = "aud_101",
                action = "CARD_SPENDING_LIMIT_ADJUSTED",
                resourceType = "SPENDING_CONTROL",
                resourceId = defaultDriverCard.id,
                timestampFormatted = "Aug 14, 2026, 03:10 PM",
                details = "Order SS-ORD-9021 spending limit updated dynamically to $95.00 (+12% substitution cushion)",
                ipAddressMasked = "10.0.4.***"
            ),
            StripeIssuingAuditLog(
                id = "aud_100",
                action = "CARDHOLDER_ENROLLED",
                resourceType = "CARDHOLDER",
                resourceId = defaultCardholder.id,
                timestampFormatted = "Aug 01, 2026, 10:00 AM",
                details = "Driver David Chen verified with KYC and issued commercial virtual purchasing card.",
                ipAddressMasked = "10.0.2.***"
            )
        )

        // Seed webhook health
        _webhookEvents.value = listOf(
            StripeWebhookEventRecord(
                id = "evt_issuing_auth_created_1",
                eventType = "issuing_authorization.request",
                createdFormatted = "Aug 14, 2026, 03:22 PM",
                isVerified = true,
                description = "Authorization request for $64.30 at Safeway #1492 - Approved",
                payloadPreview = "{\"id\": \"iauth_1Nzk4hSafewayApproved\", \"amount\": 6430, \"currency\": \"usd\", \"approved\": true}"
            ),
            StripeWebhookEventRecord(
                id = "evt_issuing_card_updated_1",
                eventType = "issuing_card.updated",
                createdFormatted = "Aug 14, 2026, 03:10 PM",
                isVerified = true,
                description = "Spending controls updated for card ic_...4242",
                payloadPreview = "{\"id\": \"ic_1Nzk4hVisaVirtual4242\", \"spending_controls\": {\"spending_limits\": [{\"amount\": 9500}]}}"
            )
        )
    }

    /**
     * Toggles between Stripe TEST mode and LIVE mode (Admin action)
     */
    suspend fun setEnvironmentMode(mode: StripeEnvironmentMode, adminName: String = "ShopSafe Admin") {
        withContext(Dispatchers.IO) {
            val updated = _issuingConfig.value.copy(
                environment = mode,
                publishableKeyMasked = if (mode == StripeEnvironmentMode.LIVE) "pk_live_51OzShopSafeIssuingProduction" else "pk_test_51OzShopSafeIssuingSandbox"
            )
            _issuingConfig.value = updated

            recordAuditLog(
                adminName = adminName,
                action = "ENVIRONMENT_SWITCHED",
                resourceType = "CONFIG",
                resourceId = mode.name,
                details = "Switched Stripe Issuing environment to ${mode.label} (${mode.badgeText})"
            )
        }
    }

    /**
     * Simulates rotation of the Stripe Issuing server credential
     */
    suspend fun rotateApiKey(adminName: String = "ShopSafe Admin"): Boolean {
        return withContext(Dispatchers.IO) {
            val now = DATE_FORMAT.format(Date())
            _issuingConfig.value = _issuingConfig.value.copy(
                lastKeyRotationDate = now
            )
            recordAuditLog(
                adminName = adminName,
                action = "API_KEY_ROTATED",
                resourceType = "CONFIG",
                resourceId = "rk_issuing_orders_readonly_server",
                details = "Rotated restricted Stripe Issuing server credentials safely without client app re-deployment."
            )
            true
        }
    }

    /**
     * Fetches the driver's active Stripe Issuing Virtual Card
     */
    fun getDriverCard(userId: String = "driver_1"): StripeIssuingCard? {
        return _issuingCards.value.find { it.shopSafeUserId == userId }
            ?: _issuingCards.value.firstOrNull()
    }

    /**
     * Dynamically adjusts the authorized spending allowance for an active order
     * with a reasonable buffer (e.g. +10-15% substitution cushion).
     */
    suspend fun authorizeOrderSpending(
        driverUserId: String,
        orderId: String,
        estimatedTotal: Double,
        cushionPercentage: Double = 0.15
    ): StripeIssuingCard? {
        return withContext(Dispatchers.IO) {
            val card = _issuingCards.value.find { it.shopSafeUserId == driverUserId } ?: return@withContext null
            val calculatedAllowance = Math.round((estimatedTotal * (1.0 + cushionPercentage)) * 100.0) / 100.0

            val updatedCard = card.copy(
                currentOrderBoundId = orderId,
                maxApprovedLimitForOrder = calculatedAllowance,
                activeSpendingAllowance = calculatedAllowance
            )

            _issuingCards.value = _issuingCards.value.map {
                if (it.id == card.id) updatedCard else it
            }

            recordAuditLog(
                adminName = "ShopSafe Automated Dispatch Engine",
                action = "SPENDING_LIMIT_ADJUSTED",
                resourceType = "SPENDING_CONTROL",
                resourceId = card.id,
                details = "Authorized $$calculatedAllowance for order $orderId (Estimated: $$estimatedTotal + ${cushionPercentage * 100}% cushion)."
            )

            updatedCard
        }
    }

    /**
     * Real-time Stripe Card Authorization Processor (Server-Side Decision Logic)
     */
    suspend fun processRealtimeAuthorization(
        cardId: String,
        orderId: String,
        merchantName: String,
        merchantCategory: String,
        requestedAmount: Double
    ): StripeIssuingAuthorization {
        return withContext(Dispatchers.IO) {
            val card = _issuingCards.value.find { it.id == cardId }
            val now = DATE_FORMAT.format(Date())

            val isCardActive = card?.status == StripeCardStatus.ACTIVE && !card.isFrozenByDriver && !card.isSuspendedByAdmin
            val isOrderMatching = card?.currentOrderBoundId == orderId || card?.currentOrderBoundId == null
            val isAmountWithinLimit = (card?.activeSpendingAllowance ?: 0.0) >= requestedAmount || (card?.maxApprovedLimitForOrder ?: 0.0) >= requestedAmount
            val isCategoryAllowed = card?.allowedMerchantCategories?.contains(merchantCategory) ?: true

            val isApproved = isCardActive && isAmountWithinLimit && isCategoryAllowed

            val declineReason = when {
                !isCardActive -> "card_inactive_or_frozen"
                !isCategoryAllowed -> "merchant_category_restricted"
                !isAmountWithinLimit -> "exceeds_order_authorized_spending_limit"
                else -> null
            }

            val decisionDetails = if (isApproved) {
                "Approved via ShopSafe active order $orderId (Requested: $$requestedAmount, Available: $${card?.activeSpendingAllowance ?: 0.0})"
            } else {
                "Declined: $declineReason (Requested $$requestedAmount at $merchantName)"
            }

            val authRecord = StripeIssuingAuthorization(
                cardId = cardId,
                cardLast4 = card?.last4 ?: "4242",
                cardholderName = card?.driverName ?: "ShopSafe Driver",
                orderId = orderId,
                merchantName = merchantName,
                merchantCategory = merchantCategory,
                amountRequested = requestedAmount,
                amountApproved = if (isApproved) requestedAmount else 0.0,
                status = if (isApproved) StripeAuthStatus.APPROVED else StripeAuthStatus.DECLINED,
                declineReason = declineReason,
                timestampFormatted = now,
                decisionDetails = decisionDetails,
                cardholderId = card?.cardholderId ?: "ich_default"
            )

            _authorizations.value = listOf(authRecord) + _authorizations.value

            // If approved, create settled transaction record
            if (isApproved) {
                val txRecord = StripeIssuingTransaction(
                    authorizationId = authRecord.id,
                    cardId = cardId,
                    cardLast4 = card?.last4 ?: "4242",
                    orderId = orderId,
                    driverId = card?.shopSafeUserId ?: "driver_1",
                    merchantName = merchantName,
                    amount = requestedAmount,
                    timestampFormatted = now,
                    receiptMatched = false,
                    reconciliationStatus = IssuingReconciliationStatus.PENDING_RECEIPT
                )
                _transactions.value = listOf(txRecord) + _transactions.value
            }

            authRecord
        }
    }

    /**
     * Connects Stripe Transaction + ShopSafe Order + Register Photo + Scanned Receipt Photo
     * into a unified reconciled record and checks for discrepancies.
     */
    suspend fun reconcileOrderPurchase(
        orderId: String,
        driverId: String,
        driverName: String,
        merchantName: String,
        expectedOrderTotal: Double,
        stripeCapturedAmount: Double,
        receiptScannedAmount: Double,
        registerPhotoUrl: String?,
        originalReceiptPhotoUrl: String?
    ): IssuingReconciliationRecord {
        return withContext(Dispatchers.IO) {
            val card = _issuingCards.value.find { it.shopSafeUserId == driverId }
            val now = DATE_FORMAT.format(Date())

            val discrepancyDiff = Math.abs(stripeCapturedAmount - receiptScannedAmount)
            val isExactMatch = discrepancyDiff < 0.05
            val isSlightVariance = discrepancyDiff <= 2.50 // Common tax / substitution rounding

            val status = when {
                isExactMatch -> IssuingReconciliationStatus.VERIFIED
                isSlightVariance -> IssuingReconciliationStatus.VERIFIED
                else -> IssuingReconciliationStatus.REVIEW_REQUIRED
            }

            val discrepancyNotes = when {
                isExactMatch -> "Verified Match: Stripe ($$stripeCapturedAmount) == Receipt ($$receiptScannedAmount)"
                isSlightVariance -> "Verified with Minor Variance (+$${String.format("%.2f", discrepancyDiff)}): Tax/Weight adjustment within allowable tolerance."
                else -> "Review Required: Stripe total ($$stripeCapturedAmount) differs from receipt total ($$receiptScannedAmount) by $${String.format("%.2f", discrepancyDiff)}. Flagged for automated review."
            }

            val reconciliationRecord = IssuingReconciliationRecord(
                orderId = orderId,
                driverId = driverId,
                driverName = driverName,
                cardholderId = card?.cardholderId ?: "ich_default",
                cardId = card?.id ?: "ic_default",
                cardLast4 = card?.last4 ?: "4242",
                stripeAuthId = "iauth_" + UUID.randomUUID().toString().take(8),
                stripeTxId = "ipi_" + UUID.randomUUID().toString().take(8),
                merchant = merchantName,
                expectedOrderTotal = expectedOrderTotal,
                amountAuthorized = card?.maxApprovedLimitForOrder ?: (expectedOrderTotal * 1.15),
                amountCaptured = stripeCapturedAmount,
                receiptScannedTotal = receiptScannedAmount,
                purchaseDateTime = now,
                registerPhotoUrl = registerPhotoUrl,
                originalReceiptPhotoUrl = originalReceiptPhotoUrl,
                status = status,
                discrepancyNotes = discrepancyNotes,
                quickbooksSynced = true,
                quickbooksTxId = "QB-EXP-" + (1000..9999).random()
            )

            // Update state
            _reconciliationRecords.value = listOf(reconciliationRecord) + _reconciliationRecords.value

            recordAuditLog(
                adminName = "ShopSafe Reconciliation Engine",
                action = "TRANSACTION_RECONCILED",
                resourceType = "RECONCILIATION",
                resourceId = reconciliationRecord.id,
                details = "Order $orderId reconciled. Status: ${status.name}. Notes: $discrepancyNotes"
            )

            reconciliationRecord
        }
    }

    /**
     * Freezes or unfreezes a card
     */
    suspend fun toggleCardFreeze(cardId: String, freeze: Boolean, reason: String? = null, triggeredBy: String = "David Chen"): StripeIssuingCard? {
        return withContext(Dispatchers.IO) {
            val card = _issuingCards.value.find { it.id == cardId } ?: return@withContext null
            val updated = card.copy(
                isFrozenByDriver = freeze,
                status = if (freeze) StripeCardStatus.FROZEN else StripeCardStatus.ACTIVE,
                freezeReason = if (freeze) reason ?: "Frozen by user" else null
            )
            _issuingCards.value = _issuingCards.value.map { if (it.id == cardId) updated else it }

            recordAuditLog(
                adminName = triggeredBy,
                action = if (freeze) "CARD_FROZEN" else "CARD_UNFROZEN",
                resourceType = "CARD",
                resourceId = cardId,
                details = if (freeze) "Card ic_...${card.last4} was frozen. Reason: $reason" else "Card ic_...${card.last4} was unfrozen and restored to active state."
            )

            updated
        }
    }

    /**
     * Reports a card lost or stolen
     */
    suspend fun reportCardLostStolen(cardId: String, adminOrDriverName: String = "David Chen"): StripeIssuingCard? {
        return withContext(Dispatchers.IO) {
            val card = _issuingCards.value.find { it.id == cardId } ?: return@withContext null
            val updated = card.copy(
                status = StripeCardStatus.LOST_STOLEN,
                isFrozenByDriver = true,
                freezeReason = "Reported lost or stolen"
            )
            _issuingCards.value = _issuingCards.value.map { if (it.id == cardId) updated else it }

            recordAuditLog(
                adminName = adminOrDriverName,
                action = "CARD_REPORTED_LOST_STOLEN",
                resourceType = "CARD",
                resourceId = cardId,
                details = "Card ic_...${card.last4} permanently deactivated due to lost/stolen report."
            )

            updated
        }
    }

    /**
     * Issues a replacement card
     */
    suspend fun issueReplacementCard(oldCardId: String, adminName: String = "ShopSafe Admin"): StripeIssuingCard? {
        return withContext(Dispatchers.IO) {
            val oldCard = _issuingCards.value.find { it.id == oldCardId } ?: return@withContext null
            val newLast4 = (1000..9999).random().toString()
            val newCard = oldCard.copy(
                id = "ic_" + UUID.randomUUID().toString().take(14),
                last4 = newLast4,
                status = StripeCardStatus.ACTIVE,
                isFrozenByDriver = false,
                isSuspendedByAdmin = false,
                freezeReason = null,
                createdAt = SHORT_DATE_FORMAT.format(Date())
            )

            _issuingCards.value = _issuingCards.value.map { if (it.id == oldCardId) it.copy(status = StripeCardStatus.CANCELED) else it } + newCard

            recordAuditLog(
                adminName = adminName,
                action = "CARD_REPLACED",
                resourceType = "CARD",
                resourceId = newCard.id,
                details = "Canceled old card ic_...${oldCard.last4} and issued replacement virtual card ic_...$newLast4 to ${newCard.driverName}."
            )

            newCard
        }
    }

    /**
     * Updates card spending limits manually from Admin
     */
    suspend fun updateCardSpendingLimits(
        cardId: String,
        newDailyLimit: Double,
        adminName: String = "ShopSafe Admin"
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val card = _issuingCards.value.find { it.id == cardId } ?: return@withContext false
            val cardholder = _cardholders.value.find { it.id == card.cardholderId }

            if (cardholder != null) {
                _cardholders.value = _cardholders.value.map {
                    if (it.id == cardholder.id) it.copy(spendingLimitDaily = newDailyLimit) else it
                }
            }

            recordAuditLog(
                adminName = adminName,
                action = "SPENDING_LIMIT_OVERRIDDEN",
                resourceType = "SPENDING_CONTROL",
                resourceId = cardId,
                details = "Admin set daily spending limit to $$newDailyLimit for ${card.driverName}."
            )
            true
        }
    }

    /**
     * Triggers a simulated incoming Stripe webhook test
     */
    suspend fun simulateWebhookTest(): StripeWebhookEventRecord {
        return withContext(Dispatchers.IO) {
            val now = DATE_FORMAT.format(Date())
            val event = StripeWebhookEventRecord(
                id = "evt_test_" + UUID.randomUUID().toString().take(8),
                eventType = "issuing_authorization.created",
                createdFormatted = now,
                isVerified = true,
                description = "Simulated webhook test verified with signing secret (${_issuingConfig.value.webhookSecretMasked})",
                payloadPreview = "{\"type\": \"issuing_authorization.created\", \"livemode\": ${_issuingConfig.value.environment == StripeEnvironmentMode.LIVE}, \"created\": ${System.currentTimeMillis() / 1000}}"
            )
            _webhookEvents.value = listOf(event) + _webhookEvents.value
            event
        }
    }

    /**
     * Helper to append to the immutable audit trail
     */
    private fun recordAuditLog(
        adminName: String,
        action: String,
        resourceType: String,
        resourceId: String,
        details: String
    ) {
        val now = DATE_FORMAT.format(Date())
        val log = StripeIssuingAuditLog(
            adminName = adminName,
            adminEmail = if (adminName.contains("David", ignoreCase = true)) "david.chen@driver.shopsafe.app" else "admin@shopsafe.app",
            action = action,
            resourceType = resourceType,
            resourceId = resourceId,
            timestampFormatted = now,
            details = details
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }
}
