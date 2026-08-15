package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shopsafe.data.models.DriverOffer
import com.example.shopsafe.service.ShopSafeFirebaseMessagingService
import com.example.shopsafe.ui.AppMode
import com.example.shopsafe.ui.ShopSafeViewModel
import com.example.shopsafe.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create FCM Notification Channel for Driver Order Alerts
        ShopSafeFirebaseMessagingService.createNotificationChannel(this)

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        // Fetch FCM token & subscribe to driver dispatch topic safely
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            if (FirebaseApp.getApps(this).isNotEmpty()) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Log.d("MainActivity", "FCM Device Token: $token")
                    }
                }

                FirebaseMessaging.getInstance().subscribeToTopic("driver_dispatches")
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("MainActivity", "Subscribed to FCM topic: driver_dispatches")
                        }
                    }
            } else {
                Log.d("MainActivity", "FirebaseApp not initialized; skipping FCM topic subscription.")
            }
        } catch (e: Exception) {
            Log.d("MainActivity", "Firebase Messaging initialization note: ${e.message}")
        }

        setContent {
            MyApplicationTheme {
                val viewModel: ShopSafeViewModel = viewModel()

                LaunchedEffect(intent) {
                    handleNotificationIntent(intent, viewModel)
                }

                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?, viewModel: ShopSafeViewModel) {
        if (intent == null) return
        val action = intent.getStringExtra("EXTRA_ACTION")
        if (action == "OPEN_ORDER_TRACKING") {
            val orderId = intent.getStringExtra("EXTRA_ORDER_ID")
            viewModel.switchAppMode(AppMode.CUSTOMER_SHOPPING)
            viewModel.customerTab.value = com.example.shopsafe.ui.CustomerTab.ORDERS
            if (!orderId.isNullOrBlank()) {
                viewModel.activeTrackedOrderId.value = orderId
            }
        } else if (action == "TRIGGER_DISPATCH_PING") {
            val storeName = intent.getStringExtra("EXTRA_STORE_NAME") ?: "ShopSafe Local Express Storefront"
            val payAmount = intent.getDoubleExtra("EXTRA_PAY_AMOUNT", 18.75)
            val pickupAddress = intent.getStringExtra("EXTRA_PICKUP_ADDRESS") ?: "100 Mission St, San Francisco, CA"
            val dropoffAddress = intent.getStringExtra("EXTRA_DROPOFF_ADDRESS") ?: "220 Montgomery St, Apt 14, San Francisco, CA"
            val distanceMiles = intent.getDoubleExtra("EXTRA_DISTANCE_MILES", 1.2)
            val estimatedMins = intent.getIntExtra("EXTRA_ESTIMATED_MINS", 18)

            val offer = DriverOffer(
                storeName = storeName,
                pickupAddress = pickupAddress,
                dropoffAddress = dropoffAddress,
                payAmount = payAmount,
                distanceMiles = distanceMiles,
                estimatedMins = estimatedMins,
                itemDetails = "Incoming Background FCM Dispatch Order",
                isShopSafeStorefrontShopping = true
            )

            viewModel.switchAppMode(AppMode.DRIVER_PORTAL)
            viewModel.triggerDispatchPing(offer)
        }
    }
}
