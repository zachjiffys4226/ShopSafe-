package com.example.shopsafe.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopsafe.data.models.DriverOffer
import com.example.shopsafe.ui.ShopSafeViewModel

/**
 * In-Map Active Delivery HUD with In-App Google Maps Navigation,
 * Instant Navigate option right after accepting, and Non-Intrusive Arrival Proximity Detection.
 * Designed to NEVER cover driver map navigation, directions, or GPS vehicle location.
 */
@Composable
fun DriverActiveDeliveryHUD(
    offer: DriverOffer,
    deliveryStep: Int,
    viewModel: ShopSafeViewModel,
    modifier: Modifier = Modifier,
    onAdvanceStep: () -> Unit,
    onCallCustomer: (String) -> Unit,
    onMessageCustomer: () -> Unit,
    onOpenExternalNavigation: (String) -> Unit
) {
    val context = LocalContext.current
    var showPhotoVerifyDialog by remember { mutableStateOf(false) }
    var showSafeCardConfirmDialog by remember { mutableStateOf(false) }

    // Navigation and Arrival state from ViewModel
    val isInAppNavigationActive by viewModel.isInAppNavigationActive.collectAsState()
    val driverTripProgress by viewModel.driverTripProgress.collectAsState()
    val isAutoDriveSimulating by viewModel.isAutoDriveSimulating.collectAsState()
    val showArrivalPromptModal by viewModel.showArrivalPromptModal.collectAsState()
    val arrivalPromptTarget by viewModel.arrivalPromptTarget.collectAsState()
    val showAcceptedNavigateQuickPrompt by viewModel.showAcceptedNavigateQuickPrompt.collectAsState()

    // Map 12 Stages to current internal deliveryStep
    val (stepTitle, stepInstruction, stepColor, buttonText, buttonIcon) = when (deliveryStep) {
        1 -> Quintuple(
            "STEP 1-4: PICKUP AT STORE",
            "Drive to ${offer.storeName}\n${offer.pickupAddress}",
            Color(0xFF0284C7),
            "ARRIVED & CONFIRM PICKUP",
            Icons.Default.Storefront
        )
        2 -> Quintuple(
            "STEP 5-8: EN ROUTE TO CUSTOMER",
            "Drive to customer drop-off location\n${offer.dropoffAddress}",
            Color(0xFF10B981),
            "ARRIVED AT CUSTOMER",
            Icons.Default.Navigation
        )
        3 -> Quintuple(
            "STEP 9-10: PROOF OF DELIVERY",
            "Hand items to customer or leave safely at front door with photo proof",
            Color(0xFFF59E0B),
            "TAKE PHOTO & COMPLETE ($${String.format(java.util.Locale.US, "%.2f", offer.payAmount)})",
            Icons.Default.CameraAlt
        )
        else -> Quintuple(
            "DELIVERY COMPLETED",
            "Payout credited to wallet: +$${String.format(java.util.Locale.US, "%.2f", offer.payAmount)}",
            Color(0xFF10B981),
            "BACK TO OPPORTUNITY MAP",
            Icons.Default.CheckCircle
        )
    }

    val isPickupArrival = arrivalPromptTarget == "PICKUP" || deliveryStep == 1
    val arrivalDestinationName = if (isPickupArrival) offer.storeName else offer.customerName
    val arrivalDestinationAddress = if (isPickupArrival) offer.pickupAddress else offer.dropoffAddress

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // NON-INTRUSIVE AUTOMATIC ARRIVAL PROXIMITY CARD (Docked in HUD, NEVER covers map/GPS)
        AnimatedVisibility(
            visible = showArrivalPromptModal,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF10B981)),
                shadowElevation = 14.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag("driver_arrival_prompt_modal")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.25f),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = "Arrival",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "📍 ARRIVAL DETECTED",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Near $arrivalDestinationName",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.dismissArrivalPrompt() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = arrivalDestinationAddress,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.confirmArrivalAtCurrentStop()
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("confirm_arrival_yes_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("✅ YES, ARRIVED", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { viewModel.dismissArrivalPrompt() },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1))
                        ) {
                            Text("Keep Driving", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // QUICK NAVIGATE CARD (Displayed right after accepting offer)
        AnimatedVisibility(
            visible = showAcceptedNavigateQuickPrompt,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFF10B981)))
                ),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag("driver_accepted_quick_navigate_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "OFFER ACCEPTED",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Ready to Navigate to ${offer.storeName}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.dismissAcceptedQuickPrompt() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.startInAppNavigation()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("start_inapp_navigation_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🧭 Start In-App Navigation", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.dismissAcceptedQuickPrompt()
                                val targetAddress = if (deliveryStep == 1) offer.pickupAddress else offer.dropoffAddress
                                onOpenExternalNavigation(targetAddress)
                            },
                            modifier = Modifier
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // 1. Top Turn-by-Turn In-App Navigation Pill
        Surface(
            color = Color(0xFF0F172A).copy(alpha = 0.96f),
            shape = RoundedCornerShape(18.dp),
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = stepColor,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(buttonIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stepTitle,
                                    color = stepColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                if (isAutoDriveSimulating) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "LIVE DRIVING",
                                            color = Color(0xFF10B981),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = stepInstruction,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2
                            )
                        }
                    }

                    // Navigation Controls Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Toggle Driving Simulation Play/Pause
                        IconButton(
                            onClick = { viewModel.toggleAutoDriveSimulation() },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF1E293B), CircleShape)
                        ) {
                            Icon(
                                if (isAutoDriveSimulating) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Simulate Driving Progress",
                                tint = if (isAutoDriveSimulating) Color(0xFF10B981) else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Fast-Forward to Near Destination (Proximity Tester)
                        IconButton(
                            onClick = {
                                val target = if (deliveryStep == 1) "PICKUP" else "DROPOFF"
                                viewModel.simulateApproachingLocation(target)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF1E293B), CircleShape)
                                .testTag("driver_test_approach_destination_button")
                        ) {
                            Icon(
                                Icons.Default.FastForward,
                                contentDescription = "Simulate Approaching Destination",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // External Google Maps Button
                        IconButton(
                            onClick = {
                                val targetAddress = if (deliveryStep == 1) offer.pickupAddress else offer.dropoffAddress
                                onOpenExternalNavigation(targetAddress)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF1E293B), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Directions,
                                contentDescription = "Open in Google Maps App",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Dynamic Live Route Progress Bar
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val distRemaining = String.format(java.util.Locale.US, "%.1f mi", (offer.distanceMiles * (1.0 - driverTripProgress.toDouble())).coerceAtLeast(0.1))
                        val etaMins = ((offer.estimatedMins * (1f - driverTripProgress)).toInt()).coerceAtLeast(1)
                        Text(
                            text = "ETA: $etaMins min ($distRemaining remaining)",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(driverTripProgress * 100).toInt()}% en route",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { driverTripProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (driverTripProgress >= 0.75f) Color(0xFF10B981) else Color(0xFF0284C7),
                        trackColor = Color(0xFF334155)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Active Order Card & Communication Actions
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.96f)),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Customer / Store Info & Pay
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = offer.storeName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${offer.itemDetails} • ${offer.distanceMiles} mi",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }

                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "$${String.format(java.util.Locale.US, "%.2f", offer.payAmount)}",
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // In-Map Communication Bar: Masked Call, Chat, Safe Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Call (Masked Number)
                    OutlinedButton(
                        onClick = { onCallCustomer("(555) 890-1234") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 12.sp)
                    }

                    // Message
                    OutlinedButton(
                        onClick = onMessageCustomer,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Message", fontSize = 12.sp)
                    }

                    // Safe Card / Verification
                    Button(
                        onClick = { showSafeCardConfirmDialog = true },
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569))
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Safe Card", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Primary Advancement Action Button (Large Touch Target)
                Button(
                    onClick = {
                        if (deliveryStep == 3) {
                            showPhotoVerifyDialog = true
                        } else {
                            onAdvanceStep()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("advance_delivery_step_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = stepColor)
                ) {
                    Icon(buttonIcon, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buttonText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Safe Card Modal Dialog
    if (showSafeCardConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSafeCardConfirmDialog = false },
            title = { Text("💳 ShopSafe Pre-Loaded Card", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Safe Card balance is authorized for order: ${offer.orderId.ifBlank { "ORD-4491" }}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Authorized Amount: $42.50\n• Card Number: •••• 9921\n• Expiry: 08/28\n• Swipe or Tap at checkout register with zero driver personal liability.")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSafeCardConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("OK, GOT IT")
                }
            }
        )
    }

    // Photo Proof of Delivery Dialog
    if (showPhotoVerifyDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoVerifyDialog = false },
            title = { Text("📸 Proof of Delivery Photo", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Capture clear photo of the delivery package at customer location: ${offer.dropoffAddress}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("📸 Live Camera Snapshot Simulated", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPhotoVerifyDialog = false
                        onAdvanceStep()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("CONFIRM & COMPLETE TRIP")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoVerifyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
