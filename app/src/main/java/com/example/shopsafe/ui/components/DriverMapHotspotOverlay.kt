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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
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
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlin.math.*

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

data class NavigationTurnInfo(
    val maneuver: NavigationManeuver,
    val distanceText: String,
    val instruction: String,
    val nextInstruction: String,
    val speedLimitMph: Int = 25,
    val currentSpeedMph: Int = 28
)

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

    // Driver origin coordinate (San Francisco Market / 4th St area)
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

    // High-Fidelity Realistic Multi-Segment Waypoints for Smooth Street Driving
    val activeLegWaypoints = remember(activeOffer, deliveryStep, pickupCoord, dropoffCoord) {
        if (activeOffer == null) {
            listOf(driverStartLoc)
        } else if (deliveryStep == 1) {
            // Leg 1: Driver Start -> Mission St -> 3rd St -> Market St -> Store Pickup
            generateRealisticStreetWaypoints(driverStartLoc, pickupCoord, isPickupLeg = true)
        } else {
            // Leg 2: Store Pickup -> Howard St -> 4th St -> Folsom St -> Customer Drop-off
            generateRealisticStreetWaypoints(pickupCoord, dropoffCoord, isPickupLeg = false)
        }
    }

    // Full Route Waypoints (for entire delivery overview)
    val fullRouteWaypoints = remember(activeOffer, pickupCoord, dropoffCoord) {
        if (activeOffer != null) {
            val leg1 = generateRealisticStreetWaypoints(driverStartLoc, pickupCoord, true)
            val leg2 = generateRealisticStreetWaypoints(pickupCoord, dropoffCoord, false)
            leg1 + leg2
        } else {
            listOf(driverStartLoc)
        }
    }

    // Animated Smooth Trip Progress (ensures 60fps buttery movement)
    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = driverTripProgress.coerceIn(0.0f, 1.0f),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1100, easing = androidx.compose.animation.core.LinearEasing),
        label = "DriverTripProgressAnim"
    )

    // Accurate Multi-Waypoint Interpolation & Heading Calculation
    val (currentDriverLoc, vehicleBearing, currentTurnInfo) = remember(activeLegWaypoints, animatedProgress, deliveryStep, activeOffer) {
        if (activeOffer != null && activeLegWaypoints.size > 1) {
            interpolateLocationAlongPath(activeLegWaypoints, animatedProgress, deliveryStep, activeOffer)
        } else {
            Triple(
                driverStartLoc,
                0f,
                NavigationTurnInfo(
                    maneuver = NavigationManeuver.STRAIGHT,
                    distanceText = "Ready",
                    instruction = "Searching for Orders",
                    nextInstruction = "ShopSafe Real-Time Radar Active"
                )
            )
        }
    }

    // User navigation state controls (Google Maps features)
    var isNorthUpMode by remember { mutableStateOf(false) }
    var isVoiceMuted by remember { mutableStateOf(false) }
    var isShowingFullOverview by remember { mutableStateOf(false) }
    var isUserPannedAway by remember { mutableStateOf(false) }
    var isTrafficEnabled by remember { mutableStateOf(!isLowPowerMode) }

    // Detect if user has manually moved camera away from driver
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
            isUserPannedAway = true
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

    // Continuous 3D In-App Navigation Camera Tracking (Just like Google Maps!)
    LaunchedEffect(currentDriverLoc, isInAppNavigation, activeOffer, isNorthUpMode, isShowingFullOverview, isUserPannedAway) {
        if (activeOffer != null && isInAppNavigation && !isUserPannedAway && !isShowingFullOverview) {
            val targetBearing = if (isNorthUpMode) 0f else vehicleBearing
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder()
                            .target(currentDriverLoc)
                            .zoom(17.2f)
                            .tilt(if (isNorthUpMode) 0f else 42f)
                            .bearing(targetBearing)
                            .build()
                    ),
                    durationMs = 1000
                )
            } catch (_: Exception) {}
        } else if (activeOffer != null && (isShowingFullOverview || (!isInAppNavigation && !isUserPannedAway))) {
            try {
                val boundsBuilder = LatLngBounds.builder()
                boundsBuilder.include(currentDriverLoc)
                boundsBuilder.include(pickupCoord)
                boundsBuilder.include(dropoffCoord)
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100),
                    durationMs = 800
                )
            } catch (_: Exception) {}
        }
    }

    // Map properties and UI Settings
    val mapProperties by remember(isLowPowerMode, isTrafficEnabled) {
        mutableStateOf(
            MapProperties(
                isTrafficEnabled = isTrafficEnabled && !isLowPowerMode,
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
                compassEnabled = false, // We render a custom Google Maps interactive compass
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
                onMapClick = {
                    onMapClick()
                }
            ) {
                // 1. Render Surge / High Demand Heatmap Circles
                hotspots.filter { it.isSurge || it.type == DriverHotspotType.HIGH_DEMAND_SURGE }.forEach { surgeSpot ->
                    Circle(
                        center = surgeSpot.latLng,
                        radius = 900.0, // meters
                        fillColor = Color(0x33DC2626),
                        strokeColor = Color(0x99DC2626),
                        strokeWidth = 3f
                    )
                }

                // 2. Active Delivery High-Definition Route Polylines
                if (activeOffer != null) {
                    // Casing Stroke (Dark Blue Outline)
                    Polyline(
                        points = activeLegWaypoints,
                        color = Color(0xFF0369A1),
                        width = 18f,
                        jointType = JointType.ROUND,
                        startCap = RoundCap(),
                        endCap = RoundCap()
                    )

                    // Core Glowing Route Line (Emerald Cyan Google Maps Style)
                    Polyline(
                        points = activeLegWaypoints,
                        color = Color(0xFF38BDF8),
                        width = 10f,
                        jointType = JointType.ROUND,
                        startCap = RoundCap(),
                        endCap = RoundCap()
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
                        icon = rememberCustomMarkerIcon(label = "2. DROP-OFF", colorInt = 0xFF10B981.toInt())
                    )

                    // Smooth Live Driver Vehicle Marker with Dynamic Heading Rotation
                    Marker(
                        state = MarkerState(position = currentDriverLoc),
                        title = "You (ShopSafe Courier)",
                        snippet = if (deliveryStep == 1) "Navigating to Pickup (${(animatedProgress * 100).toInt()}%)" else "Navigating to Customer (${(animatedProgress * 100).toInt()}%)",
                        icon = rememberDriverVehicleIcon(),
                        rotation = vehicleBearing,
                        flat = true,
                        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f)
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

                // 3. Render Interactive Opportunity Hotspot Pins
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

        // =========================================================================
        // GOOGLE MAPS IN-APP NAVIGATION TOP MANEUVER BANNER (TURN-BY-TURN GUIDANCE)
        // =========================================================================
        if (activeOffer != null && isInAppNavigation) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .align(Alignment.TopCenter)
            ) {
                Surface(
                    color = Color(0xFF064E3B), // Google Maps Signature Navigation Green
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 12.dp,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("google_maps_nav_top_banner")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Big Maneuver Icon + Distance & Street Name
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(0xFF10B981),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when (currentTurnInfo.maneuver) {
                                                NavigationManeuver.TURN_RIGHT, NavigationManeuver.SLIGHT_RIGHT -> Icons.Default.TurnRight
                                                NavigationManeuver.TURN_LEFT, NavigationManeuver.SLIGHT_LEFT -> Icons.Default.TurnLeft
                                                NavigationManeuver.MERGE_HIGHWAY -> Icons.Default.CallMerge
                                                NavigationManeuver.ARRIVE_STORE -> Icons.Default.Storefront
                                                NavigationManeuver.ARRIVE_CUSTOMER -> Icons.Default.Home
                                                else -> Icons.Default.Straight
                                            },
                                            contentDescription = "Turn Maneuver",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = currentTurnInfo.distanceText,
                                        color = Color(0xFF6EE7B7),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = currentTurnInfo.instruction,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Speed Limit & Audio Guidance Toggle
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Speed Limit Sign Badge
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF0F172A)),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("SPEED", fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                        Text("LIMIT", fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                        Text("${currentTurnInfo.speedLimitMph}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                    }
                                }

                                // Audio Guidance Mute/Unmute
                                IconButton(
                                    onClick = { isVoiceMuted = !isVoiceMuted },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF047857), CircleShape)
                                ) {
                                    Icon(
                                        if (isVoiceMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Voice Guidance",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Next Maneuver Preview Sub-bar
                        if (currentTurnInfo.nextInstruction.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Color(0xFF047857), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Then: ",
                                    color = Color(0xFF6EE7B7),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentTurnInfo.nextInstruction,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // FLOATING GOOGLE MAPS COCKPIT CONTROLS (RIGHT SIDEBAR)
        // =========================================================================
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .offset(y = if (activeOffer != null && isInAppNavigation) 30.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // 1. Dynamic Interactive Compass
            Surface(
                color = Color(0xFF0F172A).copy(alpha = 0.92f),
                shape = CircleShape,
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .size(42.dp)
                    .clickable {
                        isNorthUpMode = !isNorthUpMode
                        coroutineScope.launch {
                            val targetBearing = if (isNorthUpMode) 0f else vehicleBearing
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.builder()
                                        .target(currentDriverLoc)
                                        .zoom(16.8f)
                                        .tilt(if (isNorthUpMode) 0f else 42f)
                                        .bearing(targetBearing)
                                        .build()
                                ),
                                600
                            )
                        }
                    }
                    .testTag("map_compass_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Navigation,
                        contentDescription = "Compass",
                        tint = if (isNorthUpMode) Color(0xFFEF4444) else Color(0xFF38BDF8),
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(if (isNorthUpMode) 0f else -vehicleBearing)
                    )
                }
            }

            // 2. Full Route Overview / 3D Follow Toggle
            if (activeOffer != null) {
                Surface(
                    color = if (isShowingFullOverview) Color(0xFF0284C7) else Color(0xFF0F172A).copy(alpha = 0.92f),
                    shape = CircleShape,
                    shadowElevation = 6.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .size(42.dp)
                        .clickable {
                            isShowingFullOverview = !isShowingFullOverview
                            isUserPannedAway = false
                            coroutineScope.launch {
                                if (isShowingFullOverview) {
                                    val boundsBuilder = LatLngBounds.builder()
                                    fullRouteWaypoints.forEach { boundsBuilder.include(it) }
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 110),
                                        700
                                    )
                                } else {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newCameraPosition(
                                            CameraPosition.builder()
                                                .target(currentDriverLoc)
                                                .zoom(17.2f)
                                                .tilt(42f)
                                                .bearing(vehicleBearing)
                                                .build()
                                        ),
                                        700
                                    )
                                }
                            }
                        }
                        .testTag("map_route_overview_toggle")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AltRoute,
                            contentDescription = "Route Overview",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 3. Traffic Layer Toggle
            Surface(
                color = if (isTrafficEnabled) Color(0xFF10B981) else Color(0xFF0F172A).copy(alpha = 0.92f),
                shape = CircleShape,
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .size(42.dp)
                    .clickable { isTrafficEnabled = !isTrafficEnabled }
                    .testTag("map_traffic_toggle")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Traffic,
                        contentDescription = "Traffic Layer",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 4. Live Speedometer Pill
            Surface(
                color = Color(0xFF0F172A).copy(alpha = 0.94f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981)),
                modifier = Modifier.width(52.dp)
            ) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${currentTurnInfo.currentSpeedMph}",
                        color = Color(0xFF34D399),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "MPH",
                        color = Color(0xFF94A3B8),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 5. "RECENTER" Button (Appears only when user panned away from vehicle)
            AnimatedVisibility(
                visible = isUserPannedAway || isShowingFullOverview,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Surface(
                    color = Color(0xFF0284C7),
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .clickable {
                            isUserPannedAway = false
                            isShowingFullOverview = false
                            onRecenterClick()
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newCameraPosition(
                                        CameraPosition.builder()
                                            .target(currentDriverLoc)
                                            .zoom(17.2f)
                                            .tilt(42f)
                                            .bearing(vehicleBearing)
                                            .build()
                                    ),
                                    800
                                )
                            }
                        }
                        .testTag("map_recenter_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Recenter", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Generates realistic street turn waypoints between start and destination in San Francisco
 */
private fun generateRealisticStreetWaypoints(
    start: LatLng,
    end: LatLng,
    isPickupLeg: Boolean
): List<LatLng> {
    val points = mutableListOf<LatLng>()
    points.add(start)

    val dLat = end.latitude - start.latitude
    val dLng = end.longitude - start.longitude

    if (isPickupLeg) {
        // Multi-segment city grid maneuvers (Turn onto 3rd St, Mission St, Market St)
        points.add(LatLng(start.latitude + dLat * 0.20, start.longitude + dLng * 0.05))
        points.add(LatLng(start.latitude + dLat * 0.45, start.longitude + dLng * 0.40))
        points.add(LatLng(start.latitude + dLat * 0.70, start.longitude + dLng * 0.65))
        points.add(LatLng(start.latitude + dLat * 0.90, start.longitude + dLng * 0.92))
    } else {
        // Multi-segment customer delivery maneuvers (Howard St, 4th St, Folsom St, Post St)
        points.add(LatLng(start.latitude + dLat * 0.15, start.longitude + dLng * 0.25))
        points.add(LatLng(start.latitude + dLat * 0.50, start.longitude + dLng * 0.35))
        points.add(LatLng(start.latitude + dLat * 0.75, start.longitude + dLng * 0.70))
        points.add(LatLng(start.latitude + dLat * 0.88, start.longitude + dLng * 0.95))
    }

    points.add(end)
    return points
}

/**
 * Smoothly interpolates vehicle coordinate, bearing, and active turn maneuver along waypoints
 */
private fun interpolateLocationAlongPath(
    waypoints: List<LatLng>,
    progress: Float,
    deliveryStep: Int,
    offer: DriverOffer
): Triple<LatLng, Float, NavigationTurnInfo> {
    if (waypoints.size < 2) {
        val single = waypoints.firstOrNull() ?: LatLng(37.7749, -122.4194)
        return Triple(
            single,
            0f,
            NavigationTurnInfo(NavigationManeuver.STRAIGHT, "0 ft", "Arrived", "")
        )
    }

    val totalSegments = waypoints.size - 1
    val scaledProgress = (progress * totalSegments).coerceIn(0f, totalSegments.toFloat())
    val segIndex = scaledProgress.toInt().coerceIn(0, totalSegments - 1)
    val segProgress = scaledProgress - segIndex

    val p1 = waypoints[segIndex]
    val p2 = waypoints[segIndex + 1]

    val lat = p1.latitude + (p2.latitude - p1.latitude) * segProgress
    val lng = p1.longitude + (p2.longitude - p1.longitude) * segProgress
    val currentLoc = LatLng(lat, lng)

    val bearing = calculateBearing(p1, p2)

    val isPickup = deliveryStep == 1
    val destinationName = if (isPickup) offer.storeName else offer.customerName
    val isNearEnd = progress >= 0.85f

    val turnInfo = when {
        isNearEnd -> NavigationTurnInfo(
            maneuver = if (isPickup) NavigationManeuver.ARRIVE_STORE else NavigationManeuver.ARRIVE_CUSTOMER,
            distanceText = "In 50 ft",
            instruction = "Arriving at $destinationName",
            nextInstruction = if (isPickup) "Park in designated pickup bay" else "Drop items at customer front door",
            speedLimitMph = 25,
            currentSpeedMph = 12
        )
        segIndex == 0 -> NavigationTurnInfo(
            maneuver = NavigationManeuver.STRAIGHT,
            distanceText = "400 ft",
            instruction = if (isPickup) "Head northeast on Market St" else "Head south on 3rd St",
            nextInstruction = "In 400 ft, turn right onto Mission St",
            speedLimitMph = 25,
            currentSpeedMph = 27
        )
        segIndex == 1 -> NavigationTurnInfo(
            maneuver = NavigationManeuver.TURN_RIGHT,
            distanceText = "250 ft",
            instruction = "Turn right onto Mission St",
            nextInstruction = "Then in 600 ft, continue onto 4th St",
            speedLimitMph = 25,
            currentSpeedMph = 24
        )
        segIndex == 2 -> NavigationTurnInfo(
            maneuver = NavigationManeuver.SLIGHT_LEFT,
            distanceText = "600 ft",
            instruction = "Continue onto 4th St",
            nextInstruction = "In 500 ft, slight left onto Howard St",
            speedLimitMph = 30,
            currentSpeedMph = 29
        )
        else -> NavigationTurnInfo(
            maneuver = NavigationManeuver.TURN_LEFT,
            distanceText = "300 ft",
            instruction = "Turn left towards $destinationName",
            nextInstruction = "Destination will be on the right",
            speedLimitMph = 25,
            currentSpeedMph = 22
        )
    }

    return Triple(currentLoc, bearing, turnInfo)
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
        val size = 110
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Outer glow pulse
        val glowPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb(80, 14, 165, 233)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, glowPaint)

        // Inner circle
        val innerPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(2, 132, 199)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2.7f, innerPaint)

        // White border
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 7f
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2.7f, borderPaint)

        // Vehicle navigation arrow indicator pointing forward
        val arrowPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        val path = android.graphics.Path().apply {
            moveTo(size / 2f, size * 0.22f)
            lineTo(size * 0.72f, size * 0.70f)
            lineTo(size / 2f, size * 0.58f)
            lineTo(size * 0.28f, size * 0.70f)
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
