package com.example.shopsafe.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class ItemCondition {
    NEW,
    USED_LIKE_NEW,
    USED_GOOD,
    USED_FAIR
}

enum class ItemCategory {
    ALL,
    ELECTRONICS,
    VEHICLES,
    APPAREL,
    HOME_GARDEN,
    SPORTING_GOODS,
    NEW_ITEMS,
    FAST_FOOD,
    GROCERY,
    CONVENIENCE
}

enum class MarketplaceSortOption {
    NEWEST,
    PRICE_LOW,
    PRICE_HIGH,
    DISTANCE
}

@Entity(tableName = "marketplace_items")
data class MarketplaceItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val condition: String,
    val pickupLocation: String, // Pickup location required
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val imageUrl: String = "",
    val sellerName: String,
    val sellerRating: Double = 4.9,
    val sellerPhone: String = "(555) 234-5678",
    val isFacebookImported: Boolean = false,
    val isNewItem: Boolean = false,
    val distanceMiles: Double = 1.2,
    val timestamp: Long = System.currentTimeMillis(),
    val itemDimensions: String = "12x12x12 in",
    val itemWeightLbs: Double = 5.0,
    val requiredVehicleType: String = "Sedan",
    val isHeavy: Boolean = false,
    val additionalImages: String = "", // Comma-separated list of unlimited photo uploads
    val videoUrl: String = "" // Optional video attachment URL
)

@Entity(tableName = "storefronts")
data class Storefront(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String, // Fast Food, Grocery, Convenience, ShopSafe Local Storefront, Pharmacy
    val rating: Double = 4.9,
    val deliveryFee: Double = 1.99,
    val estimatedMins: Int = 18,
    val distanceMiles: Double = 1.2,
    val bannerDrawableName: String = "img_shopsafe_hero_1786335470767",
    val address: String,
    val isShopSafeEmployeeHub: Boolean = false, // True for local storefront where ShopSafe employees shop
    val isOpen: Boolean = true,
    val phoneNumber: String = "(800) 746-7726",
    val taxRate: Double = 0.0825,
    val cityState: String = "San Francisco, CA"
)

val Storefront.isFastFood: Boolean
    get() = category.contains("Fast Food", ignoreCase = true) || category.contains("Food", ignoreCase = true) || category.contains("Burger", ignoreCase = true)

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val storeId: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String = "",
    val isPopular: Boolean = false,
    val aisle: String = "Aisle 1",
    val inStock: Boolean = true,
    val stockQuantity: Int = 10,
    val taxAmount: Double = 0.0,
    val substituteOptions: String = "",
    val additionalImages: String = "", // Comma-separated list of unlimited photo uploads
    val videoUrl: String = "" // Optional video attachment URL
)

data class CartItem(
    val id: String = UUID.randomUUID().toString(),
    val storeId: String,
    val storeName: String,
    val itemId: String,
    val name: String,
    val price: Double,
    val quantity: Int = 1,
    val specialInstructions: String = "",
    val isMarketplaceItem: Boolean = false,
    val pickupLocation: String = ""
)

enum class OrderStatus {
    PLACED,
    SHOPPING_OR_PREPARING,
    DRIVER_ASSIGNED,
    ON_THE_WAY,
    APPROACHING,
    DELIVERED
}

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val storeOrSellerName: String,
    val itemsSummary: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val serviceFee: Double,
    val tip: Double,
    val total: Double,
    val status: String = OrderStatus.PLACED.name,
    val pickupAddress: String,
    val dropoffAddress: String,
    val customerName: String = "You",
    val driverName: String = "Marcus Vance (ShopSafe Courier)",
    val driverPhone: String = "(555) 890-1234",
    val driverRating: Double = 4.95,
    val driverLatitude: Double = 37.7790,
    val driverLongitude: Double = -122.4150,
    val estimatedDeliveryMins: Int = 22,
    val timestamp: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Credit Card (•••• 4242)",
    val driverVehicleMake: String = "Toyota",
    val driverVehicleModel: String = "Camry",
    val driverVehicleYear: String = "2022",
    val driverVehicleColor: String = "Midnight Blue",
    val driverLicensePlate: String = "7XYZ89",
    val driverVehicleType: String = "Sedan",
    val driverIsCheckrVerified: Boolean = true,
    val driverSelfiePhotoUrl: String = "",
    val customerRating: Int = 0,
    val customerFeedback: String = "",
    val hasBeenReviewed: Boolean = false,
    val deliveryInstructions: String = ""
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "storeOrSellerName" to storeOrSellerName,
        "itemsSummary" to itemsSummary,
        "subtotal" to subtotal,
        "deliveryFee" to deliveryFee,
        "serviceFee" to serviceFee,
        "tip" to tip,
        "total" to total,
        "status" to status,
        "pickupAddress" to pickupAddress,
        "dropoffAddress" to dropoffAddress,
        "customerName" to customerName,
        "driverName" to driverName,
        "driverPhone" to driverPhone,
        "driverRating" to driverRating,
        "driverLatitude" to driverLatitude,
        "driverLongitude" to driverLongitude,
        "estimatedDeliveryMins" to estimatedDeliveryMins,
        "timestamp" to timestamp,
        "paymentMethod" to paymentMethod,
        "driverVehicleMake" to driverVehicleMake,
        "driverVehicleModel" to driverVehicleModel,
        "driverVehicleYear" to driverVehicleYear,
        "driverVehicleColor" to driverVehicleColor,
        "driverLicensePlate" to driverLicensePlate,
        "driverVehicleType" to driverVehicleType,
        "driverIsCheckrVerified" to driverIsCheckrVerified,
        "driverSelfiePhotoUrl" to driverSelfiePhotoUrl,
        "customerRating" to customerRating,
        "customerFeedback" to customerFeedback,
        "hasBeenReviewed" to hasBeenReviewed,
        "deliveryInstructions" to deliveryInstructions
    )

    companion object {
        fun fromFirestoreMap(map: Map<String, Any>): Order {
            return Order(
                id = map["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                storeOrSellerName = map["storeOrSellerName"] as? String ?: "Store",
                itemsSummary = map["itemsSummary"] as? String ?: "",
                subtotal = (map["subtotal"] as? Number)?.toDouble() ?: 0.0,
                deliveryFee = (map["deliveryFee"] as? Number)?.toDouble() ?: 0.0,
                serviceFee = (map["serviceFee"] as? Number)?.toDouble() ?: 0.0,
                tip = (map["tip"] as? Number)?.toDouble() ?: 0.0,
                total = (map["total"] as? Number)?.toDouble() ?: 0.0,
                status = map["status"] as? String ?: OrderStatus.PLACED.name,
                pickupAddress = map["pickupAddress"] as? String ?: "",
                dropoffAddress = map["dropoffAddress"] as? String ?: "",
                customerName = map["customerName"] as? String ?: "You",
                driverName = map["driverName"] as? String ?: "Marcus Vance",
                driverPhone = map["driverPhone"] as? String ?: "(555) 890-1234",
                driverRating = (map["driverRating"] as? Number)?.toDouble() ?: 4.95,
                driverLatitude = (map["driverLatitude"] as? Number)?.toDouble() ?: 37.7790,
                driverLongitude = (map["driverLongitude"] as? Number)?.toDouble() ?: -122.4150,
                estimatedDeliveryMins = (map["estimatedDeliveryMins"] as? Number)?.toInt() ?: 20,
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                paymentMethod = map["paymentMethod"] as? String ?: "Credit Card",
                driverVehicleMake = map["driverVehicleMake"] as? String ?: "Toyota",
                driverVehicleModel = map["driverVehicleModel"] as? String ?: "Camry",
                driverVehicleYear = map["driverVehicleYear"] as? String ?: "2022",
                driverVehicleColor = map["driverVehicleColor"] as? String ?: "Midnight Blue",
                driverLicensePlate = map["driverLicensePlate"] as? String ?: "7XYZ89",
                driverVehicleType = map["driverVehicleType"] as? String ?: "Sedan",
                driverIsCheckrVerified = map["driverIsCheckrVerified"] as? Boolean ?: true,
                driverSelfiePhotoUrl = map["driverSelfiePhotoUrl"] as? String ?: "",
                customerRating = (map["customerRating"] as? Number)?.toInt() ?: 0,
                customerFeedback = map["customerFeedback"] as? String ?: "",
                hasBeenReviewed = map["hasBeenReviewed"] as? Boolean ?: false,
                deliveryInstructions = map["deliveryInstructions"] as? String ?: ""
            )
        }
    }
}

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val threadId: String,
    val senderName: String,
    val senderRole: String, // Buyer, Seller, Driver, Support
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val locationAddress: String? = null
)

@Entity(tableName = "chat_threads")
data class ChatThread(
    @PrimaryKey val threadId: String = UUID.randomUUID().toString(),
    val title: String,
    val partnerName: String,
    val partnerPhone: String,
    val lastMessage: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val relatedItemId: String = "",
    val isDriverThread: Boolean = false
)

@Entity(tableName = "driver_profile")
data class DriverProfile(
    @PrimaryKey val id: String = "current_driver",
    val name: String = "Alex Rivera",
    val email: String = "alex.rivera@example.com",
    val phone: String = "(555) 345-6789",
    val isOnline: Boolean = true,
    val currentBalance: Double = 0.0,
    val balanceStatus: String = "AVAILABLE", // AVAILABLE, PENDING, PROCESSING, PAID
    val pendingTransferAmount: Double = 0.0,
    val pendingTransferTimestamp: Long = 0L,
    val todayEarned: Double = 0.0,
    val weekEarned: Double = 0.0,
    val lifetimeEarned: Double = 1420.50,
    val completedToday: Int = 0,
    val totalCompletedDeliveries: Int = 54,
    val rating: Double = 4.98,
    val preferredPayoutMethod: String = "Instant Cashout (Debit Card)",
    val activeOrderId: String? = null,
    val authProvider: String = "FACEBOOK", // EMAIL, FACEBOOK, GOOGLE, APPLE
    val isTwoFactorEnabled: Boolean = true,
    // Stripe Connect Integration Details
    val stripeConnectAccountId: String = "acct_1Nzk4hShopSafeDriver",
    val stripeConnectStatus: String = "ACTIVE", // ACTIVE, PENDING_VERIFICATION, RESTRICTED
    val stripePayoutSchedule: String = "Instant & Daily Automated",
    // Checkr Background Check
    val checkrStatus: String = "APPROVED", // APPROVED, PENDING, PROCESSING
    val checkrSsnLast4: String = "4321",
    val checkrDatePassed: String = "Aug 2026",
    // Auto Insurance
    val insuranceVerified: Boolean = true,
    val insuranceProvider: String = "State Farm Insurance",
    val insurancePolicyNum: String = "POL-9876543",
    val insuranceExpiry: String = "12/2027",
    // Driver's License
    val driversLicenseVerified: Boolean = true,
    val licenseState: String = "CA",
    val licenseNumber: String = "D7891023",
    val licenseExpiry: String = "05/2028",
    // In-person Selfie
    val selfieVerified: Boolean = true,
    val selfiePhotoUrl: String = "",
    // Full Vehicle Details
    val vehicleMake: String = "Toyota",
    val vehicleModel: String = "Camry",
    val vehicleYear: String = "2022",
    val vehicleColor: String = "Midnight Blue",
    val licensePlate: String = "7XYZ89",
    val vehicleType: String = "Sedan",
    // Banking Details for Weekly Direct Deposit
    val bankName: String = "Chase Bank",
    val bankRoutingNumber: String = "121000358",
    val bankAccountNumber: String = "•••• 8821",
    // Debit Card Details for Instant Transfers
    val debitCardNumber: String = "•••• •••• •••• 4242",
    val debitCardExpiry: String = "08/28",
    val debitCardCvv: String = "•••"
)

data class AuthUser(
    val id: String = "driver_101",
    val name: String = "Alex Rivera",
    val email: String = "alex.rivera@example.com",
    val phone: String = "(555) 345-6789",
    val authProvider: String = "FACEBOOK",
    val isDriver: Boolean = true,
    val isAdmin: Boolean = false,
    val isTwoFactorEnabled: Boolean = true,
    val checkrStatus: String = "APPROVED",
    val insuranceVerified: Boolean = true,
    val driversLicenseVerified: Boolean = true,
    val selfieVerified: Boolean = true,
    val vehicleMake: String = "Toyota",
    val vehicleModel: String = "Camry",
    val vehicleYear: String = "2022",
    val vehicleColor: String = "Midnight Blue",
    val licensePlate: String = "7XYZ89",
    val vehicleType: String = "Sedan",
    val bankRoutingNumber: String = "121000358",
    val bankAccountNumber: String = "•••• 8821",
    val debitCardNumber: String = "•••• •••• •••• 4242"
)

@Entity(tableName = "driver_offers")
data class DriverOffer(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val storeName: String,
    val customerName: String = "Sarah Jenkins",
    val pickupAddress: String,
    val dropoffAddress: String,
    val payAmount: Double, // Base pay + 100% tip + surge
    val basePay: Double = payAmount * 0.70,
    val tipAmount: Double = payAmount * 0.30,
    val bonusAmount: Double = 0.0,
    val distanceMiles: Double,
    val estimatedMins: Int,
    val itemDetails: String,
    val isShopSafeStorefrontShopping: Boolean = false, // Employee shopping task
    val orderId: String = "",
    val deliveryInstructions: String = "",
    val surgeMultiplier: Double = 1.0,
    val surgeBonusAmount: Double = 0.0,
    val surgeZoneName: String = "",
    val pickupLat: Double = 37.7749,
    val pickupLng: Double = -122.4194,
    val dropoffLat: Double = 37.7885,
    val dropoffLng: Double = -122.3995,
    val offerExpiresInSeconds: Int = 45,
    val offerReceivedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "payouts")
data class PayoutTransaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val driverId: String = "driver_101",
    val amount: Double = 0.0,
    val method: String = "Instant Cashout (Debit Card)", // Instant Cashout, Direct Deposit, ShopSafe Safe Card
    val fee: Double = 0.0,
    val netPayoutAmount: Double = amount - fee,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // PENDING, PROCESSING, COMPLETED, FAILED
    val stripeTransferId: String = "tr_${UUID.randomUUID().toString().take(16)}",
    val stripePayoutId: String = "po_${UUID.randomUUID().toString().take(16)}",
    val stripeConnectAccountId: String = "acct_1Nzk4hShopSafeDriver",
    val destinationAccount: String = "Debit Card (•••• 4242)",
    val currency: String = "usd",
    val estimatedArrival: String = "Instant (~1-2 mins)",
    val stripeStatus: String = "paid",
    val failureReason: String? = null
) {
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "driverId" to driverId,
            "amount" to amount,
            "method" to method,
            "fee" to fee,
            "netPayoutAmount" to netPayoutAmount,
            "timestamp" to timestamp,
            "status" to status,
            "stripeTransferId" to stripeTransferId,
            "stripePayoutId" to stripePayoutId,
            "stripeConnectAccountId" to stripeConnectAccountId,
            "destinationAccount" to destinationAccount,
            "currency" to currency,
            "estimatedArrival" to estimatedArrival,
            "stripeStatus" to stripeStatus,
            "failureReason" to failureReason
        )
    }

    companion object {
        fun fromFirestoreMap(map: Map<String, Any?>): PayoutTransaction {
            val amt = (map["amount"] as? Number)?.toDouble() ?: 0.0
            val feeVal = (map["fee"] as? Number)?.toDouble() ?: 0.0
            val net = (map["netPayoutAmount"] as? Number)?.toDouble() ?: (amt - feeVal)
            return PayoutTransaction(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                driverId = map["driverId"] as? String ?: "driver_101",
                amount = amt,
                method = map["method"] as? String ?: "Instant Cashout (Debit Card)",
                fee = feeVal,
                netPayoutAmount = net,
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                status = map["status"] as? String ?: "COMPLETED",
                stripeTransferId = map["stripeTransferId"] as? String ?: "",
                stripePayoutId = map["stripePayoutId"] as? String ?: "",
                stripeConnectAccountId = map["stripeConnectAccountId"] as? String ?: "acct_1Nzk4hShopSafeDriver",
                destinationAccount = map["destinationAccount"] as? String ?: "Debit Card (•••• 4242)",
                currency = map["currency"] as? String ?: "usd",
                estimatedArrival = map["estimatedArrival"] as? String ?: "Instant (~1-2 mins)",
                stripeStatus = map["stripeStatus"] as? String ?: "paid",
                failureReason = map["failureReason"] as? String
            )
        }
    }
}

@Entity(tableName = "completed_deliveries")
data class CompletedDelivery(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val driverId: String = "driver_101",
    val orderId: String = "",
    val storeOrSellerName: String = "ShopSafe Verified Merchant",
    val customerName: String = "Customer",
    val pickupAddress: String = "100 Mission St, San Francisco, CA",
    val dropoffAddress: String = "220 Montgomery St, San Francisco, CA",
    val itemsSummary: String = "1x Grocery Order",
    val basePay: Double = 9.50,
    val tipAmount: Double = 5.00,
    val peakBonus: Double = 2.00,
    val totalEarnings: Double = 16.50,
    val distanceMiles: Double = 1.4,
    val durationMinutes: Int = 18,
    val timestamp: Long = System.currentTimeMillis(),
    val proofPhotoUrl: String = "",
    val status: String = "COMPLETED",
    val customerRating: Double = 5.0,
    val deliveryNotes: String = "Handed directly to customer with verified PIN."
) {
    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "driverId" to driverId,
            "orderId" to orderId,
            "storeOrSellerName" to storeOrSellerName,
            "customerName" to customerName,
            "pickupAddress" to pickupAddress,
            "dropoffAddress" to dropoffAddress,
            "itemsSummary" to itemsSummary,
            "basePay" to basePay,
            "tipAmount" to tipAmount,
            "peakBonus" to peakBonus,
            "totalEarnings" to totalEarnings,
            "distanceMiles" to distanceMiles,
            "durationMinutes" to durationMinutes,
            "timestamp" to timestamp,
            "proofPhotoUrl" to proofPhotoUrl,
            "status" to status,
            "customerRating" to customerRating,
            "deliveryNotes" to deliveryNotes
        )
    }

    companion object {
        fun fromFirestoreMap(map: Map<String, Any?>): CompletedDelivery {
            return CompletedDelivery(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                driverId = map["driverId"] as? String ?: "driver_101",
                orderId = map["orderId"] as? String ?: "",
                storeOrSellerName = map["storeOrSellerName"] as? String ?: "ShopSafe Merchant",
                customerName = map["customerName"] as? String ?: "Customer",
                pickupAddress = map["pickupAddress"] as? String ?: "",
                dropoffAddress = map["dropoffAddress"] as? String ?: "",
                itemsSummary = map["itemsSummary"] as? String ?: "Delivery Order",
                basePay = (map["basePay"] as? Number)?.toDouble() ?: 0.0,
                tipAmount = (map["tipAmount"] as? Number)?.toDouble() ?: 0.0,
                peakBonus = (map["peakBonus"] as? Number)?.toDouble() ?: 0.0,
                totalEarnings = (map["totalEarnings"] as? Number)?.toDouble() ?: 0.0,
                distanceMiles = (map["distanceMiles"] as? Number)?.toDouble() ?: 0.0,
                durationMinutes = (map["durationMinutes"] as? Number)?.toInt() ?: 0,
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                proofPhotoUrl = map["proofPhotoUrl"] as? String ?: "",
                status = map["status"] as? String ?: "COMPLETED",
                customerRating = (map["customerRating"] as? Number)?.toDouble() ?: 5.0,
                deliveryNotes = map["deliveryNotes"] as? String ?: ""
            )
        }
    }
}

/**
 * Represents a user's referral account status, unique referral code, and delivery fee credits balance.
 */
data class UserReferralProfile(
    val userId: String = "user_101",
    val userName: String = "Alex Rivera",
    val referralCode: String = "SHOPSAFE-ALEX789",
    val availableDeliveryCredits: Double = 5.00,
    val totalEarnedCredits: Double = 10.00,
    val totalFriendsReferred: Int = 1,
    val referredByCode: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "userName" to userName,
            "referralCode" to referralCode,
            "availableDeliveryCredits" to availableDeliveryCredits,
            "totalEarnedCredits" to totalEarnedCredits,
            "totalFriendsReferred" to totalFriendsReferred,
            "referredByCode" to referredByCode,
            "lastUpdated" to lastUpdated
        )
    }

    companion object {
        fun fromFirestoreMap(map: Map<String, Any?>): UserReferralProfile {
            return UserReferralProfile(
                userId = map["userId"] as? String ?: "user_101",
                userName = map["userName"] as? String ?: "ShopSafe Member",
                referralCode = map["referralCode"] as? String ?: "SHOPSAFE-MEMBER",
                availableDeliveryCredits = (map["availableDeliveryCredits"] as? Number)?.toDouble() ?: 5.00,
                totalEarnedCredits = (map["totalEarnedCredits"] as? Number)?.toDouble() ?: 5.00,
                totalFriendsReferred = (map["totalFriendsReferred"] as? Number)?.toInt() ?: 0,
                referredByCode = map["referredByCode"] as? String,
                lastUpdated = (map["lastUpdated"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Represents a individual referral event where a friend redeemed a code and both received delivery fee credits.
 */
data class ReferralRewardRecord(
    val id: String = UUID.randomUUID().toString(),
    val referrerUserId: String,
    val referrerName: String,
    val refereeUserId: String,
    val refereeName: String,
    val referralCode: String,
    val referrerRewardAmount: Double = 5.00,
    val refereeRewardAmount: Double = 5.00,
    val status: String = "COMPLETED",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "referrerUserId" to referrerUserId,
            "referrerName" to referrerName,
            "refereeUserId" to refereeUserId,
            "refereeName" to refereeName,
            "referralCode" to referralCode,
            "referrerRewardAmount" to referrerRewardAmount,
            "refereeRewardAmount" to refereeRewardAmount,
            "status" to status,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromFirestoreMap(map: Map<String, Any?>): ReferralRewardRecord {
            return ReferralRewardRecord(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                referrerUserId = map["referrerUserId"] as? String ?: "",
                referrerName = map["referrerName"] as? String ?: "Friend",
                refereeUserId = map["refereeUserId"] as? String ?: "",
                refereeName = map["refereeName"] as? String ?: "New User",
                referralCode = map["referralCode"] as? String ?: "",
                referrerRewardAmount = (map["referrerRewardAmount"] as? Number)?.toDouble() ?: 5.00,
                refereeRewardAmount = (map["refereeRewardAmount"] as? Number)?.toDouble() ?: 5.00,
                status = map["status"] as? String ?: "COMPLETED",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Automated Driver Mileage Tracker Record:
 * Tracks miles driven exclusively between store pickup and customer drop-off.
 * Explicitly excludes any commute driving to/from the driver's home.
 */
@Entity(tableName = "driver_mileage_logs")
data class DriverMileageLog(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val driverId: String = "driver_101",
    val orderId: String = "",
    val storeOrSellerName: String = "ShopSafe Merchant",
    val pickupAddress: String = "",
    val dropoffAddress: String = "",
    val milesDriven: Double = 0.0,
    val taxDeductionValue: Double = 0.0, // IRS Rate ($0.67/mile)
    val startTimestamp: Long = System.currentTimeMillis(),
    val endTimestamp: Long = System.currentTimeMillis(),
    val isHomeTravelExcluded: Boolean = true, // strictly active delivery only
    val status: String = "LOGGED", // "ACTIVE_TRACKING", "LOGGED"
    val durationMinutes: Int = 0
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "driverId" to driverId,
        "orderId" to orderId,
        "storeOrSellerName" to storeOrSellerName,
        "pickupAddress" to pickupAddress,
        "dropoffAddress" to dropoffAddress,
        "milesDriven" to milesDriven,
        "taxDeductionValue" to taxDeductionValue,
        "startTimestamp" to startTimestamp,
        "endTimestamp" to endTimestamp,
        "isHomeTravelExcluded" to isHomeTravelExcluded,
        "status" to status,
        "durationMinutes" to durationMinutes
    )

    companion object {
        fun fromFirestoreMap(map: Map<String, Any?>): DriverMileageLog {
            val miles = (map["milesDriven"] as? Number)?.toDouble() ?: 0.0
            return DriverMileageLog(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                driverId = map["driverId"] as? String ?: "driver_101",
                orderId = map["orderId"] as? String ?: "",
                storeOrSellerName = map["storeOrSellerName"] as? String ?: "ShopSafe Merchant",
                pickupAddress = map["pickupAddress"] as? String ?: "",
                dropoffAddress = map["dropoffAddress"] as? String ?: "",
                milesDriven = miles,
                taxDeductionValue = (map["taxDeductionValue"] as? Number)?.toDouble() ?: (miles * 0.67),
                startTimestamp = (map["startTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                endTimestamp = (map["endTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isHomeTravelExcluded = map["isHomeTravelExcluded"] as? Boolean ?: true,
                status = map["status"] as? String ?: "LOGGED",
                durationMinutes = (map["durationMinutes"] as? Number)?.toInt() ?: 0
            )
        }
    }
}


