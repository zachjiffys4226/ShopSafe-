package com.example.shopsafe.data.seed

import com.example.shopsafe.data.models.CompletedDelivery
import java.util.UUID

object DriverSeedData {

    fun getInitialCompletedDeliveries(driverId: String = "driver_101"): List<CompletedDelivery> {
        val now = System.currentTimeMillis()
        val oneHour = 3600 * 1000L
        val oneDay = 24 * oneHour

        return listOf(
            CompletedDelivery(
                id = "del_hist_01",
                driverId = driverId,
                orderId = "ord_hist_101",
                storeOrSellerName = "ShopSafe Local Storefront Hub #1",
                customerName = "David Kim",
                pickupAddress = "100 Mission St, San Francisco, CA",
                dropoffAddress = "340 Pine St, Apt 8B, San Francisco, CA",
                itemsSummary = "Personal Shopper Express Grocery Order (4 items)",
                basePay = 14.50,
                tipAmount = 7.00,
                peakBonus = 3.00,
                totalEarnings = 24.50,
                distanceMiles = 1.6,
                durationMinutes = 22,
                timestamp = now - (45 * 60 * 1000L), // 45 mins ago today
                proofPhotoUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&auto=format&fit=crop&q=80",
                status = "COMPLETED",
                customerRating = 5.0,
                deliveryNotes = "Handed directly to resident. Verified 4-digit PIN 8821."
            ),
            CompletedDelivery(
                id = "del_hist_02",
                driverId = driverId,
                orderId = "ord_hist_102",
                storeOrSellerName = "Burger Craft & Shake Co.",
                customerName = "Samantha Reed",
                pickupAddress = "525 Market St, San Francisco, CA",
                dropoffAddress = "88 Colin P Kelly Jr St, San Francisco, CA",
                itemsSummary = "2x Double Wagyu Burger + Truffle Fries + Salted Caramel Shake",
                basePay = 9.25,
                tipAmount = 6.50,
                peakBonus = 2.50,
                totalEarnings = 18.25,
                distanceMiles = 1.1,
                durationMinutes = 14,
                timestamp = now - (2 * oneHour), // 2 hours ago today
                proofPhotoUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=80",
                status = "COMPLETED",
                customerRating = 5.0,
                deliveryNotes = "Left at front desk concierge with building attendant."
            ),
            CompletedDelivery(
                id = "del_hist_03",
                driverId = driverId,
                orderId = "ord_hist_103",
                storeOrSellerName = "CVS Pharmacy Express",
                customerName = "Michael Zhang",
                pickupAddress = "701 Market St, San Francisco, CA",
                dropoffAddress = "220 Montgomery St, Suite 400, San Francisco, CA",
                itemsSummary = "First Aid Essentials + Cold Relief Medicine + Electrolyte Drinks",
                basePay = 10.30,
                tipAmount = 6.50,
                peakBonus = 0.00,
                totalEarnings = 16.80,
                distanceMiles = 0.9,
                durationMinutes = 16,
                timestamp = now - (4 * oneHour), // 4 hours ago today
                proofPhotoUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=800&auto=format&fit=crop&q=80",
                status = "COMPLETED",
                customerRating = 4.9,
                deliveryNotes = "Verified ID for non-prescription healthcare supplies."
            ),
            CompletedDelivery(
                id = "del_hist_04",
                driverId = driverId,
                orderId = "ord_hist_104",
                storeOrSellerName = "Fresh Market Organic Groceries",
                customerName = "Jessica Taylor",
                pickupAddress = "850 Valencia St, San Francisco, CA",
                dropoffAddress = "1200 Van Ness Ave, San Francisco, CA",
                itemsSummary = "Organic Sourdough + Farm Eggs + Almond Milk + Fresh Berries (6 items)",
                basePay = 16.00,
                tipAmount = 9.00,
                peakBonus = 3.00,
                totalEarnings = 28.00,
                distanceMiles = 2.3,
                durationMinutes = 28,
                timestamp = now - (6 * oneHour), // 6 hours ago today
                proofPhotoUrl = "https://images.unsplash.com/photo-1543083477-4f785aeafaa9?w=800&auto=format&fit=crop&q=80",
                status = "COMPLETED",
                customerRating = 5.0,
                deliveryNotes = "Carefully packed cold produce with insulated tote bag."
            ),
            CompletedDelivery(
                id = "del_hist_05",
                driverId = driverId,
                orderId = "ord_hist_105",
                storeOrSellerName = "Taco Fiesta Gourmet",
                customerName = "Carlos Mendoza",
                pickupAddress = "3300 24th St, San Francisco, CA",
                dropoffAddress = "450 Howard St, San Francisco, CA",
                itemsSummary = "Street Taco Platter (6 tacos) + Guacamole & Fresh Tortilla Chips",
                basePay = 8.50,
                tipAmount = 5.00,
                peakBonus = 2.00,
                totalEarnings = 15.50,
                distanceMiles = 1.4,
                durationMinutes = 19,
                timestamp = now - oneDay - (2 * oneHour), // Yesterday
                proofPhotoUrl = "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=800&auto=format&fit=crop&q=80",
                status = "COMPLETED",
                customerRating = 5.0,
                deliveryNotes = "Direct handover to client outside lobby."
            ),
            CompletedDelivery(
                id = "del_hist_06",
                driverId = driverId,
                orderId = "ord_hist_106",
                storeOrSellerName = "ShopSafe Marketplace Escrow Pickup",
                customerName = "Emily Watson",
                pickupAddress = "742 Market St, Safe Exchange Zone, San Francisco, CA",
                dropoffAddress = "601 Townsend St, San Francisco, CA",
                itemsSummary = "Sony WH-1000XM5 Wireless Headphones (Verified Marketplace Handover)",
                basePay = 15.00,
                tipAmount = 7.00,
                peakBonus = 0.00,
                totalEarnings = 22.00,
                distanceMiles = 2.1,
                durationMinutes = 25,
                timestamp = now - oneDay - (5 * oneHour), // Yesterday
                proofPhotoUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80",
                status = "COMPLETED",
                customerRating = 5.0,
                deliveryNotes = "ShopSafe QR Code scanned at safe exchange hub."
            ),
            CompletedDelivery(
                id = "del_hist_07",
                driverId = driverId,
                orderId = "ord_hist_107",
                storeOrSellerName = "Golden Dragon Dim Sum",
                customerName = "Brian Lin",
                pickupAddress = "818 Washington St, San Francisco, CA",
                dropoffAddress = "150 California St, San Francisco, CA",
                itemsSummary = "Har Gow Steamed Shrimp Dumplings + BBQ Pork Buns + Roast Duck",
                basePay = 11.40,
                tipAmount = 8.00,
                peakBonus = 2.00,
                totalEarnings = 21.40,
                distanceMiles = 1.8,
                durationMinutes = 20,
                timestamp = now - (2 * oneDay), // 2 days ago
                proofPhotoUrl = "https://images.unsplash.com/photo-1498654896293-37aacf113fd9?w=800&auto=format&fit=crop&q=80",
                status = "COMPLETED",
                customerRating = 5.0,
                deliveryNotes = "Fast delivery during lunch peak."
            ),
            CompletedDelivery(
                id = "del_hist_08",
                driverId = driverId,
                orderId = "ord_hist_108",
                storeOrSellerName = "Walgreens 24/7",
                customerName = "Rachel Adams",
                pickupAddress = "135 4th St, San Francisco, CA",
                dropoffAddress = "500 3rd St, Apt 204, San Francisco, CA",
                itemsSummary = "Prescription Refill + Sparkling Water 12-pack + Protein Bars",
                basePay = 9.20,
                tipAmount = 5.00,
                peakBonus = 0.00,
                totalEarnings = 14.20,
                distanceMiles = 0.7,
                durationMinutes = 12,
                timestamp = now - (3 * oneDay), // 3 days ago
                proofPhotoUrl = "https://images.unsplash.com/photo-1576602976047-174e57a47881?w=800&auto=format&fit=crop&q=80",
                status = "COMPLETED",
                customerRating = 5.0,
                deliveryNotes = "Handed off with signed digital receipt."
            ),
            CompletedDelivery(
                id = "del_hist_09",
                driverId = driverId,
                orderId = "ord_hist_109",
                storeOrSellerName = "Artisan Sourdough Bakery",
                customerName = "Oliver Queen",
                pickupAddress = "1400 Haight St, San Francisco, CA",
                dropoffAddress = "250 King St, San Francisco, CA",
                itemsSummary = "Country Sourdough Loaf + 4x Almond Croissants + Cold Brew Jug",
                basePay = 12.00,
                tipAmount = 6.00,
                peakBonus = 1.50,
                totalEarnings = 19.50,
                distanceMiles = 2.8,
                durationMinutes = 26,
                timestamp = now - (4 * oneDay), // 4 days ago
                proofPhotoUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=800&auto=format&fit=crop&q=80",
                status = "COMPLETED",
                customerRating = 5.0,
                deliveryNotes = "Early morning pastry delivery."
            ),
            CompletedDelivery(
                id = "del_hist_10",
                driverId = driverId,
                orderId = "ord_hist_110",
                storeOrSellerName = "Pizzeria Napoletana",
                customerName = "Lucas Romero",
                pickupAddress = "1600 Stockton St, San Francisco, CA",
                dropoffAddress = "700 Bush St, San Francisco, CA",
                itemsSummary = "1x Margherita Pizza + 1x Diavola Pizza + Garlic Knots",
                basePay = 10.50,
                tipAmount = 7.50,
                peakBonus = 3.00,
                totalEarnings = 21.00,
                distanceMiles = 1.5,
                durationMinutes = 18,
                timestamp = now - (5 * oneDay), // 5 days ago
                proofPhotoUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800&auto=format&fit=crop&q=80",
                status = "COMPLETED",
                customerRating = 5.0,
                deliveryNotes = "Kept hot in thermal pizza bag. Delivered piping hot."
            )
        )
    }
}
