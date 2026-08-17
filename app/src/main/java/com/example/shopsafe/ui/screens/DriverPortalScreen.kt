package com.example.shopsafe.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.shopsafe.ui.ShopSafeViewModel
import com.example.shopsafe.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class DriverNavigationTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    MAP("Map", Icons.Default.Map),
    EARNINGS("Earnings", Icons.Default.AccountBalanceWallet),
    ACTIVITY("Activity", Icons.Default.History),
    MESSAGES("Messages", Icons.Default.Chat),
    ACCOUNT("Account", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverPortalScreen(
    viewModel: ShopSafeViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State Collection
    val profile by viewModel.driverProfile.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val availableOffers by viewModel.availableDriverOffers.collectAsState()
    val activeOffer by viewModel.activeDriverOffer.collectAsState()
    val deliveryStep by viewModel.driverDeliveryStep.collectAsState()
    val isNetworkConnected by viewModel.isNetworkConnected.collectAsState()
    val isLowPowerModeEnabled by viewModel.isLowPowerModeEnabled.collectAsState()

    // Dispatch Ping State & AI Real Order State
    val isDispatchActive by viewModel.isDispatchActive.collectAsState()
    val dispatchOffer by viewModel.incomingDispatchOffer.collectAsState()
    val timerSeconds by viewModel.dispatchTimerSeconds.collectAsState()
    val isInAppNavigationActive by viewModel.isInAppNavigationActive.collectAsState()
    val driverTripProgress by viewModel.driverTripProgress.collectAsState()
    val realOrdersPendingDispatch by viewModel.realOrdersPendingDispatch.collectAsState()
    val isAiProcessingOrder by viewModel.isAiProcessingOrder.collectAsState()
    val aiOrderDispatchAnalysis by viewModel.aiOrderDispatchAnalysis.collectAsState()
    val selectedRealOrderForAi by viewModel.selectedRealOrderForAi.collectAsState()
    val showAiOrderProcessingSheet by viewModel.showAiOrderProcessingSheet.collectAsState()

    // Modals
    val showRequestPayoutScreen by viewModel.showRequestPayoutScreen.collectAsState()
    val showDemandHeatmap by viewModel.showDriverDemandHeatmapModal.collectAsState()
    val showAddStoreModal by viewModel.showAddStoreModal.collectAsState()
    val showMerchantPassModal by viewModel.showMerchantIntegrationPassModal.collectAsState()
    val showPriceMismatchModal by viewModel.showPriceAdjustmentModal.collectAsState()
    val showDriverPhotoScannerModal by viewModel.showDriverPhotoScannerModal.collectAsState()
    val showMileageTripCompletedDialog by viewModel.showMileageTripCompletedDialog.collectAsState()
    val lastRecordedTripMiles by viewModel.lastRecordedTripMiles.collectAsState()
    val lastRecordedTaxDeduction by viewModel.lastRecordedTaxDeduction.collectAsState()

    // Map & Hotspot Engine State (Reflecting Real Online Delivery Orders)
    val orders by viewModel.orders.collectAsState()
    val firestoreOrders by viewModel.firestoreOrders.collectAsState()
    var selectedNavTab by remember { mutableStateOf(DriverNavigationTab.MAP) }
    var selectedFilter by remember { mutableStateOf(DriverMapFilter.ALL) }
    val opportunityHotspots = remember(orders, firestoreOrders) { 
        DriverOpportunityEngine.generateOpportunityHotspotsFromOrders(firestoreOrders + orders) 
    }
    var selectedHotspot by remember { mutableStateOf<DriverOpportunityHotspot?>(null) }
    var detailsHotspot by remember { mutableStateOf<DriverOpportunityHotspot?>(null) }
    var showExpandableMenu by remember { mutableStateOf(false) }
    var showSafetySOSModal by remember { mutableStateOf(false) }
    var showPreShiftChecklist by remember { mutableStateOf(false) }
    var showDeliveryCelebrationModal by remember { mutableStateOf(false) }
    var celebrationEarnedAmount by remember { mutableDoubleStateOf(24.50) }

    // Live Online Shift Timer
    val isOnline = profile?.isOnline ?: true
    var onlineShiftSeconds by remember { mutableIntStateOf(4850) } // ~1h 20m

    LaunchedEffect(isOnline) {
        if (isOnline) {
            while (true) {
                delay(1000L)
                onlineShiftSeconds++
            }
        }
    }

    val shiftHours = onlineShiftSeconds / 3600
    val shiftMins = (onlineShiftSeconds % 3600) / 60
    val shiftSecs = onlineShiftSeconds % 60
    val formattedShiftTime = String.format("%02d:%02d:%02d", shiftHours, shiftMins, shiftSecs)

    // Pre-Shift Checklist Modal Trigger
    if (showPreShiftChecklist) {
        PreShiftChecklistModal(
            onDismiss = { showPreShiftChecklist = false },
            onAcknowledge = {
                showPreShiftChecklist = false
                viewModel.toggleDriverOnline(true)
            }
        )
    }

    // Root Container: Google Maps as Primary Screen Layer
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // LAYER 0: Persistent Google Maps Canvas
        DriverInteractiveGoogleMap(
            modifier = Modifier.fillMaxSize(),
            isOnline = isOnline,
            hotspots = opportunityHotspots,
            selectedHotspot = selectedHotspot,
            selectedFilter = selectedFilter,
            activeOffer = activeOffer,
            deliveryStep = deliveryStep,
            isLowPowerMode = isLowPowerModeEnabled,
            driverTripProgress = driverTripProgress,
            isInAppNavigation = isInAppNavigationActive,
            onHotspotClick = { hotspot ->
                selectedHotspot = hotspot
            },
            onMapClick = {
                selectedHotspot = null
            },
            onRecenterClick = {
                selectedHotspot = null
            }
        )

        // LAYER 1: Top Floating Header Bar (Online Status, Timer, Earnings, SOS) - Hidden during active delivery
        if (activeOffer == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter)
            ) {
                // Floating Status Pill
                Surface(
                    color = Color(0xFF0F172A).copy(alpha = 0.94f),
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Online/Offline Toggle Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isOnline) Color(0xFF065F46) else Color(0xFF334155))
                                .clickable {
                                    if (!isOnline) {
                                        showPreShiftChecklist = true
                                    } else {
                                        viewModel.toggleDriverOnline(false)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (isOnline) Color(0xFF34D399) else Color(0xFF94A3B8),
                                shape = CircleShape,
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOnline) "ONLINE" else "OFFLINE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Online Timer & Today's Earnings
                        if (isOnline) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(formattedShiftTime, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("•", color = Color(0xFF64748B), fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("$142.50", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Action Icons: Low Power + SOS Safety Shield
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.toggleLowPowerMode() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.BatterySaver,
                                    contentDescription = "Low Power Mode",
                                    tint = if (isLowPowerModeEnabled) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // SOS Red Shield Button
                            IconButton(
                                onClick = { showSafetySOSModal = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFFDC2626), CircleShape)
                                    .testTag("driver_sos_button")
                            ) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = "Emergency SOS & Safety",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Floating Map Filter Chips Bar (Horizontal Scroll)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DriverMapFilter.values().forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B).copy(alpha = 0.92f),
                            shape = RoundedCornerShape(14.dp),
                            shadowElevation = 4.dp,
                            modifier = Modifier.clickable { selectedFilter = filter }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(filter.icon, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = filter.label,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // LAYER 2: Floating Quick Menu Button (ShopSafe Shield) - Hidden during active delivery
        if (activeOffer == null && selectedNavTab == DriverNavigationTab.MAP) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = { showExpandableMenu = true },
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("shopsafe_driver_quick_menu_fab")
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "ShopSafe Menu", modifier = Modifier.size(24.dp))
                }
            }
        }

        // LAYER 3: Auto-Dispatch Radar HUD / Amenity Hotspot Preview (When No Active Delivery)
        if (activeOffer == null && selectedNavTab == DriverNavigationTab.MAP) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 76.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (selectedHotspot != null) {
                    DriverOpportunityPreviewCard(
                        hotspot = selectedHotspot!!,
                        onAccept = { hotspot ->
                            // For surge/amenity hotspots, navigate to the zone
                            selectedHotspot = null
                        },
                        onViewDetails = { hotspot ->
                            detailsHotspot = hotspot
                        },
                        onDismiss = {
                            selectedHotspot = null
                        }
                    )
                } else {
                    // Floating Auto-Dispatch Radar Pill Card
                    Surface(
                        color = Color(0xFF0F172A).copy(alpha = 0.95f),
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("driver_auto_dispatch_radar_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (isOnline) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF334155),
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (isOnline) Icons.Default.Radar else Icons.Default.PauseCircle,
                                            contentDescription = "Radar",
                                            tint = if (isOnline) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isOnline) "✨ AI Real Order Dispatch Active" else "⏸️ Driver is Offline",
                                        color = if (isOnline) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isOnline) {
                                            if (realOrdersPendingDispatch.isNotEmpty()) "🔥 ${realOrdersPendingDispatch.size} Real Order(s) pending AI dispatch"
                                            else "Gemini AI processing live customer orders..."
                                        } else "Go Online to receive order pings",
                                        color = if (realOrdersPendingDispatch.isNotEmpty()) Color(0xFF34D399) else Color(0xFFCBD5E1),
                                        fontSize = 11.sp,
                                        fontWeight = if (realOrdersPendingDispatch.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }

                            if (isOnline) {
                                Button(
                                    onClick = { viewModel.triggerNextAvailableDispatchPing() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("driver_test_ping_button")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (realOrdersPendingDispatch.isNotEmpty()) "Dispatch AI" else "AI Ping", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else {
                                Button(
                                    onClick = { showPreShiftChecklist = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Go Online", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // LAYER 4: Active Delivery In-Map HUD (When an Offer is Active)
        if (activeOffer != null && selectedNavTab == DriverNavigationTab.MAP) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 76.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                DriverActiveDeliveryHUD(
                    offer = activeOffer!!,
                    deliveryStep = deliveryStep,
                    viewModel = viewModel,
                    onAdvanceStep = {
                        if (deliveryStep == 3) {
                            showDeliveryCelebrationModal = true
                        }
                        viewModel.advanceDriverStep()
                    },
                    onCallCustomer = { phone ->
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    onMessageCustomer = {
                        selectedNavTab = DriverNavigationTab.MESSAGES
                    },
                    onOpenExternalNavigation = { address ->
                        try {
                            val uri = Uri.parse("google.navigation:q=${Uri.encode(address)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            context.startActivity(mapIntent)
                        } catch (_: Exception) {
                            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(address)}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                        }
                    }
                )
            }
        }

        // LAYER 5: Bottom Navigation Bar (MAP | EARNINGS | ACTIVITY | MESSAGES | ACCOUNT)
        Surface(
            color = Color(0xFF0F172A).copy(alpha = 0.98f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            shadowElevation = 12.dp
        ) {
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                modifier = Modifier.height(64.dp)
            ) {
                DriverNavigationTab.values().forEach { tab ->
                    val isSelected = selectedNavTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedNavTab = tab },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = tab.label,
                                tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFF64748B)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFF1E293B)
                        )
                    )
                }
            }
        }

        // LAYER 6: Secondary Overlays (Full Screen with Persistent "BACK TO MAP")
        when (selectedNavTab) {
            DriverNavigationTab.EARNINGS -> {
                DriverEarningsOverlay(
                    viewModel = viewModel,
                    onBackToMap = { selectedNavTab = DriverNavigationTab.MAP },
                    onRequestInstantPayout = { viewModel.showRequestPayoutScreen.value = true }
                )
            }
            DriverNavigationTab.ACTIVITY -> {
                DriverActivityOverlay(
                    viewModel = viewModel,
                    onBackToMap = { selectedNavTab = DriverNavigationTab.MAP }
                )
            }
            DriverNavigationTab.MESSAGES -> {
                DriverMessagesOverlay(
                    viewModel = viewModel,
                    onBackToMap = { selectedNavTab = DriverNavigationTab.MAP }
                )
            }
            DriverNavigationTab.ACCOUNT -> {
                DriverAccountOverlay(
                    viewModel = viewModel,
                    onBackToMap = { selectedNavTab = DriverNavigationTab.MAP }
                )
            }
            DriverNavigationTab.MAP -> {
                // Map is active in background
            }
        }

        // Modals & Bottom Sheets
        if (detailsHotspot != null) {
            DriverOrderDetailsSheet(
                hotspot = detailsHotspot!!,
                onAccept = { hotspot ->
                    val offer = DriverOffer(
                        storeName = hotspot.storeOrSellerName,
                        pickupAddress = hotspot.pickupAddress,
                        dropoffAddress = hotspot.dropoffAddress,
                        payAmount = hotspot.payAmount,
                        distanceMiles = hotspot.distanceMiles,
                        estimatedMins = hotspot.estimatedMins,
                        itemDetails = hotspot.itemSummary,
                        deliveryInstructions = hotspot.specialInstructions
                    )
                    viewModel.acceptDriverOffer(offer)
                    celebrationEarnedAmount = hotspot.payAmount
                    detailsHotspot = null
                    selectedHotspot = null
                },
                onDismiss = { detailsHotspot = null }
            )
        }

        if (showExpandableMenu) {
            DriverExpandableMenuSheet(
                onDismiss = { showExpandableMenu = false },
                onSelectShortcut = { shortcutId ->
                    when (shortcutId) {
                        "MAP" -> selectedNavTab = DriverNavigationTab.MAP
                        "EARNINGS", "BANKING" -> selectedNavTab = DriverNavigationTab.EARNINGS
                        "HISTORY", "MILEAGE" -> selectedNavTab = DriverNavigationTab.ACTIVITY
                        "MESSAGES" -> selectedNavTab = DriverNavigationTab.MESSAGES
                        "PERFORMANCE", "VEHICLES", "DOCUMENTS", "SETTINGS" -> selectedNavTab = DriverNavigationTab.ACCOUNT
                        "HOTSPOTS" -> viewModel.showDriverDemandHeatmapModal.value = true
                        "SHOP" -> viewModel.switchAppMode(com.example.shopsafe.ui.AppMode.CUSTOMER_SHOPPING)
                        "REFERRALS" -> viewModel.showReferralHubModal.value = true
                        "SUPPORT" -> showSafetySOSModal = true
                        "ORDERS" -> {
                            selectedNavTab = DriverNavigationTab.MAP
                            selectedHotspot = opportunityHotspots.firstOrNull()
                        }
                    }
                }
            )
        }

        if (showSafetySOSModal) {
            DriverSafetySOSModal(
                activeOrderId = activeOffer?.orderId,
                onDismiss = { showSafetySOSModal = false },
                onCallEmergency911 = {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                },
                onCallSafetyDispatch = {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:8007467726"))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                },
                onReportIncident = { _ ->
                    // Incident reported
                },
                onCancelDelivery = { _ ->
                    viewModel.activeDriverOffer.value = null
                }
            )
        }

        // Automated Priority Dispatch Ping Modal (Hidden during active delivery navigation)
        if (activeOffer == null && isDispatchActive && dispatchOffer != null) {
            DriverDispatchPingModal(
                offer = dispatchOffer!!,
                timerSeconds = timerSeconds,
                onAccept = {
                    viewModel.acceptDispatchOffer()
                },
                onDecline = {
                    viewModel.denyDispatchOffer()
                }
            )
        }

        // Stripe Connect Payout Screen
        if (showRequestPayoutScreen) {
            RequestPayoutScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.showRequestPayoutScreen.value = false }
            )
        }

        // Demand Heatmap Modal
        if (showDemandHeatmap) {
            DriverDemandHeatmapModal(
                viewModel = viewModel,
                onClose = { viewModel.showDriverDemandHeatmapModal.value = false }
            )
        }

        // Add Store Modal
        if (showAddStoreModal) {
            AddStoreLocationModal(
                viewModel = viewModel,
                onDismiss = { viewModel.showAddStoreModal.value = false }
            )
        }

        // Merchant Pass Modal
        if (showMerchantPassModal) {
            MerchantIntegrationPassModal(
                viewModel = viewModel,
                onDismiss = { viewModel.showMerchantIntegrationPassModal.value = false }
            )
        }

        // Price Adjustment Modal
        if (showPriceMismatchModal) {
            PriceMismatchModal(
                viewModel = viewModel,
                currentOrderTotal = activeOffer?.payAmount ?: 18.50,
                onDismiss = { viewModel.showPriceAdjustmentModal.value = false }
            )
        }

        // Driver Photo Scanner Modal
        if (showDriverPhotoScannerModal) {
            DriverPhotoScannerModal(
                viewModel = viewModel,
                onDismiss = { viewModel.showDriverPhotoScannerModal.value = false }
            )
        }

        // Delivery Completion Celebration Dialog
        if (showDeliveryCelebrationModal) {
            AlertDialog(
                onDismissRequest = { showDeliveryCelebrationModal = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎉 Delivery Completed!", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "+$${String.format(java.util.Locale.US, "%.2f", celebrationEarnedAmount)} Credited to Wallet",
                            color = Color(0xFF10B981),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• 100% Customer tip included\n• Automated IRS mileage log recorded\n• Available for instant transfer to your debit card via Stripe Connect.")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeliveryCelebrationModal = false
                            selectedNavTab = DriverNavigationTab.MAP
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("BACK TO OPPORTUNITY MAP")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeliveryCelebrationModal = false
                            selectedNavTab = DriverNavigationTab.EARNINGS
                        }
                    ) {
                        Text("VIEW EARNINGS")
                    }
                }
            )
        }

        // Automated Mileage Recorded Dialog
        if (showMileageTripCompletedDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissMileageCompletedDialog() },
                title = { Text("🚗 Delivery Mileage Logged", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("• Delivery Miles: ${String.format(java.util.Locale.US, "%.2f", lastRecordedTripMiles)} mi")
                        Text("• IRS Tax Deduction: $${String.format(java.util.Locale.US, "%.2f", lastRecordedTaxDeduction)} ($0.67/mi)")
                        Text("• Excludes home commute miles in compliance with IRS courier guidelines.")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissMileageCompletedDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("OK")
                    }
                }
            )
        }
        // Gemini AI Real Order Dispatch Review Sheet (Hidden during active delivery navigation)
        if (activeOffer == null && showAiOrderProcessingSheet && selectedRealOrderForAi != null) {
            AiOrderDispatchSheet(
                viewModel = viewModel,
                order = selectedRealOrderForAi,
                analysis = aiOrderDispatchAnalysis,
                isLoading = isAiProcessingOrder,
                onDismiss = { viewModel.dismissAiOrderProcessingSheet() },
                onAcceptAndStart = { order, analysis ->
                    viewModel.dispatchRealOrderWithAi(order, analysis)
                }
            )
        }
    }
}

@Composable
fun PreShiftChecklistModal(
    onDismiss: () -> Unit,
    onAcknowledge: () -> Unit
) {
    var checkTires by remember { mutableStateOf(false) }
    var checkBrakes by remember { mutableStateOf(false) }
    var checkLights by remember { mutableStateOf(false) }
    var checkDamage by remember { mutableStateOf(false) }

    val allChecked = checkTires && checkBrakes && checkLights && checkDamage

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pre-Shift Vehicle Inspection", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Please confirm your vehicle is safe to drive before going online.", fontSize = 14.sp)
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { checkTires = !checkTires }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(checked = checkTires, onCheckedChange = { checkTires = it })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tires have sufficient tread and pressure", fontSize = 14.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { checkBrakes = !checkBrakes }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(checked = checkBrakes, onCheckedChange = { checkBrakes = it })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Brakes are functioning normally", fontSize = 14.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { checkLights = !checkLights }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(checked = checkLights, onCheckedChange = { checkLights = it })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Headlights, taillights, and indicators work", fontSize = 14.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { checkDamage = !checkDamage }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(checked = checkDamage, onCheckedChange = { checkDamage = it })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("No new or unreported vehicle damage", fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAcknowledge,
                enabled = allChecked
            ) {
                Text("Acknowledge & Go Online")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
