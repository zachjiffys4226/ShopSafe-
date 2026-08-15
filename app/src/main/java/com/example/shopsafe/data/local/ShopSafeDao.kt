package com.example.shopsafe.data.local

import androidx.room.*
import com.example.shopsafe.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopSafeDao {
    // Marketplace Queries
    @Query("SELECT * FROM marketplace_items ORDER BY timestamp DESC")
    fun getAllMarketplaceItems(): Flow<List<MarketplaceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketplaceItem(item: MarketplaceItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketplaceItems(items: List<MarketplaceItem>)

    @Query("SELECT COUNT(*) FROM marketplace_items")
    suspend fun getMarketplaceItemCount(): Int

    @Query("SELECT * FROM marketplace_items WHERE id = :id")
    suspend fun getMarketplaceItemById(id: String): MarketplaceItem?

    // Storefront & Food Queries
    @Query("SELECT * FROM storefronts")
    fun getAllStorefronts(): Flow<List<Storefront>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStorefronts(stores: List<Storefront>)

    @Query("SELECT * FROM food_items WHERE storeId = :storeId")
    fun getFoodItemsForStore(storeId: String): Flow<List<FoodItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(items: List<FoodItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(item: FoodItem)

    // Order Queries
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: String): Order?

    @Query("SELECT * FROM orders WHERE id = :id")
    fun observeOrderById(id: String): Flow<Order?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: String, status: String)

    @Query("UPDATE orders SET customerRating = :rating, customerFeedback = :feedback, hasBeenReviewed = 1 WHERE id = :id")
    suspend fun updateOrderReview(id: String, rating: Int, feedback: String)

    // Chat Queries
    @Query("SELECT * FROM chat_threads ORDER BY lastUpdated DESC")
    fun getAllChatThreads(): Flow<List<ChatThread>>

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesForThread(threadId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatThread(thread: ChatThread)

    @Query("UPDATE chat_threads SET lastMessage = :text, lastUpdated = :timestamp WHERE threadId = :threadId")
    suspend fun updateThreadLastMessage(threadId: String, text: String, timestamp: Long)

    // Driver Portal Queries
    @Query("SELECT * FROM driver_profile WHERE id = 'current_driver'")
    fun getDriverProfile(): Flow<DriverProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDriverProfile(profile: DriverProfile)

    @Query("SELECT * FROM driver_offers")
    fun getAvailableDriverOffers(): Flow<List<DriverOffer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriverOffers(offers: List<DriverOffer>)

    @Query("DELETE FROM driver_offers WHERE id = :id")
    suspend fun deleteDriverOffer(id: String)

    @Query("SELECT * FROM payouts ORDER BY timestamp DESC")
    fun getPayoutHistory(): Flow<List<PayoutTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayoutTransaction(transaction: PayoutTransaction)

    @Query("UPDATE payouts SET status = :status, stripeTransferId = :stripeTransferId WHERE id = :id")
    suspend fun updatePayoutStatus(id: String, status: String, stripeTransferId: String)

    @Query("SELECT * FROM payouts WHERE id = :id")
    suspend fun getPayoutById(id: String): PayoutTransaction?

    // Completed Deliveries Queries
    @Query("SELECT * FROM completed_deliveries ORDER BY timestamp DESC")
    fun getAllCompletedDeliveries(): Flow<List<CompletedDelivery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletedDelivery(delivery: CompletedDelivery)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletedDeliveries(deliveries: List<CompletedDelivery>)

    @Query("SELECT COUNT(*) FROM completed_deliveries")
    suspend fun getCompletedDeliveryCount(): Int

    @Query("SELECT SUM(totalEarnings) FROM completed_deliveries")
    suspend fun getTotalCompletedEarnings(): Double?

    @Query("SELECT * FROM completed_deliveries WHERE id = :id")
    suspend fun getCompletedDeliveryById(id: String): CompletedDelivery?

    @Query("DELETE FROM completed_deliveries WHERE id = :id")
    suspend fun deleteCompletedDelivery(id: String)

    // Driver Automated Mileage Tracking Queries (Pickup-to-Dropoff Delivery Miles Only)
    @Query("SELECT * FROM driver_mileage_logs ORDER BY startTimestamp DESC")
    fun getAllMileageLogs(): Flow<List<DriverMileageLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMileageLog(log: DriverMileageLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMileageLogs(logs: List<DriverMileageLog>)

    @Query("SELECT SUM(milesDriven) FROM driver_mileage_logs")
    fun getTotalDeliveryMileage(): Flow<Double?>

    @Query("SELECT SUM(taxDeductionValue) FROM driver_mileage_logs")
    fun getTotalTaxDeduction(): Flow<Double?>

    @Query("SELECT SUM(milesDriven) FROM driver_mileage_logs WHERE startTimestamp >= :startOfDayTimestamp")
    fun getTodayDeliveryMileage(startOfDayTimestamp: Long): Flow<Double?>

    @Query("DELETE FROM driver_mileage_logs WHERE id = :id")
    suspend fun deleteMileageLog(id: String)

    @Query("DELETE FROM driver_mileage_logs")
    suspend fun clearAllMileageLogs()

    // Admin Panel Queries
    @Query("DELETE FROM marketplace_items WHERE id = :id")
    suspend fun deleteMarketplaceItem(id: String)

    @Query("DELETE FROM storefronts WHERE id = :id")
    suspend fun deleteStorefront(id: String)

    @Query("DELETE FROM food_items WHERE id = :id")
    suspend fun deleteFoodItem(id: String)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrder(id: String)

    @Query("UPDATE food_items SET price = :newPrice WHERE id = :id")
    suspend fun updateFoodItemPrice(id: String, newPrice: Double)

    @Query("UPDATE storefronts SET isOpen = :isOpen WHERE id = :id")
    suspend fun updateStorefrontOpenStatus(id: String, isOpen: Boolean)
}
