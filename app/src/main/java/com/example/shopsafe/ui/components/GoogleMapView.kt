package com.example.shopsafe.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.Color as AndroidColor
import com.example.shopsafe.data.models.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    pickupTitle: String = "ShopSafe Pick Up Location",
    dropoffTitle: String = "Customer Drop-off Location",
    storeName: String = "ShopSafe Express Hub",
    showDriverMarker: Boolean = true,
    driverName: String = "Alex Rivera (ShopSafe Courier)",
    showTurnByTurnHud: Boolean = true,
    showRouteOptions: Boolean = true,
    isLowPowerMode: Boolean = false,
    showSurgeOverlay: Boolean = true,
    initialSelectedSurgeZoneId: String? = "hotspot_1",
    onSurgeZoneSelected: ((DemandHotspot) -> Unit)? = null,
    onNavigateExternalClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Route Optimization Mode State
    var selectedOptimizationMode by remember { mutableStateOf(RouteOptimizationMode.FASTEST_TRAFFIC) }
    var isTrafficLayerEnabled by remember { mutableStateOf(!isLowPowerMode) }
    var currentMapType by remember { mutableStateOf(MapType.NORMAL) }
    var sleekThemeMode by remember { mutableStateOf(if (isLowPowerMode) SleekMapThemeMode.SLEEK_DARK else SleekMapThemeMode.SLEEK_DARK) }
    var driverProgress by remember { mutableFloatStateOf(0.35f) }
    var isExpandedHud by remember { mutableStateOf(true) }
    var currentStepIndex by remember { mutableIntStateOf(0) }

    // Real-Time Surge Pricing Engine State
    var isSurgeOverlayEnabled by remember { mutableStateOf(showSurgeOverlay) }
    var surgeHotspots by remember { mutableStateOf(DemandHeatmapEngine.generateHotspots()) }
    var selectedSurgeHotspot by remember {
        mutableStateOf<DemandHotspot?>(
            surgeHotspots.find { it.id == initialSelectedSurgeZoneId } ?: surgeHotspots.firstOrNull()
        )
    }
    var surgeCountdown by remember { mutableIntStateOf(42) }
    var isSurgeHudExpanded by remember { mutableStateOf(true) }

    // Dynamic Real-Time Demand Engine Ticker
    LaunchedEffect(isSurgeOverlayEnabled) {
        if (isSurgeOverlayEnabled) {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                if (surgeCountdown > 1) {
                    surgeCountdown--
                } else {
                    surgeCountdown = 45
                    surgeHotspots = surgeHotspots.map { spot ->
                        val volumeVariance = (-3..4).random()
                        val newVolume = (spot.orderVolumePerHour + volumeVariance).coerceIn(25, 120)
                        val newMultiplier = String.format("%.1f", (newVolume / 30.0).coerceIn(1.2, 3.5)).toDouble()
                        spot.copy(
                            orderVolumePerHour = newVolume,
                            surgeMultiplier = newMultiplier
                        )
                    }
                    selectedSurgeHotspot = surgeHotspots.find { it.id == selectedSurgeHotspot?.id } ?: surgeHotspots.firstOrNull()
                }
            }
        }
    }

    LaunchedEffect(isLowPowerMode) {
        if (isLowPowerMode) {
            isTrafficLayerEnabled = false
            sleekThemeMode = SleekMapThemeMode.SLEEK_DARK
        }
    }

    // Calculate current optimized route from store location to customer address
    val optimizedRoute = remember(pickupTitle, dropoffTitle, storeName, selectedOptimizationMode, driverProgress) {
        DeliveryRouteEngine.calculateOptimizedRoute(
            storeName = storeName,
            pickupAddress = pickupTitle,
            dropoffAddress = dropoffTitle,
            mode = selectedOptimizationMode,
            driverProgress = driverProgress
        )
    }

    // Google Maps Camera Position Setup
    val midpointLat = (optimizedRoute.origin.latLng.latitude + optimizedRoute.destination.latLng.latitude) / 2.0
    val midpointLng = (optimizedRoute.origin.latLng.longitude + optimizedRoute.destination.latLng.longitude) / 2.0
    val defaultCenter = LatLng(midpointLat, midpointLng)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 13.5f)
    }

    // Animate camera when mode or addresses change
    LaunchedEffect(optimizedRoute.origin.latLng, optimizedRoute.destination.latLng) {
        try {
            val boundsBuilder = LatLngBounds.builder()
            boundsBuilder.include(optimizedRoute.origin.latLng)
            boundsBuilder.include(optimizedRoute.destination.latLng)
            optimizedRoute.polylinePoints.forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 120),
                durationMs = 800
            )
        } catch (_: Exception) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(defaultCenter, 13.5f),
                durationMs = 800
            )
        }
    }

    val mapProperties by remember(isTrafficLayerEnabled, currentMapType, sleekThemeMode) {
        mutableStateOf(
            MapProperties(
                isTrafficEnabled = isTrafficLayerEnabled,
                mapType = currentMapType,
                isMyLocationEnabled = false,
                mapStyleOptions = when (sleekThemeMode) {
                    SleekMapThemeMode.SLEEK_DARK -> MapStyleOptions(SleekMapStyleUtils.SLEEK_DARK_STYLE_JSON)
                    SleekMapThemeMode.SLEEK_LIGHT -> MapStyleOptions(SleekMapStyleUtils.SLEEK_LIGHT_STYLE_JSON)
                    SleekMapThemeMode.STANDARD -> null
                }
            )
        )
    }

    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            )
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF0F172A))
    ) {
        // Real Google Maps Compose SDK View with fallback Canvas
        var isMapLoaded by remember { mutableStateOf(false) }

        val isPlayServicesAvailable = remember(context) {
            try {
                val availability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
                val result = availability.isGooglePlayServicesAvailable(context)
                result == com.google.android.gms.common.ConnectionResult.SUCCESS
            } catch (_: Throwable) {
                false
            }
        }

        if (isPlayServicesAvailable) {
            val greenMarkerIcon = remember(context) {
                SleekMapStyleUtils.createHighContrastMarker(
                    context = context,
                    mainColorHex = "#059669",
                    ringColorHex = "#FFFFFF",
                    symbolType = MarkerSymbolType.STORE
                )
            }

            val redMarkerIcon = remember(context) {
                SleekMapStyleUtils.createHighContrastMarker(
                    context = context,
                    mainColorHex = "#DC2626",
                    ringColorHex = "#FFFFFF",
                    symbolType = MarkerSymbolType.DROPOFF
                )
            }

            val azureMarkerIcon = remember(context) {
                SleekMapStyleUtils.createHighContrastMarker(
                    context = context,
                    mainColorHex = "#0284C7",
                    ringColorHex = "#FDE047",
                    symbolType = MarkerSymbolType.DRIVER
                )
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings,
                onMapLoaded = { isMapLoaded = true }
            ) {
                // 1. Store Pickup Location Marker (Green Pin)
                Marker(
                    state = MarkerState(position = optimizedRoute.origin.latLng),
                    title = "🛒 Pickup: ${optimizedRoute.origin.title}",
                    snippet = optimizedRoute.origin.address,
                    icon = greenMarkerIcon
                )

                // 2. Customer Delivery Destination Marker (Red Pin)
                Marker(
                    state = MarkerState(position = optimizedRoute.destination.latLng),
                    title = "🏠 Customer Dropoff",
                    snippet = optimizedRoute.destination.address,
                    icon = redMarkerIcon
                )

                // 3. Driver Live Courier Position Marker (Azure Blue)
                if (showDriverMarker) {
                    Marker(
                        state = MarkerState(position = optimizedRoute.driverPosition),
                        title = "🚗 $driverName",
                        snippet = "Live Courier Location • ${optimizedRoute.trafficStatus}",
                        icon = azureMarkerIcon
                    )
                }

                // 4. Optimized Multi-Point Delivery Route Polyline
                val routeColor = when (selectedOptimizationMode) {
                    RouteOptimizationMode.FASTEST_TRAFFIC -> Color(0xFF1A73E8) // Google Maps Navigation Blue
                    RouteOptimizationMode.ECO_SAVER -> Color(0xFF16A34A)       // Eco Green
                    RouteOptimizationMode.HIGHWAY_EXPRESS -> Color(0xFF8B5CF6) // Highway Violet
                }

                Polyline(
                    points = optimizedRoute.polylinePoints,
                    color = routeColor,
                    width = 16f,
                    geodesic = true
                )

                // 5. Real-Time Surge Pricing Heatmap Circles & Zone Pins
                if (isSurgeOverlayEnabled) {
                    surgeHotspots.forEach { hotspot ->
                        val isSelected = selectedSurgeHotspot?.id == hotspot.id
                        val fillColor = when {
                            hotspot.intensity > 0.9 -> Color(0xFFEF4444).copy(alpha = 0.38f) // Crimson Red Ultra Surge
                            hotspot.intensity > 0.8 -> Color(0xFFEA580C).copy(alpha = 0.32f) // Flame Orange High Surge
                            else -> Color(0xFFEAB308).copy(alpha = 0.28f) // Amber Gold Moderate Surge
                        }
                        val strokeColor = when {
                            hotspot.intensity > 0.9 -> Color(0xFFDC2626)
                            hotspot.intensity > 0.8 -> Color(0xFFC2410C)
                            else -> Color(0xFFCA8A04)
                        }

                        // Translucent Surge Radius Circle
                        Circle(
                            center = hotspot.latLng,
                            radius = hotspot.radiusMeters,
                            fillColor = fillColor,
                            strokeColor = strokeColor,
                            strokeWidth = if (isSelected) 6f else 3f
                        )

                        // Surge Zone Multiplier Pin
                        Marker(
                            state = MarkerState(position = hotspot.latLng),
                            title = "⚡ ${hotspot.surgeMultiplier}x SURGE (${hotspot.name})",
                            snippet = "+$${String.format(java.util.Locale.US, "%.2f", hotspot.estimatedSurgeBonus())} extra payout per order • ~${hotspot.orderVolumePerHour} orders/hr",
                            onClick = {
                                selectedSurgeHotspot = hotspot
                                onSurgeZoneSelected?.invoke(hotspot)
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(hotspot.latLng, 14.5f),
                                        800
                                    )
                                }
                                true
                            }
                        )
                    }
                }
            }
        } else {
            // High-Performance Interactive Fallback Vector Map Canvas
            InteractiveFallbackVectorMap(
                modifier = Modifier.fillMaxSize(),
                optimizedRoute = optimizedRoute,
                selectedOptimizationMode = selectedOptimizationMode,
                showDriverMarker = showDriverMarker,
                driverName = driverName,
                isSurgeOverlayEnabled = isSurgeOverlayEnabled,
                surgeHotspots = surgeHotspots,
                selectedSurgeHotspot = selectedSurgeHotspot,
                onSurgeZoneSelected = { spot ->
                    selectedSurgeHotspot = spot
                    onSurgeZoneSelected?.invoke(spot)
                }
            )
        }

        // Top Header: Route Optimization Mode Selector & Live Traffic Badge
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Google Maps Live Engine Branding & Traffic Chip
            Surface(
                color = Color(0xFF0F172A).copy(alpha = 0.92f),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF1A73E8),
                            shape = CircleShape,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = "Google Maps SDK",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Google Maps SDK Delivery Route",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (isLowPowerMode) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFFEAB308),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "🔋 LOW POWER (10s)",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (isLowPowerMode) "🔋 Battery Saver Active • Refresh: 10s • ${optimizedRoute.distanceMiles} mi" else "${sleekThemeMode.displayName} • ${optimizedRoute.distanceMiles} mi (~${optimizedRoute.estimatedDurationMins}m)",
                                fontSize = 9.sp,
                                color = if (isLowPowerMode) Color(0xFFFDE047) else Color(0xFF38BDF8)
                            )
                        }
                    }

                    // Map Tools: Theme Toggle, Traffic Toggle, Surge Overlay Toggle & Recenter
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (showSurgeOverlay) {
                            IconButton(
                                onClick = { isSurgeOverlayEnabled = !isSurgeOverlayEnabled },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Toggle Real-Time Surge Pricing Overlay",
                                    tint = if (isSurgeOverlayEnabled) Color(0xFFF59E0B) else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    sleekThemeMode = when (sleekThemeMode) {
                                        SleekMapThemeMode.SLEEK_DARK -> SleekMapThemeMode.SLEEK_LIGHT
                                        SleekMapThemeMode.SLEEK_LIGHT -> SleekMapThemeMode.STANDARD
                                        SleekMapThemeMode.STANDARD -> SleekMapThemeMode.SLEEK_DARK
                                    }
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Toggle Map Theme mode",
                                    tint = if (sleekThemeMode != SleekMapThemeMode.STANDARD) Color(0xFF38BDF8) else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { isTrafficLayerEnabled = !isTrafficLayerEnabled },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Traffic,
                                    contentDescription = "Toggle Real-Time Traffic Layer",
                                    tint = if (isTrafficLayerEnabled) Color(0xFF4ADE80) else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val boundsBuilder = LatLngBounds.builder()
                                        boundsBuilder.include(optimizedRoute.origin.latLng)
                                        boundsBuilder.include(optimizedRoute.destination.latLng)
                                        optimizedRoute.polylinePoints.forEach { boundsBuilder.include(it) }
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120),
                                            800
                                        )
                                    } catch (_: Exception) {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(defaultCenter, 13.5f),
                                            800
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Recenter Map Camera on Delivery Route",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                currentMapType = when (currentMapType) {
                                    MapType.NORMAL -> MapType.SATELLITE
                                    MapType.SATELLITE -> MapType.TERRAIN
                                    else -> MapType.NORMAL
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Switch Map Type (Normal, Satellite, Terrain)",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Route Optimization Mode Selector Chips
            if (showRouteOptions) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RouteOptimizationMode.values().forEach { mode ->
                        val isSelected = selectedOptimizationMode == mode
                        val (chipBg, chipBorder, chipText) = when {
                            isSelected -> Triple(Color(0xFF1E293B), Color(0xFF38BDF8), Color(0xFF38BDF8))
                            else -> Triple(Color(0xFF0F172A).copy(alpha = 0.85f), Color.Transparent, Color(0xFF94A3B8))
                        }

                        Surface(
                            color = chipBg,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, chipBorder),
                            modifier = Modifier
                                .clickable { selectedOptimizationMode = mode }
                                .testTag("route_opt_mode_${mode.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mode.badge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = mode.displayName,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = chipText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Turn-by-Turn Navigation & Route Details Card
        if (showTurnByTurnHud) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.96f),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Header with current step & summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = Color(0xFF16A34A),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = when (optimizedRoute.steps.getOrNull(currentStepIndex)?.maneuver) {
                                        NavigationManeuver.TURN_RIGHT, NavigationManeuver.SLIGHT_RIGHT -> Icons.Default.TurnRight
                                        NavigationManeuver.TURN_LEFT, NavigationManeuver.SLIGHT_LEFT -> Icons.Default.TurnLeft
                                        NavigationManeuver.MERGE_HIGHWAY -> Icons.Default.CallMerge
                                        NavigationManeuver.ARRIVE_CUSTOMER -> Icons.Default.Home
                                        NavigationManeuver.ARRIVE_STORE -> Icons.Default.Store
                                        else -> Icons.Default.Straight
                                    },
                                    contentDescription = "Navigation Maneuver",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                val activeStep = optimizedRoute.steps.getOrNull(currentStepIndex) ?: optimizedRoute.steps.first()
                                Text(
                                    text = activeStep.instruction,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${optimizedRoute.routeSummary} • Step ${currentStepIndex + 1}/${optimizedRoute.steps.size}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // External Google Maps app Launcher Button
                        IconButton(
                            onClick = {
                                if (onNavigateExternalClick != null) {
                                    onNavigateExternalClick()
                                } else {
                                    val gmmIntentUri = Uri.parse("google.navigation:q=${Uri.encode(dropoffTitle)}&mode=d")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (_: Exception) {
                                        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${Uri.encode(pickupTitle)}&destination=${Uri.encode(dropoffTitle)}&travelmode=driving")
                                        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF1E293B), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Open in Google Maps",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Addresses: Origin -> Destination
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Store, contentDescription = "Store pickup location", tint = Color(0xFF4ADE80), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Pick Up: $pickupTitle", fontSize = 10.sp, color = Color(0xFFE2E8F0), maxLines = 1)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = "Dropoff destination location", tint = Color(0xFFF87171), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Dropoff: $dropoffTitle", fontSize = 10.sp, color = Color(0xFFCBD5E1), maxLines = 1)
                            }
                        }

                        // Next Step / Step Cycle control
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .clickable {
                                    currentStepIndex = (currentStepIndex + 1) % optimizedRoute.steps.size
                                    driverProgress = (currentStepIndex.toFloat() / (optimizedRoute.steps.size - 1)).coerceIn(0.1f, 0.95f)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Next Step", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = "Advance to next turn-by-turn navigation step", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        } else if (isSurgeOverlayEnabled) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.96f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Surge Panel Header: Title, Ticker Countdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Surge Active",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Real-Time Surge Pricing Active",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        // Pulse/Ticker Countdown
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFFEF4444), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Next Refresh: ${surgeCountdown}s",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedSurgeHotspot != null) {
                        val spot = selectedSurgeHotspot!!
                        // Selected Zone Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = spot.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = spot.dominantCategory,
                                            fontSize = 10.sp,
                                            color = Color(0xFFF59E0B),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "~${spot.orderVolumePerHour} orders/hr",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            // Dynamic multiplier display
                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    color = when {
                                        spot.intensity > 0.9 -> Color(0xFFEF4444)
                                        spot.intensity > 0.8 -> Color(0xFFEA580C)
                                        else -> Color(0xFFF59E0B)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "+$${String.format("%.2f", spot.estimatedSurgeBonus())} pay",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${spot.surgeMultiplier}x Multiplier",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Position yourself in this highlighted zone to instantly receive increased base pay multipliers and priority dispatch on new orders.",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 14.sp
                        )
                    } else {
                        Text(
                            text = "Select a surge zone on the map to see real-time payouts and multipliers.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Zone selection horizontal chips list
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        surgeHotspots.forEach { hotspot ->
                            val isSelected = selectedSurgeHotspot?.id == hotspot.id
                            Surface(
                                color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFFF59E0B) else Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable {
                                    selectedSurgeHotspot = hotspot
                                    onSurgeZoneSelected?.invoke(hotspot)
                                    coroutineScope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(hotspot.latLng, 14.2f),
                                            800
                                        )
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${hotspot.surgeMultiplier}x",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = hotspot.name,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White else Color(0xFF64748B),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class SleekMapThemeMode(val displayName: String) {
    SLEEK_DARK("✨ Sleek Dark"),
    SLEEK_LIGHT("☀️ Sleek Light"),
    STANDARD("🗺️ Standard")
}

enum class MarkerSymbolType {
    STORE,
    DROPOFF,
    DRIVER
}

object SleekMapStyleUtils {

    const val SLEEK_DARK_STYLE_JSON = """[
      { "elementType": "geometry", "stylers": [ { "color": "#0F172A" } ] },
      { "elementType": "labels.text.fill", "stylers": [ { "color": "#E2E8F0" } ] },
      { "elementType": "labels.text.stroke", "stylers": [ { "color": "#0F172A" }, { "weight": 3 } ] },
      { "featureType": "administrative", "elementType": "geometry.stroke", "stylers": [ { "color": "#334155" } ] },
      { "featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [ { "color": "#F8FAFC" } ] },
      { "featureType": "landscape", "elementType": "geometry", "stylers": [ { "color": "#1E293B" } ] },
      { "featureType": "poi", "elementType": "geometry", "stylers": [ { "color": "#1E293B" } ] },
      { "featureType": "poi", "elementType": "labels.text.fill", "stylers": [ { "color": "#94A3B8" } ] },
      { "featureType": "poi.park", "elementType": "geometry.fill", "stylers": [ { "color": "#064E3B" } ] },
      { "featureType": "poi.park", "elementType": "labels.text.fill", "stylers": [ { "color": "#6EE7B7" } ] },
      { "featureType": "road", "elementType": "geometry", "stylers": [ { "color": "#334155" } ] },
      { "featureType": "road", "elementType": "geometry.stroke", "stylers": [ { "color": "#0F172A" } ] },
      { "featureType": "road", "elementType": "labels.text.fill", "stylers": [ { "color": "#CBD5E1" } ] },
      { "featureType": "road.highway", "elementType": "geometry", "stylers": [ { "color": "#0369A1" } ] },
      { "featureType": "road.highway", "elementType": "geometry.stroke", "stylers": [ { "color": "#0284C7" } ] },
      { "featureType": "road.highway", "elementType": "labels.text.fill", "stylers": [ { "color": "#F0F9FF" } ] },
      { "featureType": "transit", "elementType": "geometry", "stylers": [ { "color": "#1E293B" } ] },
      { "featureType": "transit.station", "elementType": "labels.text.fill", "stylers": [ { "color": "#38BDF8" } ] },
      { "featureType": "water", "elementType": "geometry", "stylers": [ { "color": "#021526" } ] },
      { "featureType": "water", "elementType": "labels.text.fill", "stylers": [ { "color": "#38BDF8" } ] }
    ]"""

    const val SLEEK_LIGHT_STYLE_JSON = """[
      { "elementType": "geometry", "stylers": [ { "color": "#F8FAFC" } ] },
      { "elementType": "labels.text.fill", "stylers": [ { "color": "#0F172A" } ] },
      { "elementType": "labels.text.stroke", "stylers": [ { "color": "#FFFFFF" }, { "weight": 4 } ] },
      { "featureType": "landscape", "elementType": "geometry.fill", "stylers": [ { "color": "#F1F5F9" } ] },
      { "featureType": "poi.park", "elementType": "geometry.fill", "stylers": [ { "color": "#DCFCE7" } ] },
      { "featureType": "road", "elementType": "geometry.fill", "stylers": [ { "color": "#FFFFFF" } ] },
      { "featureType": "road", "elementType": "geometry.stroke", "stylers": [ { "color": "#CBD5E1" } ] },
      { "featureType": "road.highway", "elementType": "geometry.fill", "stylers": [ { "color": "#0284C7" } ] },
      { "featureType": "road.highway", "elementType": "labels.text.fill", "stylers": [ { "color": "#0F172A" } ] },
      { "featureType": "water", "elementType": "geometry.fill", "stylers": [ { "color": "#BAE6FD" } ] }
    ]"""

    fun createHighContrastMarker(
        context: android.content.Context,
        mainColorHex: String,
        ringColorHex: String = "#FFFFFF",
        symbolType: MarkerSymbolType
    ): BitmapDescriptor? {
        return try {
            com.google.android.gms.maps.MapsInitializer.initialize(context)
            val density = context.resources.displayMetrics.density
            val w = (54 * density).toInt().coerceAtLeast(1)
            val h = (68 * density).toInt().coerceAtLeast(1)

            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

            val mainColor = AndroidColor.parseColor(mainColorHex)
            val ringColor = AndroidColor.parseColor(ringColorHex)
            val shadowColor = AndroidColor.parseColor("#0F172A")

            val cx = w / 2f
            val cy = 26f * density
            val radius = 22f * density

            // 1. Dark Shadow Halo
            paint.color = shadowColor
            paint.alpha = 220
            canvas.drawCircle(cx, cy + (3 * density), radius + (3.5f * density), paint)

            // 2. High Contrast White Ring Halo
            paint.alpha = 255
            paint.color = ringColor
            canvas.drawCircle(cx, cy, radius + (2f * density), paint)

            // 3. Main Colored Badge Circle
            paint.color = mainColor
            canvas.drawCircle(cx, cy, radius, paint)

            // 4. Pointer Pin Tail
            val path = android.graphics.Path().apply {
                moveTo(cx - (11 * density), cy + (12 * density))
                lineTo(cx + (11 * density), cy + (12 * density))
                lineTo(cx, h - (3 * density))
                close()
            }
            paint.color = ringColor
            canvas.drawPath(path, paint)

            val innerPath = android.graphics.Path().apply {
                moveTo(cx - (8 * density), cy + (11 * density))
                lineTo(cx + (8 * density), cy + (11 * density))
                lineTo(cx, h - (6 * density))
                close()
            }
            paint.color = mainColor
            canvas.drawPath(innerPath, paint)

            // 5. Draw High-Contrast Symbol / Emoji Text
            paint.color = AndroidColor.WHITE
            paint.textAlign = android.graphics.Paint.Align.CENTER
            paint.textSize = 15f * density
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val symbolText = when (symbolType) {
                MarkerSymbolType.STORE -> "🛒"
                MarkerSymbolType.DROPOFF -> "🏠"
                MarkerSymbolType.DRIVER -> "🚗"
            }

            val fontMetrics = paint.fontMetrics
            val baseline = cy - (fontMetrics.ascent + fontMetrics.descent) / 2
            canvas.drawText(symbolText, cx, baseline, paint)

            BitmapDescriptorFactory.fromBitmap(bitmap)
        } catch (_: Throwable) {
            null
        }
    }
}

@Composable
fun InteractiveFallbackVectorMap(
    modifier: Modifier = Modifier,
    optimizedRoute: OptimizedDeliveryRoute,
    selectedOptimizationMode: RouteOptimizationMode,
    showDriverMarker: Boolean = true,
    driverName: String = "ShopSafe Courier",
    isSurgeOverlayEnabled: Boolean = false,
    surgeHotspots: List<DemandHotspot> = emptyList(),
    selectedSurgeHotspot: DemandHotspot? = null,
    onSurgeZoneSelected: ((DemandHotspot) -> Unit)? = null
) {
    val routeColor = when (selectedOptimizationMode) {
        RouteOptimizationMode.FASTEST_TRAFFIC -> Color(0xFF1A73E8)
        RouteOptimizationMode.ECO_SAVER -> Color(0xFF16A34A)
        RouteOptimizationMode.HIGHWAY_EXPRESS -> Color(0xFF8B5CF6)
    }

    Box(
        modifier = modifier
            .background(Color(0xFF0F172A))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Sleek Grid Lines & Street Blocks
            val gridStep = 45.dp.toPx()
            var x = 0f
            while (x < canvasW) {
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.7f),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 2f
                )
                x += gridStep
            }
            var y = 0f
            while (y < canvasH) {
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.7f),
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 2f
                )
                y += gridStep
            }

            // Diagonal Highway Arteries
            drawLine(
                color = Color(0xFF334155).copy(alpha = 0.6f),
                start = Offset(0f, canvasH * 0.3f),
                end = Offset(canvasW, canvasH * 0.7f),
                strokeWidth = 14f
            )
            drawLine(
                color = Color(0xFF0284C7).copy(alpha = 0.35f),
                start = Offset(0f, canvasH * 0.3f),
                end = Offset(canvasW, canvasH * 0.7f),
                strokeWidth = 6f
            )

            // 2. Surge Heatmap Circles
            if (isSurgeOverlayEnabled) {
                surgeHotspots.forEachIndexed { index, hotspot ->
                    val hx = canvasW * (0.25f + ((index % 3) * 0.25f))
                    val hy = canvasH * (0.25f + ((index / 3) * 0.35f))
                    val radius = 55.dp.toPx()
                    val isSel = selectedSurgeHotspot?.id == hotspot.id

                    drawCircle(
                        color = when {
                            hotspot.intensity > 0.9 -> Color(0xFFEF4444).copy(alpha = 0.28f)
                            hotspot.intensity > 0.8 -> Color(0xFFEA580C).copy(alpha = 0.24f)
                            else -> Color(0xFFEAB308).copy(alpha = 0.20f)
                        },
                        radius = radius,
                        center = Offset(hx, hy)
                    )
                    drawCircle(
                        color = if (isSel) Color(0xFFF59E0B) else Color(0xFFCA8A04).copy(alpha = 0.6f),
                        radius = radius,
                        center = Offset(hx, hy),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = if (isSel) 4f else 2f)
                    )
                }
            }

            // 3. Dynamic Smooth Polyline Route
            val startOffset = Offset(canvasW * 0.22f, canvasH * 0.68f)
            val endOffset = Offset(canvasW * 0.78f, canvasH * 0.28f)
            val midOffset = Offset(canvasW * 0.52f, canvasH * 0.45f)

            val routePath = Path().apply {
                moveTo(startOffset.x, startOffset.y)
                quadraticTo(midOffset.x, midOffset.y + 30f, endOffset.x, endOffset.y)
            }

            // Route Glow & Line
            drawPath(
                path = routePath,
                color = routeColor.copy(alpha = 0.3f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 24f)
            )
            drawPath(
                path = routePath,
                color = routeColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 10f,
                    pathEffect = PathEffect.cornerPathEffect(24f)
                )
            )

            // 4. Store Pickup Marker Pin (Green)
            drawCircle(
                color = Color.White,
                radius = 16.dp.toPx(),
                center = startOffset
            )
            drawCircle(
                color = Color(0xFF059669),
                radius = 13.dp.toPx(),
                center = startOffset
            )

            // 5. Customer Dropoff Marker Pin (Red)
            drawCircle(
                color = Color.White,
                radius = 16.dp.toPx(),
                center = endOffset
            )
            drawCircle(
                color = Color(0xFFDC2626),
                radius = 13.dp.toPx(),
                center = endOffset
            )

            // 6. Courier Live Location Marker Pin (Azure Blue)
            if (showDriverMarker) {
                val driverPos = Offset(canvasW * 0.46f, canvasH * 0.52f)
                drawCircle(
                    color = Color(0xFFFDE047),
                    radius = 18.dp.toPx(),
                    center = driverPos
                )
                drawCircle(
                    color = Color(0xFF0284C7),
                    radius = 14.dp.toPx(),
                    center = driverPos
                )
            }
        }
    }
}

