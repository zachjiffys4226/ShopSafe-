package com.example

import android.app.Application
import android.content.ComponentCallbacks2
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.android.gms.maps.MapsInitializer
import com.google.firebase.FirebaseApp

class ShopSafeApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        // 0. Global Uncaught Exception Handler to prevent hard process crash
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("ShopSafeApp", "Uncaught exception in thread ${thread.name}: ${throwable.message}", throwable)
            try {
                defaultHandler?.uncaughtException(thread, throwable)
            } catch (_: Throwable) {
                // Prevent crash loop
            }
        }

        // 1. Initialize Firebase App safely
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("ShopSafeApp", "FirebaseApp initialized successfully in Application.onCreate()")
            }
        } catch (e: Throwable) {
            Log.w("ShopSafeApp", "FirebaseApp initialization notice: ${e.message}")
        }

        // 2. Initialize Google Maps safely
        try {
            MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST) { renderer ->
                when (renderer) {
                    MapsInitializer.Renderer.LATEST -> Log.d("ShopSafeApp", "Google Maps modern LATEST renderer loaded.")
                    MapsInitializer.Renderer.LEGACY -> Log.d("ShopSafeApp", "Google Maps fallback legacy renderer loaded.")
                }
            }
        } catch (e: Throwable) {
            Log.d("ShopSafeApp", "MapsInitializer notice: ${e.message}")
        }

        // 3. Initialize Stripe Android SDK PaymentConfiguration safely
        try {
            com.stripe.android.PaymentConfiguration.init(
                applicationContext,
                "pk_test_51OzShopSafeIssuingSandbox"
            )
            Log.d("ShopSafeApp", "Stripe Android SDK PaymentConfiguration initialized.")
        } catch (e: Throwable) {
            Log.w("ShopSafeApp", "Stripe initialization notice: ${e.message}")
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024) // 50 MB
                    .build()
            }
            .allowHardware(false) // Prevents ashmem pinning issues in virtualized graphics environments
            .crossfade(true)
            .build()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        try {
            val loader = coil.Coil.imageLoader(this)
            loader.memoryCache?.trimMemory(level)
            if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
                loader.memoryCache?.clear()
            }
        } catch (e: Exception) {
            Log.d("ShopSafeApp", "Memory trim handled: ${e.message}")
        }
    }
}

