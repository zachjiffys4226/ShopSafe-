package com.example.shopsafe.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * High-visibility banner displayed to drivers when network connectivity is lost.
 * Prevents drivers from missing time-sensitive 72-second order dispatch pings and informs
 * them of real-time offline status.
 */
@Composable
fun DriverOfflineNetworkBanner(
    isNetworkConnected: Boolean,
    isSimulatedOffline: Boolean,
    onReconnectClick: () -> Unit,
    onToggleSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isNetworkConnected,
        enter = expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
        exit = shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) + fadeOut(),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse_warning")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
        val alphaGlow by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
        )

        var isChecking by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("driver_offline_warning_banner"),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF450A0A),
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.5.dp,
                color = Color(0xFFEF4444).copy(alpha = alphaGlow)
            )
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF7F1D1D),
                                Color(0xFF450A0A)
                            )
                        )
                    )
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Pulsing Alert Badge
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFDC2626),
                                modifier = Modifier
                                    .size(38.dp)
                                    .scale(pulseScale)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.WifiOff,
                                        contentDescription = "No Network Connection",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "NO NETWORK CONNECTION",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = Color(0xFFFCA5A5),
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFF991B1B),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "DISPATCH PAUSED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFFEF2F2),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Your device is offline. Real-time 72s order pings and push alerts cannot be delivered.",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFFEE2E2),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Protection Badge & Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = Color(0xFF1E1B4B).copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Acceptance rate protected",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE0E7FF)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (isSimulatedOffline) {
                                TextButton(
                                    onClick = onToggleSimulation,
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFDE047)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.testTag("resume_network_button")
                                ) {
                                    Text("Restore Live", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        isChecking = true
                                        onReconnectClick()
                                        delay(800)
                                        isChecking = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDC2626),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("retry_connection_button")
                            ) {
                                if (isChecking) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Checking...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = "Retry connection icon", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry Connection", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dynamic Connectivity & Dispatch Status Badge for TopAppBar and Header Bars
 */
@Composable
fun DriverConnectivityBadge(
    isOnline: Boolean,
    isNetworkConnected: Boolean,
    isLowPowerModeEnabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_beacon")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        when {
            !isNetworkConnected -> {
                Surface(
                    color = Color(0xFF7F1D1D),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = modifier.testTag("driver_status_offline_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = dotAlpha))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "DISCONNECTED • NO NETWORK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFEF2F2)
                        )
                    }
                }
            }
            isOnline -> {
                Surface(
                    color = Color(0xFF14532D),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E)),
                    modifier = modifier.testTag("driver_status_online_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4ADE80))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "ONLINE • DISPATCH READY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDCFCE7)
                        )
                    }
                }
            }
            else -> {
                Surface(
                    color = Color(0xFF334155),
                    shape = RoundedCornerShape(12.dp),
                    modifier = modifier
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "SHIFT PAUSED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }

        if (isLowPowerModeEnabled) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                color = Color(0xFF854D0E),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEAB308))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.BatterySaver,
                        contentDescription = "Low Power Mode active",
                        tint = Color(0xFFFDE047),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "LOW POWER 10s",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFEF08A)
                    )
                }
            }
        }
    }
}

/**
 * Driver Portal Low Power Mode Battery Saver Card.
 * Displays battery conservation stats, map tracking refresh rate (10s), notification polling intervals,
 * and OLED screen energy savings.
 */
@Composable
fun DriverLowPowerBannerCard(
    isLowPowerModeEnabled: Boolean,
    onToggleLowPowerMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isLowPowerModeEnabled) Color(0xFF1E293B) else Color(0xFFF8FAFC),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isLowPowerModeEnabled) Color(0xFFEAB308) else Color(0xFFCBD5E1)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        color = if (isLowPowerModeEnabled) Color(0xFFEAB308).copy(alpha = 0.2f) else Color(0xFF0284C7).copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isLowPowerModeEnabled) Icons.Default.BatterySaver else Icons.Default.BatteryChargingFull,
                                contentDescription = "Low Power Mode Battery Saver",
                                tint = if (isLowPowerModeEnabled) Color(0xFFEAB308) else Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Low Power Battery Saver",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isLowPowerModeEnabled) Color.White else Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isLowPowerModeEnabled) {
                                Surface(
                                    color = Color(0xFFEAB308),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "SAVING ~35%",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isLowPowerModeEnabled) "Map tracking set to 10s refresh • Notifications throttled • Dark theme locked" else "Standard mode active • 2s high-precision map refresh",
                            fontSize = 11.sp,
                            color = if (isLowPowerModeEnabled) Color(0xFFFDE047) else Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = isLowPowerModeEnabled,
                    onCheckedChange = { onToggleLowPowerMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Color(0xFFEAB308),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1)
                    )
                )
            }

            AnimatedVisibility(visible = isLowPowerModeEnabled) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Map Refresh", fontSize = 10.sp, color = Color.Gray)
                            Text("10 sec (vs 2s)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Dispatch Polling", fontSize = 10.sp, color = Color.Gray)
                            Text("3 sec throttled", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Display Power", fontSize = 10.sp, color = Color.Gray)
                            Text("OLED Dark Theme", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        }
                        Column {
                            Text("Est. Shift Boost", fontSize = 10.sp, color = Color.Gray)
                            Text("+2.5 hrs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Diagnostics & Network Monitoring Controller for Driver Portal
 */
@Composable
fun DriverConnectivityDiagnosticsBar(
    isNetworkConnected: Boolean,
    isSimulatedOffline: Boolean,
    onToggleSimulatedOffline: () -> Unit,
    onTestFcmAlert: () -> Unit,
    onConnectLiveDispatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isNetworkConnected) Color(0xFF1E293B) else Color(0xFF3B0764),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("driver_connectivity_diagnostics_bar")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isNetworkConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = if (isNetworkConnected) "Network connected" else "Network disconnected",
                        tint = if (isNetworkConnected) Color(0xFF22C55E) else Color(0xFFF87171),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (isNetworkConnected) "Network: LTE/Wi-Fi Active" else "Network: Offline (Suspended)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isNetworkConnected) "Live 72s dispatch alerts active" else "Order alerts paused until reconnection",
                            fontSize = 10.sp,
                            color = if (isNetworkConnected) Color(0xFF94A3B8) else Color(0xFFFCA5A5)
                        )
                    }
                }

                // Simulation Toggle Button
                OutlinedButton(
                    onClick = onToggleSimulatedOffline,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isSimulatedOffline) Color(0xFF4ADE80) else Color(0xFFFCA5A5)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("simulate_network_toggle")
                ) {
                    Icon(
                        imageVector = if (isSimulatedOffline) Icons.Default.NetworkCheck else Icons.Default.SignalCellularConnectedNoInternet0Bar,
                        contentDescription = if (isSimulatedOffline) "Reconnect network" else "Simulate network loss",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSimulatedOffline) "Reconnect" else "Simulate Lost Net",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTestFcmAlert,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("test_fcm_alert_button"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = "Test push notification alert", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test FCM Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onConnectLiveDispatch,
                    enabled = isNetworkConnected,
                    modifier = Modifier
                        .weight(1.2f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("connect_live_dispatch_button"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0284C7),
                        disabledContainerColor = Color(0xFF475569)
                    )
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = "Connect to live dispatch system", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Connect Live Dispatch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
