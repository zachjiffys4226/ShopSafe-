package com.example.shopsafe.data.models

import com.google.android.gms.maps.model.LatLng
import java.util.Locale
import java.util.UUID

/**
 * Driver Hotspot Opportunity Types
 */
enum class DriverHotspotType(
    val displayName: String,
    val categoryBadge: String,
    val iconEmoji: String,
    val colorHex: Long
) {
    STORE_ORDER("Store Order", "RETAIL / GROCERY", "🛒", 0xFF16A34A),
    RESTAURANT_ORDER("Restaurant Order", "FOOD DELIVERY", "🍔", 0xFFEA580C),
    LOCAL_SELLER("Local Seller", "VERIFIED VENDOR", "🏪", 0xFF0284C7),
    MARKETPLACE_ITEM("Marketplace Item", "P2P ITEM PICKUP", "📦", 0xFF7C3AED),
    PERSON_TO_PERSON("Person-to-Person", "P2P DELIVERY", "🤝", 0xFF0D9488),
    HIGH_DEMAND_SURGE("High Demand Zone", "BONUS SURGE +$3", "🔥", 0xFFDC2626),
    GAS_STATION("Cheap Fuel Stop", "DRIVER AMENITY", "⛽", 0xFF475569),
    DRIVER_SUPPORT("Driver Hub", "SUPPORT CENTER", "🛡️", 0xFF4F46E5)
}

/**
 * Delivery Stop Model for Multi-Stop Orders
 */
data class DeliveryStop(
    val stopNumber: Int,
    val title: String,
    val address: String,
    val latLng: LatLng,
    val isPickup: Boolean,
    val estimatedMins: Int,
    val customerOrStoreName: String,
    val isCompleted: Boolean = false
)

/**
 * Interactive Driver Opportunity Hotspot on Google Maps
 */
data class DriverOpportunityHotspot(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: DriverHotspotType,
    val latLng: LatLng,
    val payAmount: Double,
    val distanceMiles: Double,
    val estimatedMins: Int,
    val pickupAddress: String,
    val dropoffAddress: String,
    val storeOrSellerName: String,
    val customerName: String = "Customer",
    val itemCount: Int = 1,
    val itemSummary: String = "Verified ShopSafe Item",
    val requiredVehicleType: String = "Sedan",
    val specialInstructions: String = "",
    val basePay: Double = payAmount * 0.65,
    val tipAmount: Double = payAmount * 0.25,
    val bonusAmount: Double = payAmount * 0.10,
    val isSurge: Boolean = false,
    val surgeMultiplier: Double = 1.0,
    val stopsCount: Int = 2,
    val stops: List<DeliveryStop> = emptyList(),
    val isHighDemandZone: Boolean = false,
    val demandOrdersAvailable: Int = 8,
    val demandTypicalHourlyPay: Double = 28.50,
    val expectedDemandLevel: String = "High",
    val isMarketplaceDelivery: Boolean = type == DriverHotspotType.MARKETPLACE_ITEM || type == DriverHotspotType.PERSON_TO_PERSON,
    val isScheduled: Boolean = false,
    val scheduledTimeText: String = "Immediate",
    val gasPrice: Double? = null,
    val gasStationBrand: String? = null
)

/**
 * Driver Compliance Document Item
 */
enum class ComplianceStatus(val label: String, val colorHex: Long) {
    VERIFIED("VERIFIED", 0xFF16A34A),
    EXPIRING_SOON("EXPIRING SOON", 0xFFD97706),
    ACTION_REQUIRED("ACTION REQUIRED", 0xFFEA580C),
    EXPIRED("EXPIRED", 0xFFDC2626)
}

data class DriverDocumentItem(
    val id: String,
    val title: String,
    val category: String,
    val status: ComplianceStatus,
    val expirationDate: String,
    val documentNumber: String,
    val description: String
)

/**
 * Driver Registered Vehicle
 */
data class DriverVehicle(
    val id: String = UUID.randomUUID().toString(),
    val make: String,
    val model: String,
    val year: String,
    val color: String,
    val licensePlate: String,
    val vehicleType: String, // Sedan, SUV, Pickup Truck, Cargo Van, Hatchback
    val cargoCapacity: String, // Compact (1-2 bags), Medium (Sedan trunk), Large (SUV/Truck), Extra Large (Van)
    val isPrimary: Boolean = true,
    val registrationStatus: ComplianceStatus = ComplianceStatus.VERIFIED,
    val insuranceStatus: ComplianceStatus = ComplianceStatus.VERIFIED,
    val photoUrl: String = ""
)

/**
 * Driver Performance & Platinum Tier Stats
 */
data class DriverPerformance(
    val rating: Double = 4.98,
    val completedDeliveriesCount: Int = 482,
    val acceptanceRatePercent: Int = 94,
    val completionRatePercent: Int = 99,
    val onTimePercentage: Int = 98,
    val driverLevel: String = "PLATINUM COURIER",
    val currentStreakDays: Int = 14,
    val customerCompliments: List<String> = listOf(
        "⚡ Super Fast Delivery",
        "📦 Perfect Condition Handling",
        "💬 Friendly & Responsive",
        "🎯 Exact Drop-off Location",
        "🌟 Above and Beyond"
    )
)

/**
 * Safety & SOS Incident Report
 */
data class SafetyIncidentReport(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val incidentType: String,
    val orderId: String? = null,
    val description: String,
    val isEmergency911: Boolean = false,
    val status: String = "DISPATCH_NOTIFIED"
)

/**
 * Engine that generates realistic driver opportunity hotspots on Google Maps
 */
object DriverOpportunityEngine {

    fun generateOpportunityHotspotsFromOrders(orders: List<Order>): List<DriverOpportunityHotspot> {
        val hotspots = mutableListOf<DriverOpportunityHotspot>()
        if (orders.isNotEmpty()) {
            orders.forEachIndexed { index, order ->
                val hotspotType = if (order.storeOrSellerName.contains("Grocery", ignoreCase = true) || 
                                      order.storeOrSellerName.contains("Walmart", ignoreCase = true) ||
                                      order.storeOrSellerName.contains("Market", ignoreCase = true)) {
                    DriverHotspotType.STORE_ORDER
                } else {
                    DriverHotspotType.RESTAURANT_ORDER
                }

                hotspots.add(
                    DriverOpportunityHotspot(
                        id = "real_hotspot_${order.id}",
                        title = "🚀 Live Order: ${order.storeOrSellerName} ($${String.format(Locale.US, "%.2f", order.total)})",
                        type = hotspotType,
                        latLng = LatLng(37.7749 + (index * 0.002), -122.4194 - (index * 0.0015)),
                        payAmount = order.total * 0.25 + 12.50,
                        distanceMiles = 1.2 + (index * 0.4),
                        estimatedMins = 12 + (index * 3),
                        pickupAddress = order.pickupAddress,
                        dropoffAddress = order.dropoffAddress,
                        storeOrSellerName = order.storeOrSellerName,
                        customerName = order.customerName,
                        itemCount = order.itemsSummary.split(",").size.coerceAtLeast(1),
                        itemSummary = order.itemsSummary,
                        specialInstructions = order.deliveryInstructions.ifBlank { "Real-time online order placed via ShopSafe." },
                        isSurge = order.total > 40.0,
                        isHighDemandZone = false,
                        demandOrdersAvailable = 1,
                        stopsCount = 1
                    )
                )
            }
        }
        hotspots.addAll(generateMapAmenitiesAndSurgeZones())
        return hotspots
    }

    /**
     * Map hotspots visible to driver: ONLY Demand Surge Zones and Driver Amenities (Gas, Hub).
     * Individual orders are NOT shown on the map for drivers to pick; instead orders are dispatched automatically via pings.
     */
    fun generateOpportunityHotspots(): List<DriverOpportunityHotspot> {
        return generateMapAmenitiesAndSurgeZones()
    }

    fun generateMapAmenitiesAndSurgeZones(): List<DriverOpportunityHotspot> {
        return listOf(
            // 1. High-Demand Surge Bonus Zone
            DriverOpportunityHotspot(
                id = "hotspot_surge_1",
                title = "🔥 High Demand Surge Zone (+$3.00 Bonus)",
                type = DriverHotspotType.HIGH_DEMAND_SURGE,
                latLng = LatLng(37.7876, -122.4093),
                payAmount = 29.00,
                distanceMiles = 1.1,
                estimatedMins = 5,
                storeOrSellerName = "Downtown / Union Square Surge Hub",
                pickupAddress = "Union Square Delivery Corridor",
                dropoffAddress = "Surrounding SF Metro Area",
                itemCount = 14,
                itemSummary = "14 Live Orders Waiting • High Tip Density",
                requiredVehicleType = "Any",
                specialInstructions = "High order volume in this 1-mile radius! Extra $3.00 bonus added to all completed trips.",
                isSurge = true,
                isHighDemandZone = true,
                demandOrdersAvailable = 14,
                demandTypicalHourlyPay = 34.50,
                expectedDemandLevel = "Very High (Peak Surge)",
                stopsCount = 1
            ),

            // 2. Mission District Surge Zone
            DriverOpportunityHotspot(
                id = "hotspot_surge_2",
                title = "🔥 Mission Food Surge (+$2.50 Bonus)",
                type = DriverHotspotType.HIGH_DEMAND_SURGE,
                latLng = LatLng(37.7600, -122.4190),
                payAmount = 26.50,
                distanceMiles = 1.8,
                estimatedMins = 8,
                storeOrSellerName = "Mission Culinary Corridor",
                pickupAddress = "Valencia & 16th St Hub",
                dropoffAddress = "SF Central Corridor",
                itemCount = 9,
                itemSummary = "Active Dinner Rush • Rapid Dispatching",
                requiredVehicleType = "Any",
                isSurge = true,
                isHighDemandZone = true,
                demandOrdersAvailable = 9,
                demandTypicalHourlyPay = 31.00,
                expectedDemandLevel = "High Demand",
                stopsCount = 1
            ),

            // 3. Gas Station Amenity Stop
            DriverOpportunityHotspot(
                id = "hotspot_gas_1",
                title = "⛽ Chevron Fast Fuel ($4.39/gal)",
                type = DriverHotspotType.GAS_STATION,
                latLng = LatLng(37.7698, -122.4468),
                payAmount = 0.0,
                distanceMiles = 0.8,
                estimatedMins = 3,
                storeOrSellerName = "Chevron Fuel & Car Wash",
                pickupAddress = "1298 Castro St, San Francisco, CA",
                dropoffAddress = "Driver Convenience Hub",
                gasPrice = 4.39,
                gasStationBrand = "Chevron (ShopSafe 5% Cashback with Safe Card)",
                itemSummary = "Lowest fuel prices nearby + Clean Restrooms + Air Pump",
                stopsCount = 1
            ),

            // 4. Driver Support & Resource Center
            DriverOpportunityHotspot(
                id = "hotspot_sup_1",
                title = "🛡️ ShopSafe Driver Support Center",
                type = DriverHotspotType.DRIVER_SUPPORT,
                latLng = LatLng(37.7901, -122.4009),
                payAmount = 0.0,
                distanceMiles = 1.4,
                estimatedMins = 6,
                storeOrSellerName = "ShopSafe Courier Support & Resource Hub",
                pickupAddress = "500 Howard St, Suite 200, San Francisco, CA",
                dropoffAddress = "Driver Operations Desk",
                itemSummary = "Free Thermal Delivery Bags + Safe Card Replacements + Live Agent Help",
                stopsCount = 1
            )
        )
    }

    /**
     * Dispatch Ping Offers Engine:
     * Generates nearby order pings that are automatically offered to the closest driver.
     * Contains pay offer, description, distance, customer's name, and coordinates for pickup/dropoff map view.
     */
    fun generateNearbyDispatchOffers(): List<DriverOffer> {
        return listOf(
            // Offer 1: Grocery Delivery
            DriverOffer(
                id = "ping_ord_101",
                storeName = "Walmart Supercenter #2041",
                customerName = "Sarah Jenkins",
                pickupAddress = "100 Mission St, San Francisco, CA",
                dropoffAddress = "450 Post St, Apt 8B, San Francisco, CA",
                payAmount = 24.50,
                basePay = 16.00,
                tipAmount = 6.50,
                bonusAmount = 2.00,
                distanceMiles = 3.2,
                estimatedMins = 18,
                itemDetails = "4 Grocery Bins (Organic Produce, Milk, Bakery, Household Essentials)",
                isShopSafeStorefrontShopping = false,
                orderId = "SS-ORD-9021",
                deliveryInstructions = "Please leave at front door and ring bell. Guard at desk will grant elevator access.",
                surgeMultiplier = 1.25,
                surgeBonusAmount = 2.00,
                surgeZoneName = "Downtown SF Surge",
                pickupLat = 37.7749,
                pickupLng = -122.4194,
                dropoffLat = 37.7885,
                dropoffLng = -122.3995,
                offerExpiresInSeconds = 45
            ),

            // Offer 2: Restaurant Food Delivery
            DriverOffer(
                id = "ping_ord_102",
                storeName = "In-N-Out Burger & Fries",
                customerName = "David Chen",
                pickupAddress = "333 Jefferson St, San Francisco, CA",
                dropoffAddress = "780 Montgomery St, San Francisco, CA",
                payAmount = 18.75,
                basePay = 12.00,
                tipAmount = 5.25,
                bonusAmount = 1.50,
                distanceMiles = 2.1,
                estimatedMins = 14,
                itemDetails = "2x Double-Double Meals, 2x Animal Fries, 2x Chocolate Shakes",
                isShopSafeStorefrontShopping = false,
                orderId = "SS-ORD-8842",
                deliveryInstructions = "Keep food hot in insulated thermal bag. Hand directly to customer in lobby.",
                surgeMultiplier = 1.15,
                surgeBonusAmount = 1.50,
                surgeZoneName = "Fisherman's Wharf Rush",
                pickupLat = 37.7638,
                pickupLng = -122.4198,
                dropoffLat = 37.7932,
                dropoffLng = -122.4055,
                offerExpiresInSeconds = 45
            ),

            // Offer 3: Local Bakery & Artisan Goods
            DriverOffer(
                id = "ping_ord_103",
                storeName = "Artisan Bakery & Coffee Co.",
                customerName = "Elena Rostova",
                pickupAddress = "520 Hayes St, San Francisco, CA",
                dropoffAddress = "1200 California St, San Francisco, CA",
                payAmount = 21.00,
                basePay = 14.50,
                tipAmount = 5.00,
                bonusAmount = 1.50,
                distanceMiles = 2.8,
                estimatedMins = 16,
                itemDetails = "1x Custom Two-Tier Velvet Cake & Pastry Gift Box",
                isShopSafeStorefrontShopping = false,
                orderId = "SS-ORD-7719",
                deliveryInstructions = "Fragile specialty cake! Please place flat on backseat floor.",
                surgeMultiplier = 1.0,
                pickupLat = 37.7758,
                pickupLng = -122.4312,
                dropoffLat = 37.7915,
                dropoffLng = -122.4155,
                offerExpiresInSeconds = 45
            ),

            // Offer 4: Marketplace Item Pickup (P2P)
            DriverOffer(
                id = "ping_ord_104",
                storeName = "Seller: Mark V. (ShopSafe Verified)",
                customerName = "Jessica Wong (Buyer)",
                pickupAddress = "240 2nd St, San Francisco, CA",
                dropoffAddress = "1850 Funston Ave, San Francisco, CA",
                payAmount = 34.00,
                basePay = 24.00,
                tipAmount = 7.00,
                bonusAmount = 3.00,
                distanceMiles = 5.4,
                estimatedMins = 26,
                itemDetails = "Sony 65\" 4K Bravia Smart OLED TV (Boxed)",
                isShopSafeStorefrontShopping = false,
                orderId = "SS-P2P-5521",
                deliveryInstructions = "Large boxed electronics (~42 lbs). Seller will assist loading into trunk/backseat.",
                surgeMultiplier = 1.3,
                surgeBonusAmount = 3.00,
                surgeZoneName = "Marketplace Heavy Item",
                pickupLat = 37.7813,
                pickupLng = -122.4011,
                dropoffLat = 37.7548,
                dropoffLng = -122.4477,
                offerExpiresInSeconds = 45
            ),

            // Offer 5: Express Pharmacy & Essentials
            DriverOffer(
                id = "ping_ord_105",
                storeName = "Walgreens Pharmacy & Care",
                customerName = "Marcus Thompson",
                pickupAddress = "135 4th St, San Francisco, CA",
                dropoffAddress = "900 Bush St, Apt 302, San Francisco, CA",
                payAmount = 19.50,
                basePay = 13.50,
                tipAmount = 6.00,
                bonusAmount = 0.0,
                distanceMiles = 1.8,
                estimatedMins = 12,
                itemDetails = "Sealed Prescription Package + Vitamin C & Sanitizer",
                isShopSafeStorefrontShopping = false,
                orderId = "SS-ORD-6632",
                deliveryInstructions = "Direct handoff required. Customer will show 4-digit security PIN.",
                surgeMultiplier = 1.0,
                pickupLat = 37.7845,
                pickupLng = -122.4048,
                dropoffLat = 37.7898,
                dropoffLng = -122.4128,
                offerExpiresInSeconds = 45
            )
        )
    }

    fun getSampleComplianceDocuments(): List<DriverDocumentItem> {
        return listOf(
            DriverDocumentItem(
                id = "doc_1",
                title = "Driver's License (Class C)",
                category = "Identification",
                status = ComplianceStatus.VERIFIED,
                expirationDate = "Oct 14, 2028",
                documentNumber = "CA-D9841209",
                description = "Valid California state driver's license with clear photo."
            ),
            DriverDocumentItem(
                id = "doc_2",
                title = "Auto Insurance Card",
                category = "Vehicle Compliance",
                status = ComplianceStatus.EXPIRING_SOON,
                expirationDate = "Sep 30, 2026",
                documentNumber = "GEICO-POL-884192",
                description = "Active commercial/rideshare endorsement policy."
            ),
            DriverDocumentItem(
                id = "doc_3",
                title = "Vehicle Registration",
                category = "Vehicle Compliance",
                status = ComplianceStatus.VERIFIED,
                expirationDate = "Dec 15, 2027",
                documentNumber = "REG-CA-7XYZ89",
                description = "Registered to current driver under state DMV."
            ),
            DriverDocumentItem(
                id = "doc_4",
                title = "Checkr Criminal & MVR Background",
                category = "Safety Compliance",
                status = ComplianceStatus.VERIFIED,
                expirationDate = "Annual Renewal: May 2027",
                documentNumber = "CHECKR-APPROVED-9941",
                description = "Comprehensive motor vehicle and safety background approved."
            )
        )
    }

    fun getSampleVehicles(): List<DriverVehicle> {
        return listOf(
            DriverVehicle(
                id = "veh_1",
                make = "Toyota",
                model = "Camry SE",
                year = "2022",
                color = "Midnight Blue",
                licensePlate = "7XYZ89",
                vehicleType = "Sedan",
                cargoCapacity = "Medium (Large trunk + backseat)",
                isPrimary = true,
                registrationStatus = ComplianceStatus.VERIFIED,
                insuranceStatus = ComplianceStatus.VERIFIED
            ),
            DriverVehicle(
                id = "veh_2",
                make = "Ford",
                model = "F-150 SuperCrew",
                year = "2021",
                color = "Oxford White",
                licensePlate = "8TRK44",
                vehicleType = "Pickup Truck",
                cargoCapacity = "Extra Large (6.5ft bed for Marketplace furniture/appliances)",
                isPrimary = false,
                registrationStatus = ComplianceStatus.VERIFIED,
                insuranceStatus = ComplianceStatus.EXPIRING_SOON
            )
        )
    }
}
