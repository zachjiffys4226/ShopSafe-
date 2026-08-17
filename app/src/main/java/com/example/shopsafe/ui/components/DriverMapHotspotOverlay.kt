package com.example.shopsafe.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopsafe.data.models.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

/**
 * Driver Map Filter Category
 */
enum class DriverMapFilter(val label: String, val icon: String) {
    ALL("All", "🗺️"),
    FOOD("Food", "🍔"),
    RETAIL("Retail", "🛒"),
    GROCERY("Grocery", "🥦"),
    MARKETPLACE("Marketplace", "📦"),
    P2P("Person-to-Person", "🤝"),
    NEARBY("Nearby (<3mi)", "📍"),
    HIGHEST_PAY("High Pay (>$20)", "💰"),
    SURGE("Surge (+Bonus)", "🔥"),
    MULTI_STOP("Multi-Stop", "🔀")
}

@Composable
fun DriverInteractiveGoogleMap(
    modifier: Modifier = Modifier,
    isOnline: Boolean,
    hotspots: List<DriverOpportunityHotspot>,
    selectedHotspot: DriverOpportunityHotspot?,
    selectedFilter: DriverMapFilter,
    activeOffer: DriverOffer?,
    deliveryStep: Int,
    isLowPowerMode: Boolean = false,
    driverTripProgress: Float = 0.05f,
    isInAppNavigation: Boolean = false,
    onHotspotClick: (DriverOpportunityHotspot) -> Unit,
    onMapClick: () -> Unit,
    onRecenterClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // San Francisco Hub Center
    val sfDefaultCenter = remember { LatLng(37.7749, -122.4194) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(sfDefaultCenter, 13.0f)
    }

    // Driver origin coordinate
    val driverStartLoc = remember { LatLng(37.7790, -122.4150) }

    // Resolved Coordinates for Pickup and Drop-off
    val pickupCoord = remember(activeOffer) {
        if (activeOffer != null) {
            if (activeOffer.pickupLat != 0.0 && activeOffer.pickupLng != 0.0) {
                LatLng(activeOffer.pickupLat, activeOffer.pickupLng)
            } else {
                DeliveryRouteEngine.resolveStoreLatLng(activeOffer.storeName, activeOffer.pickupAddress)
            }
        } else {
            sfDefaultCenter
        }
    }

    val dropoffCoord = remember(activeOffer) {
        if (activeOffer != null) {
            if (activeOffer.dropoffLat != 0.0 && activeOffer.dropoffLng != 0.0) {
                LatLng(activeOffer.dropoffLat, activeOffer.dropoffLng)
            } else {
                DeliveryRouteEngine.resolveDropoffLatLng(activeOffer.dropoffAddress)
            }
        } else {
            sfDefaultCenter
        }
    }

    // Dynamic Interpolated Driver Vehicle Location along the active leg
    val currentDriverLoc = remember(activeOffer, deliveryStep, driverTripProgress) {
        if (activeOffer != null) {
            val progress = driverTripProgress.coerceIn(0.0f, 1.0f)
            if (deliveryStep == 1) {
                // Leg 1: Driver -> Pickup Store
                val lat = driverStartLoc.latitude + (pickupCoord.latitude - driverStartLoc.latitude) * progress
                val lng = driverStartLoc.longitude + (pickupCoord.longitude - driverStartLoc.longitude) * progress
                LatLng(lat, lng)
            } else {
                // Leg 2: Store -> Customer Drop-off
                val lat = pickupCoord.latitude + (dropoffCoord.latitude - pickupCoord.latitude) * progress
                val lng = pickupCoord.longitude + (dropoffCoord.longitude - pickupCoord.longitude) * progress
                LatLng(lat, lng)
            }
        } else {
            driverStartLoc
        }
    }

    // Filter hotspots based on selected chip
    val filteredHotspots = remember(hotspots, selectedFilter) {
        when (selectedFilter) {
            DriverMapFilter.ALL -> hotspots
            DriverMapFilter.FOOD -> hotspots.filter { it.type == DriverHotspotType.RESTAURANT_ORDER }
            DriverMapFilter.RETAIL, DriverMapFilter.GROCERY -> hotspots.filter { it.type == DriverHotspotType.STORE_ORDER }
            DriverMapFilter.MARKETPLACE -> hotspots.filter { it.type == DriverHotspotType.MARKETPLACE_ITEM }
            DriverMapFilter.P2P -> hotspots.filter { it.type == DriverHotspotType.PERSON_TO_PERSON }
            DriverMapFilter.NEARBY -> hotspots.filter { it.distanceMiles <= 3.0 }
            DriverMapFilter.HIGHEST_PAY -> hotspots.filter { it.payAmount >= 20.0 }
            DriverMapFilter.SURGE -> hotspots.filter { it.isSurge || it.type == DriverHotspotType.HIGH_DEMAND_SURGE }
            DriverMapFilter.MULTI_STOP -> hotspots.filter { it.stopsCount > 2 || it.type == DriverHotspotType.PERSON_TO_PERSON }
        }
    }

    // Camera animation when selected hotspot changes
    LaunchedEffect(selectedHotspot) {
        if (selectedHotspot != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(selectedHotspot.latLng, 14.5f),
                durationMs = 600
            )
        }
    }

    // Smooth In-App 3D Navigation Camera tracking driver vehicle
    LaunchedEffect(currentDriverLoc, isInAppNavigation, activeOffer) {
        if (activeOffer != null && isInAppNavigation) {
            val targetDest = if (deliveryStep == 1) pickupCoord else dropoffCoord
            val bearing = calculateBearing(currentDriverLoc, targetDest)
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder()
                            .target(currentDriverLoc)
                            .zoom(16.5f)
                            .tilt(38f)
                            .bearing(bearing)
                            .build()
                    ),
                    durationMs = 800
                )
            } catch (_: Exception) {}
        } else if (activeOffer != null && !isInAppNavigation) {
            try {
                val boundsBuilder = LatLngBounds.builder()
                boundsBuilder.include(currentDriverLoc)
                boundsBuilder.include(pickupCoord)
                boundsBuilder.include(dropoffCoord)
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 90),
                    durationMs = 600
                )
            } catch (_: Exception) {}
        }
    }

    // Map properties and UI Settings
    val mapProperties by remember(isLowPowerMode) {
        mutableStateOf(
            MapProperties(
                isTrafficEnabled = !isLowPowerMode,
                mapType = MapType.NORMAL,
                isMyLocationEnabled = false,
                mapStyleOptions = MapStyleOptions(SLEEK_DARK_MAP_STYLE)
            )
        )
    }

    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
                rotationGesturesEnabled = true,
                scrollGesturesEnabled = true,
                tiltGesturesEnabled = true,
                zoomGesturesEnabled = true
            )
        )
    }

    val isPlayServicesAvailable = remember(context) {
        try {
            val availability = GoogleApiAvailability.getInstance()
            val result = availability.isGooglePlayServicesAvailable(context)
            result == ConnectionResult.SUCCESS
        } catch (_: Throwable) {
            false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isPlayServicesAvailable) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings,
                onMapClick = { onMapClick() }
            ) {
                // 1. Render Surge / High Demand Heatmap Circles (Only when no active delivery)
                if (activeOffer == null) {
                    hotspots.filter { it.isSurge || it.type == DriverHotspotType.HIGH_DEMAND_SURGE }.forEach { surgeSpot ->
                        Circle(
                            center = surgeSpot.latLng,
                            radius = 900.0, // meters
                            fillColor = Color(0x33DC2626),
                            strokeColor = Color(0x99DC2626),
                            strokeWidth = 3f
                        )
                    }
                }

                // 2. Active Delivery Route Polylines (if an order is currently active)
                if (activeOffer != null) {
                    val activeRoute = remember(activeOffer) {
                        DeliveryRouteEngine.calculateOptimizedRoute(
                            storeName = activeOffer.storeName,
                            pickupAddress = activeOffer.pickupAddress,
                            dropoffAddress = activeOffer.dropoffAddress
                        )
                    }

                    // Multi-stop Polyline
                    Polyline(
                        points = listOf(driverStartLoc, pickupCoord, dropoffCoord),
                        color = Color(0xFF10B981),
                        width = 14f
                    )

                    // Pickup Stop Marker
                    Marker(
                        state = MarkerState(position = pickupCoord),
                        title = "Pickup: ${activeOffer.storeName}",
                        snippet = activeOffer.pickupAddress,
                        icon = rememberCustomMarkerIcon(label = "1. PICKUP", colorInt = 0xFF0284C7.toInt())
                    )

                    // Drop-off Stop Marker
                    Marker(
                        state = MarkerState(position = dropoffCoord),
                        title = "Drop-off: ${activeOffer.customerName}",
                        snippet = activeOffer.dropoffAddress,
                        icon = rememberCustomMarkerIcon(label = "2. DROP-OFF", colorInt = 0xFF16A34A.toInt())
                    )

                    // Driver Live Moving Position Marker
                    Marker(
                        state = MarkerState(position = currentDriverLoc),
                        title = "You (ShopSafe Courier)",
                        snippet = if (deliveryStep == 1) "En Route to Pickup (${(driverTripProgress * 100).toInt()}%)" else "En Route to Customer (${(driverTripProgress * 100).toInt()}%)",
                        icon = rememberDriverVehicleIcon()
                    )
                } else {
                    // Persistent Driver Current Location Marker (Alex Rivera)
                    Marker(
                        state = MarkerState(position = driverStartLoc),
                        title = "Your Location",
                        snippet = if (isOnline) "🟢 ONLINE • Searching for orders" else "⚪ OFFLINE",
                        icon = rememberDriverVehicleIcon()
                    )
                }

                // 3. Render Interactive Opportunity Hotspot Pins (Only when no active delivery)
                if (activeOffer == null) {
                    filteredHotspots.forEach { hotspot ->
                        val isSelected = selectedHotspot?.id == hotspot.id
                        val markerIcon = rememberHotspotMarkerIcon(
                            hotspot = hotspot,
                            isSelected = isSelected
                        )

                        Marker(
                            state = MarkerState(position = hotspot.latLng),
                            title = hotspot.title,
                            snippet = "${hotspot.storeOrSellerName} • ${hotspot.distanceMiles} mi",
                            icon = markerIcon,
                            onClick = {
                                onHotspotClick(hotspot)
                                true
                            }
                        )
                    }
                }
            }
        } else {
            // Interactive Fallback Vector Map Canvas
            val fallbackRoute = remember(activeOffer) {
                DeliveryRouteEngine.calculateOptimizedRoute(
                    storeName = activeOffer?.storeName ?: "ShopSafe Hub",
                    pickupAddress = activeOffer?.pickupAddress ?: "100 Mission St",
                    dropoffAddress = activeOffer?.dropoffAddress ?: "450 Post St"
                )
            }
            InteractiveFallbackVectorMap(
                modifier = Modifier.fillMaxSize(),
                optimizedRoute = fallbackRoute,
                selectedOptimizationMode = RouteOptimizationMode.FASTEST_TRAFFIC,
                showDriverMarker = true,
                driverName = "ShopSafe Courier"
            )
        }
    }
}

private fun calculateBearing(from: LatLng, to: LatLng): Float {
    val lat1 = Math.toRadians(from.latitude)
    val lng1 = Math.toRadians(from.longitude)
    val lat2 = Math.toRadians(to.latitude)
    val lng2 = Math.toRadians(to.longitude)
    val dLng = lng2 - lng1
    val y = Math.sin(dLng) * Math.cos(lat2)
    val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLng)
    var brng = Math.toDegrees(Math.atan2(y, x)).toFloat()
    return (brng + 360f) % 360f
}

/**
 * Creates custom bitmap icon for driver vehicle with direction indicator
 */
@Composable
fun rememberDriverVehicleIcon(): BitmapDescriptor {
    return remember {
        val size = 96
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Outer glow pulse
        val glowPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb(70, 59, 130, 246)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, glowPaint)

        // Inner circle
        val innerPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(37, 99, 235)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2.8f, innerPaint)

        // White border
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2.8f, borderPaint)

        // Vehicle navigation arrow indicator
        val arrowPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        val path = android.graphics.Path().apply {
            moveTo(size / 2f, size * 0.28f)
            lineTo(size * 0.65f, size * 0.65f)
            lineTo(size / 2f, size * 0.55f)
            lineTo(size * 0.35f, size * 0.65f)
            close()
        }
        canvas.drawPath(path, arrowPaint)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

/**
 * Creates high-contrast custom bitmap badge icons for opportunity hotspots
 */
@Composable
fun rememberHotspotMarkerIcon(
    hotspot: DriverOpportunityHotspot,
    isSelected: Boolean
): BitmapDescriptor {
    return remember(hotspot.id, hotspot.payAmount, isSelected) {
        val width = if (isSelected) 140 else 120
        val height = if (isSelected) 90 else 80
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val primaryColor = hotspot.type.colorHex.toInt()

        // Background pill
        val pillRect = RectF(6f, 6f, width - 6f, height - 16f)
        val pillPaint = Paint().apply {
            isAntiAlias = true
            color = primaryColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(pillRect, 20f, 20f, pillPaint)

        // Border (Gold if selected or surge, White otherwise)
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = if (isSelected) android.graphics.Color.rgb(255, 215, 0) else android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = if (isSelected) 6f else 4f
        }
        canvas.drawRoundRect(pillRect, 20f, 20f, borderPaint)

        // Pointer triangle at bottom
        val pointerPath = android.graphics.Path().apply {
            moveTo(width / 2f - 12f, height - 16f)
            lineTo(width / 2f + 12f, height - 16f)
            lineTo(width / 2f, height - 2f)
            close()
        }
        canvas.drawPath(pointerPath, pillPaint)

        // Text: Price or Badge
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = if (hotspot.payAmount > 0) 30f else 24f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        val label = if (hotspot.payAmount > 0) {
            "$${String.format(java.util.Locale.US, "%.2f", hotspot.payAmount)}"
        } else if (hotspot.type == DriverHotspotType.GAS_STATION) {
            "⛽ $${hotspot.gasPrice ?: 4.39}"
        } else if (hotspot.type == DriverHotspotType.HIGH_DEMAND_SURGE) {
            "🔥 +$3"
        } else {
            "🛡️ HUB"
        }

        canvas.drawText(label, width / 2f, (height - 16f) / 2f + 10f, textPaint)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

/**
 * Custom pickup / dropoff marker icon
 */
@Composable
fun rememberCustomMarkerIcon(label: String, colorInt: Int): BitmapDescriptor {
    return remember(label, colorInt) {
        val width = 160
        val height = 70
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val pillRect = RectF(4f, 4f, width - 4f, height - 14f)
        val pillPaint = Paint().apply {
            isAntiAlias = true
            color = colorInt
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(pillRect, 16f, 16f, pillPaint)

        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(pillRect, 16f, 16f, borderPaint)

        val textPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 22f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(label, width / 2f, (height - 14f) / 2f + 8f, textPaint)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

private const val SLEEK_DARK_MAP_STYLE = """
[
  { "elementType": "geometry", "stylers": [ { "color": "#18202d" } ] },
  { "elementType": "labels.text.fill", "stylers": [ { "color": "#8ec3b9" } ] },
  { "elementType": "labels.text.stroke", "stylers": [ { "color": "#1a3646" } ] },
  { "featureType": "administrative.country", "elementType": "geometry.stroke", "stylers": [ { "color": "#4b6878" } ] },
  { "featureType": "administrative.land_parcel", "elementType": "labels.text.fill", "stylers": [ { "color": "#64779e" } ] },
  { "featureType": "administrative.province", "elementType": "geometry.stroke", "stylers": [ { "color": "#4b6878" } ] },
  { "featureType": "landscape.man_made", "elementType": "geometry.stroke", "stylers": [ { "color": "#334e68" } ] },
  { "featureType": "landscape.natural", "elementType": "geometry", "stylers": [ { "color": "#021019" } ] },
  { "featureType": "poi", "elementType": "geometry", "stylers": [ { "color": "#283d6a" } ] },
  { "featureType": "poi", "elementType": "labels.text.fill", "stylers": [ { "color": "#6f9ba5" } ] },
  { "featureType": "poi", "elementType": "labels.text.stroke", "stylers": [ { "color": "#1d2c4d" } ] },
  { "featureType": "poi.park", "elementType": "geometry.fill", "stylers": [ { "color": "#023e58" } ] },
  { "featureType": "poi.park", "elementType": "labels.text.fill", "stylers": [ { "color": "#3C7680" } ] },
  { "featureType": "road", "elementType": "geometry", "stylers": [ { "color": "#304a7d" } ] },
  { "featureType": "road", "elementType": "labels.text.fill", "stylers": [ { "color": "#98a5be" } ] },
  { "featureType": "road", "elementType": "labels.text.stroke", "stylers": [ { "color": "#1d2c4d" } ] },
  { "featureType": "road.highway", "elementType": "geometry", "stylers": [ { "color": "#2c6693" } ] },
  { "featureType": "road.highway", "elementType": "geometry.stroke", "stylers": [ { "color": "#255779" } ] },
  { "featureType": "road.highway", "elementType": "labels.text.fill", "stylers": [ { "color": "#b0d5ce" } ] },
  { "featureType": "road.highway", "elementType": "labels.text.stroke", "stylers": [ { "color": "#023e58" } ] },
  { "featureType": "transit", "elementType": "labels.text.fill", "stylers": [ { "color": "#98a5be" } ] },
  { "featureType": "transit", "elementType": "labels.text.stroke", "stylers": [ { "color": "#1d2c4d" } ] },
  { "featureType": "transit.line", "elementType": "geometry.fill", "stylers": [ { "color": "#283d6a" } ] },
  { "featureType": "transit.station", "elementType": "geometry", "stylers": [ { "color": "#3a4762" } ] },
  { "featureType": "water", "elementType": "geometry", "stylers": [ { "color": "#0e1626" } ] },
  { "featureType": "water", "elementType": "labels.text.fill", "stylers": [ { "color": "#4e6d70" } ] }
]
"""
