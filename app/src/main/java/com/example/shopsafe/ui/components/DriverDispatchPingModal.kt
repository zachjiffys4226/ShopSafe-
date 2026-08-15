package com.example.shopsafe.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.shopsafe.data.models.DeliveryRouteEngine
import com.example.shopsafe.data.models.DriverOffer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*

private const val PING_MAP_STYLE = """
[
  { "elementType": "geometry", "stylers": [ { "color": "#1e293b" } ] },
  { "elementType": "labels.text.fill", "stylers": [ { "color": "#94a3b8" } ] },
  { "elementType": "labels.text.stroke", "stylers": [ { "color": "#0f172a" } ] },
  { "featureType": "road", "elementType": "geometry", "stylers": [ { "color": "#334155" } ] },
  { "featureType": "road.highway", "elementType": "geometry", "stylers": [ { "color": "#0284c7" } ] },
  { "featureType": "water", "elementType": "geometry", "stylers": [ { "color": "#0b1329" } ] }
]
"""

@Composable
fun DriverDispatchPingModal(
    offer: DriverOffer,
    timerSeconds: Int,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val totalSeconds = 45f
    val progress = (timerSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)

    // Pulsing animation for the dispatch ping badge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Resolved Coordinates for Pickup, Drop-off, and Driver
    val driverLoc = remember { LatLng(37.7790, -122.4150) }
    val pickupCoord = remember(offer) {
        if (offer.pickupLat != 0.0 && offer.pickupLng != 0.0) {
            LatLng(offer.pickupLat, offer.pickupLng)
        } else {
            DeliveryRouteEngine.resolveStoreLatLng(offer.storeName, offer.pickupAddress)
        }
    }
    val dropoffCoord = remember(offer) {
        if (offer.dropoffLat != 0.0 && offer.dropoffLng != 0.0) {
            LatLng(offer.dropoffLat, offer.dropoffLng)
        } else {
            DeliveryRouteEngine.resolveDropoffLatLng(offer.dropoffAddress)
        }
    }

    // Polyline connecting Driver -> Pickup -> Drop-off
    val routePoints = remember(pickupCoord, dropoffCoord) {
        listOf(
            driverLoc,
            LatLng((driverLoc.latitude + pickupCoord.latitude) / 2, (driverLoc.longitude + pickupCoord.longitude) / 2),
            pickupCoord,
            LatLng((pickupCoord.latitude + dropoffCoord.latitude) / 2, (pickupCoord.longitude + dropoffCoord.longitude) / 2),
            dropoffCoord
        )
    }

    val centerCoord = remember(driverLoc, pickupCoord, dropoffCoord) {
        LatLng(
            (driverLoc.latitude + pickupCoord.latitude + dropoffCoord.latitude) / 3.0,
            (driverLoc.longitude + pickupCoord.longitude + dropoffCoord.longitude) / 3.0
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerCoord, 12.8f)
    }

    LaunchedEffect(pickupCoord, dropoffCoord) {
        try {
            kotlinx.coroutines.delay(350L)
            val boundsBuilder = LatLngBounds.builder()
            boundsBuilder.include(driverLoc)
            boundsBuilder.include(pickupCoord)
            boundsBuilder.include(dropoffCoord)
            val bounds = boundsBuilder.build()
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 48),
                durationMs = 400
            )
        } catch (_: Throwable) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(centerCoord, 12.8f)
        }
    }

    Dialog(
        onDismissRequest = onDecline,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f)
                .testTag("driver_dispatch_ping_modal"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Brush.verticalGradient(
                    listOf(Color(0xFF38BDF8), Color(0xFF10B981))
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // 1. TOP HEADER: Priority Ping Badge + Countdown Timer Ring
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF0284C7).copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Radar,
                                    contentDescription = "Radar Ping",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⚡ AUTOMATIC DISPATCH",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "Matched as Closest Driver",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Timer Circular Progress Badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(46.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = if (timerSeconds <= 10) Color(0xFFEF4444) else Color(0xFF10B981),
                            trackColor = Color(0xFF334155),
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "${timerSeconds}s",
                            color = if (timerSeconds <= 10) Color(0xFFEF4444) else Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. PAY OFFER & STATS BANNER
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL GUARANTEED PAY",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "$${String.format(java.util.Locale.US, "%.2f", offer.payAmount)}",
                                    color = Color(0xFF10B981),
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // Distance & Time Chips
                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Navigation, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${offer.distanceMiles} mi total",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Est. ${offer.estimatedMins} mins",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFF334155))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Pay Breakdown Tags
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Base: $${String.format(java.util.Locale.US, "%.2f", offer.basePay)}",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "+ 100% Tip: $${String.format(java.util.Locale.US, "%.2f", offer.tipAmount)}",
                                color = Color(0xFF34D399),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (offer.bonusAmount > 0) {
                                Text(
                                    text = "+ Surge: $${String.format(java.util.Locale.US, "%.2f", offer.bonusAmount)}",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. MAP VIEW OF PICKUP & DROPOFF LOCATIONS
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val isPlayServicesAvailable = remember(context) {
                        try {
                            val availability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
                            val result = availability.isGooglePlayServicesAvailable(context)
                            result == com.google.android.gms.common.ConnectionResult.SUCCESS
                        } catch (_: Throwable) {
                            false
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isPlayServicesAvailable) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                properties = MapProperties(
                                    mapType = MapType.NORMAL,
                                    isMyLocationEnabled = false,
                                    mapStyleOptions = MapStyleOptions(PING_MAP_STYLE)
                                ),
                                uiSettings = MapUiSettings(
                                    zoomControlsEnabled = false,
                                    compassEnabled = false,
                                    myLocationButtonEnabled = false,
                                    mapToolbarEnabled = false,
                                    scrollGesturesEnabled = true,
                                    zoomGesturesEnabled = true
                                )
                            ) {
                                // Polyline Route from Driver to Pickup to Dropoff
                                Polyline(
                                    points = routePoints,
                                    color = Color(0xFF10B981),
                                    width = 10f
                                )

                                // Pickup Location Marker
                                Marker(
                                    state = MarkerState(position = pickupCoord),
                                    title = "Pickup: ${offer.storeName}",
                                    snippet = offer.pickupAddress,
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                                )

                                // Dropoff Location Marker
                                Marker(
                                    state = MarkerState(position = dropoffCoord),
                                    title = "Drop-off: ${offer.customerName}",
                                    snippet = offer.dropoffAddress,
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                                )

                                // Driver Location Marker
                                Marker(
                                    state = MarkerState(position = driverLoc),
                                    title = "Your Location",
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                                )
                            }
                        } else {
                            // High-performance fallback vector route schematic
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Background grid
                                val step = 28.dp.toPx()
                                var x = 0f
                                while (x < w) {
                                    drawLine(Color(0xFF334155).copy(alpha = 0.4f), androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, h), 1.5f)
                                    x += step
                                }
                                var y = 0f
                                while (y < h) {
                                    drawLine(Color(0xFF334155).copy(alpha = 0.4f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(w, y), 1.5f)
                                    y += step
                                }

                                // Route curve
                                val pDriver = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.75f)
                                val pPickup = androidx.compose.ui.geometry.Offset(w * 0.48f, h * 0.35f)
                                val pDropoff = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.65f)

                                val routePath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(pDriver.x, pDriver.y)
                                    quadraticTo(w * 0.3f, h * 0.6f, pPickup.x, pPickup.y)
                                    quadraticTo(w * 0.65f, h * 0.25f, pDropoff.x, pDropoff.y)
                                }

                                drawPath(routePath, Color(0xFF10B981), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f, cap = androidx.compose.ui.graphics.StrokeCap.Round))

                                // Draw node circles
                                drawCircle(Color(0xFF0284C7), radius = 10.dp.toPx(), center = pDriver)
                                drawCircle(Color.White, radius = 5.dp.toPx(), center = pDriver)

                                drawCircle(Color(0xFFF59E0B), radius = 12.dp.toPx(), center = pPickup)
                                drawCircle(Color.White, radius = 6.dp.toPx(), center = pPickup)

                                drawCircle(Color(0xFF10B981), radius = 12.dp.toPx(), center = pDropoff)
                                drawCircle(Color.White, radius = 6.dp.toPx(), center = pDropoff)
                            }
                        }

                        // Map Overlay Banner
                        Surface(
                            color = Color(0xFF0F172A).copy(alpha = 0.85f),
                            shape = RoundedCornerShape(bottomEnd = 10.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Live Route Preview", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. SCROLLABLE DETAILS: Customer Name, Description & Addresses
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Customer & Merchant Row
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            // Customer Name
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Customer: ${offer.customerName}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "⭐ 4.9 • Verified ShopSafe Member",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Description & Items Summary
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Order Description:",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = offer.itemDetails,
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (offer.deliveryInstructions.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.Notes, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Note: ${offer.deliveryInstructions}",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Pickup and Dropoff Address Cards
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Stop 1: Pickup
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF0284C7),
                                    shape = CircleShape,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("1", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("PICKUP: ${offer.storeName}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(offer.pickupAddress, color = Color(0xFF94A3B8), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 2.dp))

                            // Stop 2: Drop-off
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF16A34A),
                                    shape = CircleShape,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("2", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("DROP-OFF: ${offer.customerName}", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(offer.dropoffAddress, color = Color(0xFF94A3B8), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 5. ACTION BUTTONS: ACCEPT (Primary) & DECLINE (Secondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier
                            .weight(0.35f)
                            .height(52.dp)
                            .testTag("driver_decline_dispatch_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                    ) {
                        Text("Pass", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier
                            .weight(0.65f)
                            .height(52.dp)
                            .testTag("driver_accept_dispatch_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ACCEPT OFFER ($${String.format(java.util.Locale.US, "%.2f", offer.payAmount)})",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
