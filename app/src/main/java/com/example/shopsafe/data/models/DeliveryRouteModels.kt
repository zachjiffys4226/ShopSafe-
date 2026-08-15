package com.example.shopsafe.data.models

import com.google.android.gms.maps.model.LatLng
import java.util.Locale

enum class RouteOptimizationMode(val displayName: String, val badge: String, val description: String) {
    FASTEST_TRAFFIC("Fastest Route", "⚡ Live Traffic", "Real-time AI traffic rerouting avoiding congestion"),
    ECO_SAVER("Eco-Friendly", "🌿 Fuel Saver", "Optimized speed limits and fewer stoplights to save gas/battery"),
    HIGHWAY_EXPRESS("Direct Highway", "🛣️ Freeway", "Prefers high-speed multi-lane transit corridors")
}

enum class NavigationManeuver {
    START,
    STRAIGHT,
    TURN_LEFT,
    TURN_RIGHT,
    SLIGHT_LEFT,
    SLIGHT_RIGHT,
    MERGE_HIGHWAY,
    ARRIVE_STORE,
    ARRIVE_CUSTOMER
}

data class NavigationStep(
    val instruction: String,
    val distanceMiles: Double,
    val distanceText: String,
    val estimatedMins: Int,
    val maneuver: NavigationManeuver,
    val latLng: LatLng
)

data class DeliveryGeoLocation(
    val title: String,
    val address: String,
    val latLng: LatLng,
    val isStore: Boolean = false
)

data class OptimizedDeliveryRoute(
    val origin: DeliveryGeoLocation,
    val destination: DeliveryGeoLocation,
    val polylinePoints: List<LatLng>,
    val distanceMiles: Double,
    val estimatedDurationMins: Int,
    val trafficDelayMins: Int,
    val trafficStatus: String, // "Light Traffic", "Moderate Traffic", "Optimal Flow"
    val optimizationMode: RouteOptimizationMode,
    val steps: List<NavigationStep>,
    val routeSummary: String,
    val driverPosition: LatLng
)

object DeliveryRouteEngine {

    // Known SF Store / Hub coordinates
    private val STORE_COORDS = mapOf(
        "Safeway" to LatLng(37.7749, -122.4194),
        "ShopSafe" to LatLng(37.7749, -122.4194),
        "Whole Foods" to LatLng(37.7813, -122.4011),
        "Target" to LatLng(37.7844, -122.4042),
        "Trader Joe's" to LatLng(37.7876, -122.4093),
        "Walgreens" to LatLng(37.7698, -122.4468),
        "McDonald's" to LatLng(37.7638, -122.4198),
        "In-N-Out" to LatLng(37.8080, -122.4177),
        "Chipotle" to LatLng(37.7901, -122.4009),
        "Local" to LatLng(37.7758, -122.4312),
        "Corner Market" to LatLng(37.7621, -122.4345)
    )

    // Known customer residential delivery areas
    private val RESIDENTIAL_COORDS = listOf(
        LatLng(37.7885, -122.3995),
        LatLng(37.7932, -122.4055),
        LatLng(37.7612, -122.4285),
        LatLng(37.7548, -122.4477),
        LatLng(37.8005, -122.4390),
        LatLng(37.7720, -122.4610)
    )

    fun resolveStoreLatLng(storeName: String, address: String): LatLng {
        for ((key, coord) in STORE_COORDS) {
            if (storeName.contains(key, ignoreCase = true) || address.contains(key, ignoreCase = true)) {
                return coord
            }
        }
        val hash = ((storeName.hashCode().toLong() xor address.hashCode().toLong()) and 0x7FFFFFFF)
        val latOffset = ((hash % 100).toDouble() / 1000.0) - 0.05
        val lngOffset = (((hash / 100) % 100).toDouble() / 1000.0) - 0.05
        return LatLng(37.7749 + latOffset, -122.4194 + lngOffset)
    }

    fun resolveDropoffLatLng(address: String): LatLng {
        val hash = (address.hashCode().toLong() and 0x7FFFFFFF).toInt()
        val base = RESIDENTIAL_COORDS[hash % RESIDENTIAL_COORDS.size]
        val latJitter = ((hash % 40).toDouble() / 10000.0) - 0.002
        val lngJitter = (((hash / 40) % 40).toDouble() / 10000.0) - 0.002
        return LatLng(base.latitude + latJitter, base.longitude + lngJitter)
    }

    fun calculateOptimizedRoute(
        storeName: String,
        pickupAddress: String,
        dropoffAddress: String,
        mode: RouteOptimizationMode = RouteOptimizationMode.FASTEST_TRAFFIC,
        driverProgress: Float = 0.35f
    ): OptimizedDeliveryRoute {
        val originCoord = resolveStoreLatLng(storeName, pickupAddress)
        val destCoord = resolveDropoffLatLng(dropoffAddress)

        val origin = DeliveryGeoLocation(
            title = storeName,
            address = pickupAddress,
            latLng = originCoord,
            isStore = true
        )

        val destination = DeliveryGeoLocation(
            title = "Customer Delivery Destination",
            address = dropoffAddress,
            latLng = destCoord,
            isStore = false
        )

        val polyline = generateSmoothPolyline(originCoord, destCoord, mode)
        val distanceMiles = calculateApproxDistance(originCoord, destCoord, mode)

        val (durationMins, trafficDelay, trafficStatus) = when (mode) {
            RouteOptimizationMode.FASTEST_TRAFFIC -> Triple((distanceMiles * 3.2).toInt().coerceAtLeast(6), 1, "Optimal Flow • Fast Travel")
            RouteOptimizationMode.ECO_SAVER -> Triple((distanceMiles * 3.8).toInt().coerceAtLeast(8), 0, "Eco Green Flow • 0 Delays")
            RouteOptimizationMode.HIGHWAY_EXPRESS -> Triple((distanceMiles * 2.8).toInt().coerceAtLeast(5), 2, "Highway Speed • Light Congestion")
        }

        val routeSummary = when (mode) {
            RouteOptimizationMode.FASTEST_TRAFFIC -> "via Market St & Van Ness Ave (AI Rerouted)"
            RouteOptimizationMode.ECO_SAVER -> "via Mission Corridor (Low RPM & Stoplight Sync)"
            RouteOptimizationMode.HIGHWAY_EXPRESS -> "via US-101 / I-80 Central Fwy (Fast Lane)"
        }

        val steps = generateTurnByTurnSteps(origin, destination, polyline, mode)

        // Interpolate current driver position along polyline based on progress
        val driverPosition = interpolatePosition(polyline, driverProgress.coerceIn(0.05f, 0.95f))

        return OptimizedDeliveryRoute(
            origin = origin,
            destination = destination,
            polylinePoints = polyline,
            distanceMiles = distanceMiles,
            estimatedDurationMins = durationMins,
            trafficDelayMins = trafficDelay,
            trafficStatus = trafficStatus,
            optimizationMode = mode,
            steps = steps,
            routeSummary = routeSummary,
            driverPosition = driverPosition
        )
    }

    private fun generateSmoothPolyline(
        start: LatLng,
        end: LatLng,
        mode: RouteOptimizationMode
    ): List<LatLng> {
        val points = mutableListOf<LatLng>()
        points.add(start)

        val midLat = (start.latitude + end.latitude) / 2.0
        val midLng = (start.longitude + end.longitude) / 2.0

        when (mode) {
            RouteOptimizationMode.FASTEST_TRAFFIC -> {
                // Multi-segment city grid curve
                points.add(LatLng(start.latitude + (end.latitude - start.latitude) * 0.25, start.longitude))
                points.add(LatLng(start.latitude + (end.latitude - start.latitude) * 0.35, start.longitude + (end.longitude - start.longitude) * 0.2))
                points.add(LatLng(midLat + 0.0015, midLng - 0.0010))
                points.add(LatLng(start.latitude + (end.latitude - start.latitude) * 0.70, end.longitude - (end.longitude - start.longitude) * 0.2))
                points.add(LatLng(end.latitude, end.longitude - (end.longitude - start.longitude) * 0.15))
            }
            RouteOptimizationMode.ECO_SAVER -> {
                // Direct surface streets with gentle cornering
                points.add(LatLng(start.latitude, start.longitude + (end.longitude - start.longitude) * 0.4))
                points.add(LatLng(midLat - 0.001, midLng))
                points.add(LatLng(end.latitude - (end.latitude - start.latitude) * 0.2, end.longitude))
            }
            RouteOptimizationMode.HIGHWAY_EXPRESS -> {
                // Highway arc bypass
                val highwayLatOffset = if (end.latitude > start.latitude) 0.004 else -0.004
                val highwayLngOffset = if (end.longitude > start.longitude) -0.005 else 0.005
                points.add(LatLng(start.latitude + 0.001, start.longitude + 0.001))
                points.add(LatLng(midLat + highwayLatOffset, midLng + highwayLngOffset))
                points.add(LatLng(end.latitude - 0.001, end.longitude - 0.001))
            }
        }

        points.add(end)

        // Subdivide points to produce ultra-smooth polyline rendering on Google Map
        val smoothPoints = mutableListOf<LatLng>()
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            smoothPoints.add(p1)
            // Add 3 intermediate interpolated points
            for (step in 1..3) {
                val fraction = step / 4.0
                smoothPoints.add(
                    LatLng(
                        p1.latitude + (p2.latitude - p1.latitude) * fraction,
                        p1.longitude + (p2.longitude - p1.longitude) * fraction
                    )
                )
            }
        }
        smoothPoints.add(points.last())
        return smoothPoints
    }

    private fun calculateApproxDistance(start: LatLng, end: LatLng, mode: RouteOptimizationMode): Double {
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLng = Math.toRadians(end.longitude - start.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(start.latitude)) * Math.cos(Math.toRadians(end.latitude)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val earthRadiusMiles = 3958.8
        val straightLine = earthRadiusMiles * c

        val roadFactor = when (mode) {
            RouteOptimizationMode.FASTEST_TRAFFIC -> 1.35
            RouteOptimizationMode.ECO_SAVER -> 1.25
            RouteOptimizationMode.HIGHWAY_EXPRESS -> 1.45
        }
        val distance = straightLine * roadFactor
        return String.format(Locale.US, "%.1f", distance.coerceIn(0.8, 15.0)).toDouble()
    }

    private fun generateTurnByTurnSteps(
        origin: DeliveryGeoLocation,
        destination: DeliveryGeoLocation,
        polyline: List<LatLng>,
        mode: RouteOptimizationMode
    ): List<NavigationStep> {
        val steps = mutableListOf<NavigationStep>()

        steps.add(
            NavigationStep(
                instruction = "Depart ${origin.title} onto main avenue",
                distanceMiles = 0.2,
                distanceText = "0.2 mi",
                estimatedMins = 1,
                maneuver = NavigationManeuver.START,
                latLng = polyline.first()
            )
        )

        when (mode) {
            RouteOptimizationMode.FASTEST_TRAFFIC -> {
                steps.add(
                    NavigationStep(
                        instruction = "In 400 ft, turn right onto Market St",
                        distanceMiles = 0.6,
                        distanceText = "0.6 mi",
                        estimatedMins = 2,
                        maneuver = NavigationManeuver.TURN_RIGHT,
                        latLng = polyline.getOrElse(polyline.size / 4) { polyline.first() }
                    )
                )
                steps.add(
                    NavigationStep(
                        instruction = "Use the left 2 lanes to turn left onto Van Ness Ave",
                        distanceMiles = 1.1,
                        distanceText = "1.1 mi",
                        estimatedMins = 4,
                        maneuver = NavigationManeuver.TURN_LEFT,
                        latLng = polyline.getOrElse(polyline.size / 2) { polyline.first() }
                    )
                )
            }
            RouteOptimizationMode.ECO_SAVER -> {
                steps.add(
                    NavigationStep(
                        instruction = "Continue straight along Mission Green Corridor",
                        distanceMiles = 1.4,
                        distanceText = "1.4 mi",
                        estimatedMins = 5,
                        maneuver = NavigationManeuver.STRAIGHT,
                        latLng = polyline.getOrElse(polyline.size / 3) { polyline.first() }
                    )
                )
                steps.add(
                    NavigationStep(
                        instruction = "Turn right onto 16th St (Eco synchronized lights)",
                        distanceMiles = 0.5,
                        distanceText = "0.5 mi",
                        estimatedMins = 2,
                        maneuver = NavigationManeuver.TURN_RIGHT,
                        latLng = polyline.getOrElse(polyline.size * 2 / 3) { polyline.first() }
                    )
                )
            }
            RouteOptimizationMode.HIGHWAY_EXPRESS -> {
                steps.add(
                    NavigationStep(
                        instruction = "Take on-ramp for US-101 / Central Fwy North",
                        distanceMiles = 1.8,
                        distanceText = "1.8 mi",
                        estimatedMins = 3,
                        maneuver = NavigationManeuver.MERGE_HIGHWAY,
                        latLng = polyline.getOrElse(polyline.size / 3) { polyline.first() }
                    )
                )
                steps.add(
                    NavigationStep(
                        instruction = "Take Exit 434A toward Downtown / Civic Center",
                        distanceMiles = 0.4,
                        distanceText = "0.4 mi",
                        estimatedMins = 1,
                        maneuver = NavigationManeuver.SLIGHT_RIGHT,
                        latLng = polyline.getOrElse(polyline.size * 2 / 3) { polyline.first() }
                    )
                )
            }
        }

        steps.add(
            NavigationStep(
                instruction = "Arrive at customer address: ${destination.address}",
                distanceMiles = 0.1,
                distanceText = "150 ft",
                estimatedMins = 1,
                maneuver = NavigationManeuver.ARRIVE_CUSTOMER,
                latLng = polyline.last()
            )
        )

        return steps
    }

    private fun interpolatePosition(points: List<LatLng>, fraction: Float): LatLng {
        if (points.isEmpty()) return LatLng(37.7749, -122.4194)
        if (points.size == 1) return points.first()

        val totalSegments = points.size - 1
        val scaledIndex = fraction * totalSegments
        val index = scaledIndex.toInt().coerceIn(0, totalSegments - 1)
        val remainder = scaledIndex - index

        val p1 = points[index]
        val p2 = points[index + 1]

        return LatLng(
            p1.latitude + (p2.latitude - p1.latitude) * remainder,
            p1.longitude + (p2.longitude - p1.longitude) * remainder
        )
    }
}
