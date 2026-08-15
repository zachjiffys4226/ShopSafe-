package com.example.shopsafe.data.models

import com.google.android.gms.maps.model.LatLng

data class DemandHotspot(
    val id: String,
    val name: String,
    val latLng: LatLng,
    val intensity: Double, // 0.0 to 1.0 (determines heatmap color & radius opacity)
    val surgeMultiplier: Double, // e.g. 1.5x, 2.4x, 3.2x
    val orderVolumePerHour: Int, // e.g. 52 orders/hr
    val radiusMeters: Double, // e.g. 1200.0 meters
    val dominantCategory: String, // e.g. "Gourmet & Fast Casual", "Grocery & Pharma"
    val peakTimeDescription: String // e.g. "Peak Dinner Rush (6 PM - 9 PM)"
) {
    fun estimatedSurgeBonus(): Double {
        return ((surgeMultiplier - 1.0) * 3.50 + 2.00).coerceAtLeast(1.50)
    }
}

object DemandHeatmapEngine {
    fun generateHotspots(): List<DemandHotspot> {
        return listOf(
            DemandHotspot(
                id = "hotspot_1",
                name = "Financial District / Downtown Core",
                latLng = LatLng(37.7900, -122.4000),
                intensity = 0.95,
                surgeMultiplier = 2.8,
                orderVolumePerHour = 84,
                radiusMeters = 1500.0,
                dominantCategory = "Fast Casual & Coffee",
                peakTimeDescription = "High Lunch & Evening Rush"
            ),
            DemandHotspot(
                id = "hotspot_2",
                name = "SoMa Technology Hub",
                latLng = LatLng(37.7820, -122.4050),
                intensity = 0.88,
                surgeMultiplier = 2.4,
                orderVolumePerHour = 68,
                radiusMeters = 1300.0,
                dominantCategory = "Late Night & Tech Dining",
                peakTimeDescription = "High Order Density"
            ),
            DemandHotspot(
                id = "hotspot_3",
                name = "Mission District Corridor",
                latLng = LatLng(37.7600, -122.4200),
                intensity = 0.82,
                surgeMultiplier = 2.1,
                orderVolumePerHour = 55,
                radiusMeters = 1400.0,
                dominantCategory = "Artisanal & Mexican Grill",
                peakTimeDescription = "Steady Dinner Flow"
            ),
            DemandHotspot(
                id = "hotspot_4",
                name = "Union Square & Theater District",
                latLng = LatLng(37.7879, -122.4075),
                intensity = 0.78,
                surgeMultiplier = 1.9,
                orderVolumePerHour = 48,
                radiusMeters = 1100.0,
                dominantCategory = "Retail & Gourmet",
                peakTimeDescription = "Active Shopping & Dining"
            ),
            DemandHotspot(
                id = "hotspot_5",
                name = "Marina & Waterfront",
                latLng = LatLng(37.8024, -122.4358),
                intensity = 0.65,
                surgeMultiplier = 1.6,
                orderVolumePerHour = 34,
                radiusMeters = 1200.0,
                dominantCategory = "Seafood & Brunch",
                peakTimeDescription = "Weekend & Evening Peak"
            )
        )
    }
}
