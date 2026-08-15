package com.example.shopsafe.data.ml

import kotlin.math.*

enum class DriverExperienceTier(
    val label: String,
    val minDeliveries: Int,
    val bonusMultiplier: Double
) {
    ROOKIE("Rookie Driver", 0, 1.0),
    PRO("Experienced Pro", 400, 1.15),
    VETERAN("Veteran VIP", 800, 1.30),
    MASTER("Master Elite", 1400, 1.50)
}

data class DriverCandidate(
    val id: String,
    val name: String,
    val vehicleType: String,
    val distanceMiles: Double,
    val historicalRating: Double,
    val acceptanceRate: Double, // e.g. 0.95 (95%)
    val punctualityScore: Double, // e.g. 0.98 (98%)
    val completedDeliveries: Int,
    val currentTrafficDelayMins: Double,
    val orderValue: Double = 125.0
) {
    val experienceTier: DriverExperienceTier
        get() = when {
            completedDeliveries >= 1400 -> DriverExperienceTier.MASTER
            completedDeliveries >= 800 -> DriverExperienceTier.VETERAN
            completedDeliveries >= 400 -> DriverExperienceTier.PRO
            else -> DriverExperienceTier.ROOKIE
        }

    val experienceScore: Double
        get() {
            // Experience score component (max 20 points based on completed deliveries)
            return (completedDeliveries / 1500.0).coerceIn(0.0, 1.0) * 20.0
        }

    val isTopRated: Boolean
        get() = historicalRating >= 4.90

    val isHighValueOrder: Boolean
        get() = orderValue >= 80.0

    val highValuePriorityBonus: Double
        get() {
            return if (isHighValueOrder && isTopRated && completedDeliveries >= 800) {
                val valueFactor = (orderValue / 150.0).coerceIn(0.8, 1.5)
                12.0 * experienceTier.bonusMultiplier * valueFactor
            } else if (isHighValueOrder && (historicalRating < 4.85 || completedDeliveries < 300)) {
                -8.0
            } else {
                0.0
            }
        }

    val mlMatchScore: Double
        get() {
            // ML Weighted Scoring Formula:
            // - Proximity weight: max 25 pts (inverse distance factor)
            // - Historical Rating weight: max 25 pts (top-rated drivers)
            // - Driver Experience Level weight: max 20 pts (completed deliveries & tier)
            // - Reliability weight: max 15 pts (acceptance & punctuality)
            // - Traffic Delay penalty weight: max 15 pts
            // - High-Value Order Experience Alignment Bonus: bonus/penalty based on experience & rating for $80+ orders

            val proximityScore = max(0.0, 25.0 * (1.0 - (distanceMiles / 10.0).coerceIn(0.0, 1.0)))
            val ratingScore = 25.0 * (historicalRating / 5.0)
            val expScore = experienceScore
            val reliabilityScore = 15.0 * ((acceptanceRate + punctualityScore) / 2.0)
            val trafficScore = max(0.0, 15.0 * (1.0 - (currentTrafficDelayMins / 15.0).coerceIn(0.0, 1.0)))

            val baseScore = proximityScore + ratingScore + expScore + reliabilityScore + trafficScore

            return (baseScore + highValuePriorityBonus).coerceIn(0.0, 100.0)
        }
}

object SmartOrderDispatcherEngine {

    fun rankDriversForOrder(
        orderPickupLocation: String,
        baseTrafficCongestionIndex: Double = 1.25, // 1.0 = normal, 1.5 = heavy traffic
        orderValue: Double = 125.0
    ): List<DriverCandidate> {
        val candidates = listOf(
            DriverCandidate(
                id = "driver_alex",
                name = "Alex Rivera (Verified VIP)",
                vehicleType = "Sedan (Toyota Camry)",
                distanceMiles = 0.8,
                historicalRating = 4.98,
                acceptanceRate = 0.96,
                punctualityScore = 0.99,
                completedDeliveries = 1420,
                currentTrafficDelayMins = 1.2 * baseTrafficCongestionIndex,
                orderValue = orderValue
            ),
            DriverCandidate(
                id = "driver_marcus",
                name = "Marcus Vance",
                vehicleType = "Electric SUV (Tesla Model Y)",
                distanceMiles = 1.4,
                historicalRating = 4.92,
                acceptanceRate = 0.91,
                punctualityScore = 0.95,
                completedDeliveries = 890,
                currentTrafficDelayMins = 2.0 * baseTrafficCongestionIndex,
                orderValue = orderValue
            ),
            DriverCandidate(
                id = "driver_elena",
                name = "Elena Rostova",
                vehicleType = "Hybrid Hatchback (Prius)",
                distanceMiles = 2.1,
                historicalRating = 4.95,
                acceptanceRate = 0.94,
                punctualityScore = 0.97,
                completedDeliveries = 1150,
                currentTrafficDelayMins = 2.8 * baseTrafficCongestionIndex,
                orderValue = orderValue
            ),
            DriverCandidate(
                id = "driver_sam",
                name = "Sam Chen",
                vehicleType = "Scooter / Delivery Bike",
                distanceMiles = 0.5,
                historicalRating = 4.82,
                acceptanceRate = 0.85,
                punctualityScore = 0.90,
                completedDeliveries = 210,
                currentTrafficDelayMins = 0.8 * baseTrafficCongestionIndex,
                orderValue = orderValue
            )
        )

        // Sort descending by ML Match Score
        return candidates.sortedByDescending { it.mlMatchScore }
    }
}
