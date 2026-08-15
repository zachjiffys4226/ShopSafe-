package com.example.shopsafe.data.models

import java.util.UUID

/**
 * Status of an individual item on the customer's shopping list.
 */
enum class ShopperItemStatus(val label: String, val badgeColor: Long) {
    PENDING("To Collect", 0xFF64748B),
    COLLECTED("Collected", 0xFF10B981),
    SUBSTITUTED("Substituted", 0xFF0284C7),
    UNAVAILABLE("Out of Stock", 0xFFDC2626)
}

/**
 * Scan method used by the shopper to verify the item.
 */
enum class ItemScanMethod {
    BARCODE,
    QR_CODE,
    AI_PHOTO_RECOGNITION,
    MANUAL_PHOTO,
    MANUAL_COUNTER
}

/**
 * Detailed order item with barcode, pricing, substitutions, and one-handed scan state.
 */
data class ShopperOrderItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val brandOrCategory: String = "Grocery Essentials",
    val requestedQuantity: Int = 1,
    val collectedQuantity: Int = 0,
    val expectedPrice: Double,
    val actualPrice: Double = expectedPrice,
    val unit: String = "ea",
    val aisle: String = "Aisle 3",
    val barcode: String = "012345678905",
    val qrCode: String = "SS-QR-${UUID.randomUUID().toString().take(8)}",
    val imageUrl: String = "",
    val status: ShopperItemStatus = ShopperItemStatus.PENDING,
    val substitutionName: String? = null,
    val substitutionPrice: Double? = null,
    val unavailableReason: String? = null,
    val scanMethod: ItemScanMethod? = null,
    val itemPhotoUri: String? = null,
    val barcodeConfidence: Float = 1.0f
) {
    val isFullyCollected: Boolean
        get() = status == ShopperItemStatus.COLLECTED && collectedQuantity >= requestedQuantity

    val isHandled: Boolean
        get() = status != ShopperItemStatus.PENDING
}

/**
 * Store Checkout Verification State & Model
 */
data class StoreCheckoutVerification(
    val orderId: String,
    val storeName: String,
    val expectedSubtotal: Double,
    val actualRegisterTotal: Double,
    val priceAdjustmentAmount: Double = 0.0,
    val adjustmentReason: String = "",
    val registerPhotoUrl: String = "",
    val receiptPhotoUrl: String = "",
    val isReceiptReadable: Boolean = true,
    val receiptOcrStoreMatch: Boolean = true,
    val isDiscrepancyFlagged: Boolean = false,
    val discrepancyNote: String? = null,
    val verificationTimestamp: Long = System.currentTimeMillis()
)
