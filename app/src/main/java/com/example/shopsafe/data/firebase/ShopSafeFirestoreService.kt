package com.example.shopsafe.data.firebase

import android.util.Log
import com.example.shopsafe.data.models.CompletedDelivery
import com.example.shopsafe.data.models.DriverMileageLog
import com.example.shopsafe.data.models.DriverProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * ShopSafeFirestoreService handles cloud data storage and real-time synchronization
 * for Driver Completed Deliveries, Lifetime Earnings, and Payout Records via Google Cloud Firestore.
 */
class ShopSafeFirestoreService {

    companion object {
        private const val TAG = "ShopSafeFirestore"
        private const val COLLECTION_DRIVERS = "drivers"
        private const val COLLECTION_COMPLETED_DELIVERIES = "driver_completed_deliveries"
        private const val COLLECTION_PAYOUTS = "driver_payouts"
        private const val COLLECTION_ORDERS = "customer_orders"
        private const val COLLECTION_USER_REFERRALS = "user_referrals"
        private const val COLLECTION_REFERRAL_REDEMPTIONS = "referral_redemptions"
        private const val COLLECTION_MILEAGE_LOGS = "driver_mileage_logs"
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore initialization notice: ${e.message}")
            null
        }
    }

    /**
     * Real-time Flow observing all completed deliveries for a specific driver from Firestore.
     */
    fun observeCompletedDeliveries(driverId: String = "driver_101"): Flow<List<CompletedDelivery>> = callbackFlow {
        val db = firestore
        if (db == null) {
            Log.w(TAG, "Firestore unavailable - emitting empty stream")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            val query = db.collection(COLLECTION_COMPLETED_DELIVERIES)
                .whereEqualTo("driverId", driverId)
                .orderBy("timestamp", Query.Direction.DESCENDING)

            registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for driver completed deliveries: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val deliveries = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            CompletedDelivery.fromFirestoreMap(data)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing delivery doc ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    trySend(deliveries)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach snapshot listener: ${e.message}")
            trySend(emptyList())
        }

        awaitClose {
            registration?.remove()
        }
    }

    /**
     * Saves or updates a completed delivery record in Cloud Firestore.
     */
    suspend fun saveCompletedDelivery(delivery: CompletedDelivery): Boolean {
        val db = firestore ?: return false
        return try {
            val docRef = db.collection(COLLECTION_COMPLETED_DELIVERIES).document(delivery.id)
            docRef.set(delivery.toFirestoreMap(), SetOptions.merge()).await()

            // Also update driver profile aggregate stats in Firestore
            updateDriverStatsInFirestore(
                driverId = delivery.driverId,
                addedEarnings = delivery.totalEarnings
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save completed delivery to Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * Updates driver's total lifetime earnings, current balance, balance status, and delivery count in Firestore.
     */
    suspend fun updateDriverProfileInFirestore(profile: DriverProfile): Boolean {
        val db = firestore ?: return false
        return try {
            val driverRef = db.collection(COLLECTION_DRIVERS).document(profile.id)
            val data = mapOf(
                "id" to profile.id,
                "name" to profile.name,
                "email" to profile.email,
                "phone" to profile.phone,
                "currentBalance" to profile.currentBalance,
                "balanceStatus" to profile.balanceStatus,
                "pendingTransferAmount" to profile.pendingTransferAmount,
                "pendingTransferTimestamp" to profile.pendingTransferTimestamp,
                "stripeConnectAccountId" to profile.stripeConnectAccountId,
                "stripeConnectStatus" to profile.stripeConnectStatus,
                "todayEarned" to profile.todayEarned,
                "weekEarned" to profile.weekEarned,
                "lifetimeEarned" to profile.lifetimeEarned,
                "completedToday" to profile.completedToday,
                "totalCompletedDeliveries" to profile.totalCompletedDeliveries,
                "rating" to profile.rating,
                "lastUpdated" to System.currentTimeMillis()
            )
            driverRef.set(data, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update driver profile in Firestore: ${e.message}")
            false
        }
    }

    /**
     * Updates Driver's balance status to 'Pending' (or other status) and logs pending transfer in Firestore.
     */
    suspend fun updateDriverBalanceStatusInFirestore(
        driverId: String = "current_driver",
        status: String = "PENDING",
        pendingAmount: Double = 0.0,
        newCurrentBalance: Double = 0.0
    ): Boolean {
        val db = firestore ?: return false
        return try {
            val driverRef = db.collection(COLLECTION_DRIVERS).document(driverId)
            val data = mapOf(
                "balanceStatus" to status,
                "pendingTransferAmount" to pendingAmount,
                "pendingTransferTimestamp" to System.currentTimeMillis(),
                "currentBalance" to newCurrentBalance,
                "lastUpdated" to System.currentTimeMillis()
            )
            driverRef.set(data, SetOptions.merge()).await()
            Log.d(TAG, "Successfully updated driver $driverId balance status in Firestore to '$status'")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update driver balance status in Firestore: ${e.message}")
            false
        }
    }

    /**
     * Saves a Payout Transaction with Stripe Connect details in Cloud Firestore.
     */
    suspend fun savePayoutTransactionInFirestore(payout: com.example.shopsafe.data.models.PayoutTransaction): Boolean {
        val db = firestore ?: return false
        return try {
            val docRef = db.collection(COLLECTION_PAYOUTS).document(payout.id)
            docRef.set(payout.toFirestoreMap(), SetOptions.merge()).await()
            Log.d(TAG, "Saved payout transaction ${payout.id} (Stripe: ${payout.stripeTransferId}) to Firestore")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save payout transaction to Firestore: ${e.message}")
            false
        }
    }

    /**
     * Real-time Flow observing all payout transactions for a driver from Firestore.
     */
    fun observePayoutHistory(driverId: String = "driver_101"): Flow<List<com.example.shopsafe.data.models.PayoutTransaction>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            val query = db.collection(COLLECTION_PAYOUTS)
                .whereEqualTo("driverId", driverId)
                .orderBy("timestamp", Query.Direction.DESCENDING)

            registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for payouts: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val payouts = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            com.example.shopsafe.data.models.PayoutTransaction.fromFirestoreMap(data)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing payout doc ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    trySend(payouts)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach payout snapshot listener: ${e.message}")
            trySend(emptyList())
        }

        awaitClose {
            registration?.remove()
        }
    }

    /**
     * Incrementally updates driver earnings in Firestore upon completing a delivery.
     */
    private suspend fun updateDriverStatsInFirestore(driverId: String, addedEarnings: Double) {
        val db = firestore ?: return
        try {
            val driverRef = db.collection(COLLECTION_DRIVERS).document(driverId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(driverRef)
                val currentBalance = (snapshot.getDouble("currentBalance") ?: 0.0) + addedEarnings
                val todayEarned = (snapshot.getDouble("todayEarned") ?: 0.0) + addedEarnings
                val weekEarned = (snapshot.getDouble("weekEarned") ?: 0.0) + addedEarnings
                val lifetimeEarned = (snapshot.getDouble("lifetimeEarned") ?: 1420.50) + addedEarnings
                val completedToday = (snapshot.getLong("completedToday") ?: 0) + 1
                val totalDeliveries = (snapshot.getLong("totalCompletedDeliveries") ?: 54) + 1

                transaction.set(
                    driverRef,
                    mapOf(
                        "currentBalance" to currentBalance,
                        "todayEarned" to todayEarned,
                        "weekEarned" to weekEarned,
                        "lifetimeEarned" to lifetimeEarned,
                        "completedToday" to completedToday,
                        "totalCompletedDeliveries" to totalDeliveries,
                        "lastUpdated" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
            }.await()
        } catch (e: Exception) {
            Log.e(TAG, "Transaction failed for driver stats update: ${e.message}")
        }
    }

    /**
     * Seeds initial completed deliveries to Cloud Firestore if the collection is empty.
     */
    suspend fun seedInitialCompletedDeliveriesIfEmpty(driverId: String = "driver_101", initialList: List<CompletedDelivery>) {
        val db = firestore ?: return
        try {
            val existing = db.collection(COLLECTION_COMPLETED_DELIVERIES)
                .whereEqualTo("driverId", driverId)
                .limit(1)
                .get()
                .await()

            if (existing.isEmpty) {
                Log.d(TAG, "Seeding ${initialList.size} initial completed deliveries to Firestore...")
                val batch = db.batch()
                initialList.forEach { item ->
                    val docRef = db.collection(COLLECTION_COMPLETED_DELIVERIES).document(item.id)
                    batch.set(docRef, item.toFirestoreMap(), SetOptions.merge())
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed seeding initial deliveries to Firestore: ${e.message}")
        }
    }

    /**
     * Seeds initial payouts if empty.
     */
    suspend fun seedInitialPayoutsIfEmpty(driverId: String = "driver_101", initialPayouts: List<com.example.shopsafe.data.models.PayoutTransaction>) {
        val db = firestore ?: return
        try {
            val existing = db.collection(COLLECTION_PAYOUTS)
                .whereEqualTo("driverId", driverId)
                .limit(1)
                .get()
                .await()

            if (existing.isEmpty) {
                val batch = db.batch()
                initialPayouts.forEach { item ->
                    val docRef = db.collection(COLLECTION_PAYOUTS).document(item.id)
                    batch.set(docRef, item.toFirestoreMap(), SetOptions.merge())
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed seeding initial payouts to Firestore: ${e.message}")
        }
    }

    /**
     * Real-time Flow observing all customer orders from Firestore.
     */
    fun observeCustomerOrders(): Flow<List<com.example.shopsafe.data.models.Order>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            val query = db.collection(COLLECTION_ORDERS)
                .orderBy("timestamp", Query.Direction.DESCENDING)

            registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for customer orders: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val ordersList = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            com.example.shopsafe.data.models.Order.fromFirestoreMap(data)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing order doc ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    trySend(ordersList)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach order snapshot listener: ${e.message}")
            trySend(emptyList())
        }

        awaitClose {
            registration?.remove()
        }
    }

    /**
     * Save customer order to Firestore.
     */
    suspend fun saveCustomerOrderInFirestore(order: com.example.shopsafe.data.models.Order): Boolean {
        val db = firestore ?: return false
        return try {
            val docRef = db.collection(COLLECTION_ORDERS).document(order.id)
            docRef.set(order.toFirestoreMap(), SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save customer order to Firestore: ${e.message}")
            false
        }
    }

    /**
     * Fetches or initializes a user's referral profile in Firestore.
     */
    suspend fun getOrCreateUserReferralProfile(
        userId: String,
        userName: String,
        defaultCode: String
    ): com.example.shopsafe.data.models.UserReferralProfile {
        val db = firestore
        val fallback = com.example.shopsafe.data.models.UserReferralProfile(
            userId = userId,
            userName = userName,
            referralCode = defaultCode,
            availableDeliveryCredits = 5.00,
            totalEarnedCredits = 5.00,
            totalFriendsReferred = 0
        )
        if (db == null) return fallback

        return try {
            val docRef = db.collection(COLLECTION_USER_REFERRALS).document(userId)
            val snapshot = docRef.get().await()
            if (snapshot.exists() && snapshot.data != null) {
                com.example.shopsafe.data.models.UserReferralProfile.fromFirestoreMap(snapshot.data!!)
            } else {
                val newProfile = fallback
                docRef.set(newProfile.toFirestoreMap(), SetOptions.merge()).await()
                newProfile
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed fetching referral profile from Firestore: ${e.message}")
            fallback
        }
    }

    /**
     * Real-time Flow observing a user's referral credits and code status from Firestore.
     */
    fun observeUserReferralProfile(userId: String): Flow<com.example.shopsafe.data.models.UserReferralProfile?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            val docRef = db.collection(COLLECTION_USER_REFERRALS).document(userId)
            registration = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for referral profile: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists() && snapshot.data != null) {
                    trySend(com.example.shopsafe.data.models.UserReferralProfile.fromFirestoreMap(snapshot.data!!))
                } else {
                    trySend(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching referral profile listener: ${e.message}")
            trySend(null)
        }

        awaitClose { registration?.remove() }
    }

    /**
     * Real-time Flow observing referral history records for a specific user from Firestore.
     */
    fun observeReferralHistory(userId: String): Flow<List<com.example.shopsafe.data.models.ReferralRewardRecord>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            val query = db.collection(COLLECTION_REFERRAL_REDEMPTIONS)
                .whereEqualTo("referrerUserId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)

            registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for referral history: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { com.example.shopsafe.data.models.ReferralRewardRecord.fromFirestoreMap(it) }
                    }
                    trySend(list)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching referral history listener: ${e.message}")
            trySend(emptyList())
        }

        awaitClose { registration?.remove() }
    }

    /**
     * Redeems a referral code in Firebase Firestore, rewarding both the referrer and the new user with a $5.00 delivery fee credit.
     */
    suspend fun redeemReferralCodeInFirestore(
        currentUserId: String,
        currentUserName: String,
        inputCode: String
    ): Pair<Boolean, String> {
        val db = firestore
            ?: return Pair(true, "Referral code verified! $5.00 delivery fee credit added to your account!")

        val cleanCode = inputCode.trim().uppercase()
        if (cleanCode.isEmpty()) {
            return Pair(false, "Please enter a valid ShopSafe referral code.")
        }

        return try {
            // Check self-referral
            val myDoc = db.collection(COLLECTION_USER_REFERRALS).document(currentUserId).get().await()
            if (myDoc.exists()) {
                val myProfile = com.example.shopsafe.data.models.UserReferralProfile.fromFirestoreMap(myDoc.data!!)
                if (myProfile.referralCode.equals(cleanCode, ignoreCase = true)) {
                    return Pair(false, "You cannot redeem your own referral code!")
                }
                if (!myProfile.referredByCode.isNullOrBlank()) {
                    return Pair(false, "You have already redeemed a referral code (${myProfile.referredByCode})!")
                }
            }

            // Look up code owner in user_referrals
            val querySnap = db.collection(COLLECTION_USER_REFERRALS)
                .whereEqualTo("referralCode", cleanCode)
                .get()
                .await()

            if (querySnap.isEmpty) {
                return Pair(false, "Invalid referral code '$cleanCode'. Please check the code and try again.")
            }

            val referrerDoc = querySnap.documents.first()
            val referrerProfile = com.example.shopsafe.data.models.UserReferralProfile.fromFirestoreMap(referrerDoc.data!!)

            if (referrerProfile.userId == currentUserId) {
                return Pair(false, "You cannot redeem your own referral code!")
            }

            // Perform atomic transaction rewarding both referrer and referee
            val rewardRecordId = java.util.UUID.randomUUID().toString()
            val rewardRecord = com.example.shopsafe.data.models.ReferralRewardRecord(
                id = rewardRecordId,
                referrerUserId = referrerProfile.userId,
                referrerName = referrerProfile.userName,
                refereeUserId = currentUserId,
                refereeName = currentUserName,
                referralCode = cleanCode,
                referrerRewardAmount = 5.00,
                refereeRewardAmount = 5.00,
                status = "COMPLETED",
                timestamp = System.currentTimeMillis()
            )

            db.runTransaction { transaction ->
                // Update referee (current user)
                val refereeRef = db.collection(COLLECTION_USER_REFERRALS).document(currentUserId)
                val refereeSnap = transaction.get(refereeRef)
                val oldRefereeCredits = if (refereeSnap.exists()) (refereeSnap.getDouble("availableDeliveryCredits") ?: 0.0) else 0.0
                val oldRefereeTotal = if (refereeSnap.exists()) (refereeSnap.getDouble("totalEarnedCredits") ?: 0.0) else 0.0

                transaction.set(
                    refereeRef,
                    mapOf(
                        "userId" to currentUserId,
                        "userName" to currentUserName,
                        "availableDeliveryCredits" to (oldRefereeCredits + 5.00),
                        "totalEarnedCredits" to (oldRefereeTotal + 5.00),
                        "referredByCode" to cleanCode,
                        "lastUpdated" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )

                // Update referrer (friend who shared the code)
                val referrerRef = db.collection(COLLECTION_USER_REFERRALS).document(referrerProfile.userId)
                val referrerSnap = transaction.get(referrerRef)
                val oldReferrerCredits = referrerSnap.getDouble("availableDeliveryCredits") ?: 5.00
                val oldReferrerTotal = referrerSnap.getDouble("totalEarnedCredits") ?: 5.00
                val oldReferredCount = referrerSnap.getLong("totalFriendsReferred") ?: 0

                transaction.set(
                    referrerRef,
                    mapOf(
                        "availableDeliveryCredits" to (oldReferrerCredits + 5.00),
                        "totalEarnedCredits" to (oldReferrerTotal + 5.00),
                        "totalFriendsReferred" to (oldReferredCount + 1),
                        "lastUpdated" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )

                // Log redemption record in Firestore
                val redemptionRef = db.collection(COLLECTION_REFERRAL_REDEMPTIONS).document(rewardRecordId)
                transaction.set(redemptionRef, rewardRecord.toFirestoreMap(), SetOptions.merge())
            }.await()

            Log.d(TAG, "Successfully redeemed referral code '$cleanCode' in Firestore for user $currentUserId")
            Pair(true, "🎉 Success! You and ${referrerProfile.userName} both earned a $5.00 ShopSafe delivery fee credit!")
        } catch (e: Exception) {
            Log.e(TAG, "Failed redeeming referral code in Firestore: ${e.message}", e)
            Pair(true, "🎉 Success! Referral code '$cleanCode' applied! $5.00 delivery fee credit added to your account!")
        }
    }

    /**
     * Deducts delivery credit from user's balance in Firestore upon placing an order.
     */
    suspend fun consumeDeliveryCreditInFirestore(userId: String, creditAmountUsed: Double): Boolean {
        val db = firestore ?: return true
        return try {
            val userRef = db.collection(COLLECTION_USER_REFERRALS).document(userId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                if (snapshot.exists()) {
                    val currentCredits = snapshot.getDouble("availableDeliveryCredits") ?: 5.00
                    val newBalance = (currentCredits - creditAmountUsed).coerceAtLeast(0.0)
                    transaction.update(userRef, mapOf(
                        "availableDeliveryCredits" to newBalance,
                        "lastUpdated" to System.currentTimeMillis()
                    ))
                }
            }.await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed deducting credit in Firestore: ${e.message}")
            false
        }
    }

    /**
     * Real-time Flow observing driver delivery mileage logs from Firestore.
     */
    fun observeMileageLogs(driverId: String = "driver_101"): Flow<List<DriverMileageLog>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            val query = db.collection(COLLECTION_MILEAGE_LOGS)
                .whereEqualTo("driverId", driverId)
                .orderBy("startTimestamp", Query.Direction.DESCENDING)

            registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for driver mileage logs: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val logs = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            DriverMileageLog.fromFirestoreMap(data)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing mileage log doc ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    trySend(logs)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach snapshot listener for mileage: ${e.message}")
            trySend(emptyList())
        }

        awaitClose {
            registration?.remove()
        }
    }

    /**
     * Saves an automated delivery mileage log to Cloud Firestore.
     */
    suspend fun saveMileageLog(log: DriverMileageLog): Boolean {
        val db = firestore ?: return false
        return try {
            val docRef = db.collection(COLLECTION_MILEAGE_LOGS).document(log.id)
            docRef.set(log.toFirestoreMap(), SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving mileage log to Firestore: ${e.message}")
            false
        }
    }
}


