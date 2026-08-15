package com.example.shopsafe.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ShopSafeFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM Registration Token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message Received from: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "⚡ New Driver Dispatch Ping!"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["storeName"]?.let { store ->
                val pay = remoteMessage.data["payAmount"] ?: "$18.75"
                "$store - $pay (72s to Accept!)"
            }
            ?: "You have a new incoming delivery offer with 72s countdown!"

        val storeName = remoteMessage.data["storeName"] ?: "ShopSafe Local Express Storefront"
        val payAmount = remoteMessage.data["payAmount"]?.toDoubleOrNull() ?: 18.75
        val pickupAddress = remoteMessage.data["pickupAddress"] ?: "100 Mission St, San Francisco, CA"
        val dropoffAddress = remoteMessage.data["dropoffAddress"] ?: "220 Montgomery St, Apt 14, San Francisco, CA"
        val distanceMiles = remoteMessage.data["distanceMiles"]?.toDoubleOrNull() ?: 1.2
        val estimatedMins = remoteMessage.data["estimatedMins"]?.toIntOrNull() ?: 18

        sendDispatchNotification(
            title = title,
            body = body,
            storeName = storeName,
            payAmount = payAmount,
            pickupAddress = pickupAddress,
            dropoffAddress = dropoffAddress,
            distanceMiles = distanceMiles,
            estimatedMins = estimatedMins
        )
    }

    private fun sendDispatchNotification(
        title: String,
        body: String,
        storeName: String,
        payAmount: Double,
        pickupAddress: String,
        dropoffAddress: String,
        distanceMiles: Double,
        estimatedMins: Int
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("EXTRA_ACTION", "TRIGGER_DISPATCH_PING")
            putExtra("EXTRA_STORE_NAME", storeName)
            putExtra("EXTRA_PAY_AMOUNT", payAmount)
            putExtra("EXTRA_PICKUP_ADDRESS", pickupAddress)
            putExtra("EXTRA_DROPOFF_ADDRESS", dropoffAddress)
            putExtra("EXTRA_DISTANCE_MILES", distanceMiles)
            putExtra("EXTRA_ESTIMATED_MINS", estimatedMins)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = CHANNEL_ID
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setSubText("ShopSafe Driver Dispatch")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Driver Dispatch Pings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time order alerts and 72-second dispatch notifications for ShopSafe drivers"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "ShopSafeFCMService"
        const val CHANNEL_ID = "shopsafe_driver_dispatch_pings"
        private const val NOTIFICATION_ID = 1001

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "ShopSafe Push Notifications & Order Updates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Real-time order status updates, driver approaching alerts, and dispatch pings"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun sendCustomerOrderStatusNotification(
            context: Context,
            title: String,
            body: String,
            orderId: String? = null,
            subText: String = "ShopSafe Real-Time Live Order Tracking"
        ) {
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("EXTRA_ACTION", "OPEN_ORDER_TRACKING")
                if (!orderId.isNullOrBlank()) {
                    putExtra("EXTRA_ORDER_ID", orderId)
                }
            }

            val notifId = ((orderId?.hashCode() ?: System.currentTimeMillis().toInt()) and 0x7FFFFFFF) % 100000 + 2000

            val pendingIntent = PendingIntent.getActivity(
                context,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            createNotificationChannel(context)
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setSubText(subText)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notifId, builder.build())
        }
    }
}
