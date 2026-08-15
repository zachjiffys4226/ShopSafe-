package com.example.shopsafe.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.shopsafe.data.models.*

@Database(
    entities = [
        MarketplaceItem::class,
        Storefront::class,
        FoodItem::class,
        Order::class,
        ChatMessage::class,
        ChatThread::class,
        DriverProfile::class,
        DriverOffer::class,
        PayoutTransaction::class,
        CompletedDelivery::class,
        DriverMileageLog::class
    ],
    version = 11,
    exportSchema = false
)
abstract class ShopSafeDatabase : RoomDatabase() {
    abstract fun dao(): ShopSafeDao

    companion object {
        @Volatile
        private var INSTANCE: ShopSafeDatabase? = null

        fun getDatabase(context: Context): ShopSafeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShopSafeDatabase::class.java,
                    "shopsafe_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
