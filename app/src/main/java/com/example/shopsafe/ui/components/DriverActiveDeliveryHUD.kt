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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.shopsafe.data.models.DriverOffer
import com.example.shopsafe.ui.ShopSafeViewModel

/**
 * In-Map Active Delivery HUD with In-App Google Maps Navigation,
 * Instant Navigate option right after accepting, and Automatic Arrival Proximity Detection.
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
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

        // (Top Turn-by-Turn status updates and instruction banners removed to keep map view unobstructed and simplified)


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

    // AUTOMATIC ARRIVAL PROXIMITY CONFIRMATION (Handled inline in bottom HUD without obstructing map view)
    LaunchedEffect(showArrivalPromptModal) {
        if (showArrivalPromptModal) {
            // Auto-acknowledge arrival state in ViewModel without popping up an obstructing modal dialog over the map
            viewModel.confirmArrivalAtCurrentStop()
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
