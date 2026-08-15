package com.example.shopsafe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopsafe.data.util.LocalizedStrings
import com.example.shopsafe.ui.AppMode
import com.example.shopsafe.ui.CustomerTab
import com.example.shopsafe.ui.ShopSafeViewModel
import com.example.shopsafe.ui.components.AdminControlDashboard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: ShopSafeViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val mode by viewModel.appMode.collectAsState()
    val currentTab by viewModel.customerTab.collectAsState()
    val callState by viewModel.activeCallState.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    var showAdminDashboard by remember { mutableStateOf(false) }
    var showProfileSettings by remember { mutableStateOf(false) }
    var showSmartDispatcher by remember { mutableStateOf(false) }

    if (currentUser == null) {
        LandingScreen(viewModel = viewModel)
        return
    }

    Scaffold(
        topBar = {
            if (mode == AppMode.CUSTOMER_SHOPPING) {
                Surface(
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo & App Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFF0284C7),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "ShopSafe",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ShopSafe",
                                color = Color(0xFF0F172A),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Mode Switcher Pill (Customer Shopping vs Driver Portal)
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(modifier = Modifier.padding(2.dp)) {
                                    Surface(
                                        color = if (mode == AppMode.CUSTOMER_SHOPPING) Color(0xFF0284C7) else Color.Transparent,
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier.padding(2.dp)
                                    ) {
                                        TextButton(
                                            onClick = { viewModel.switchAppMode(AppMode.CUSTOMER_SHOPPING) },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Shop",
                                                color = if (mode == AppMode.CUSTOMER_SHOPPING) Color.White else Color(0xFF475569),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Surface(
                                        color = if (mode == AppMode.DRIVER_PORTAL) Color(0xFF16A34A) else Color.Transparent,
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier.padding(2.dp)
                                    ) {
                                        TextButton(
                                            onClick = { viewModel.switchAppMode(AppMode.DRIVER_PORTAL) },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Driver Portal",
                                                color = if (mode == AppMode.DRIVER_PORTAL) Color.White else Color(0xFF475569),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Profile & Settings Button
                            IconButton(onClick = { showProfileSettings = true }) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile Settings & Driver Mode",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (mode == AppMode.CUSTOMER_SHOPPING) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = currentTab == CustomerTab.MARKETPLACE,
                        onClick = { viewModel.selectCustomerTab(CustomerTab.MARKETPLACE) },
                        icon = { Icon(Icons.Default.Storefront, contentDescription = "Marketplace") },
                        label = { Text(LocalizedStrings.get("marketplace", currentLanguage)) }
                    )

                    NavigationBarItem(
                        selected = currentTab == CustomerTab.FOOD_STORES,
                        onClick = { viewModel.selectCustomerTab(CustomerTab.FOOD_STORES) },
                        icon = { Icon(Icons.Default.Fastfood, contentDescription = "ShopSafe Food Delivery") },
                        label = { Text(LocalizedStrings.get("food_stores", currentLanguage)) }
                    )

                    NavigationBarItem(
                        selected = currentTab == CustomerTab.CART,
                        onClick = { viewModel.selectCustomerTab(CustomerTab.CART) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (cartItems.isNotEmpty()) {
                                        Badge { Text("${cartItems.size}") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                            }
                        },
                        label = { Text(LocalizedStrings.get("cart", currentLanguage)) }
                    )

                    NavigationBarItem(
                        selected = currentTab == CustomerTab.ORDERS,
                        onClick = { viewModel.selectCustomerTab(CustomerTab.ORDERS) },
                        icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Live Orders") },
                        label = { Text(LocalizedStrings.get("orders", currentLanguage)) }
                    )

                    NavigationBarItem(
                        selected = currentTab == CustomerTab.MESSAGES,
                        onClick = { viewModel.selectCustomerTab(CustomerTab.MESSAGES) },
                        icon = { Icon(Icons.Default.Chat, contentDescription = "Messages") },
                        label = { Text(LocalizedStrings.get("messages", currentLanguage)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (mode == AppMode.DRIVER_PORTAL) {
                DriverPortalScreen(viewModel = viewModel)
            } else {
                when (currentTab) {
                    CustomerTab.MARKETPLACE -> MarketplaceScreen(viewModel = viewModel)
                    CustomerTab.FOOD_STORES -> FoodAndStoresScreen(viewModel = viewModel)
                    CustomerTab.CART -> CartAndCheckoutScreen(viewModel = viewModel)
                    CustomerTab.ORDERS -> OrderTrackingScreen(viewModel = viewModel)
                    CustomerTab.MESSAGES -> MessagingScreen(viewModel = viewModel)
                }
            }

            // Live Encrypted In-App Call Screen Overlay
            InAppCallScreen(
                callState = callState,
                onMuteToggle = { viewModel.toggleMuteCall() },
                onSpeakerToggle = { viewModel.toggleSpeakerCall() },
                onEndCall = { viewModel.endCall() }
            )

            // Float Admin Button if logged in as Admin
            if (currentUser?.isAdmin == true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp, end = 24.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { showAdminDashboard = true },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White) },
                        text = { Text("Admin Control", fontWeight = FontWeight.Bold) },
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(6.dp)
                    )
                }
            }

            if (showAdminDashboard) {
                AdminControlDashboard(
                    viewModel = viewModel,
                    onClose = { showAdminDashboard = false }
                )
            }

            if (showProfileSettings) {
                ProfileSettingsModal(
                    viewModel = viewModel,
                    onClose = { showProfileSettings = false },
                    onOpenSmartDispatcher = { showSmartDispatcher = true }
                )
            }

            if (showSmartDispatcher) {
                SmartDispatcherModal(
                    viewModel = viewModel,
                    onClose = { showSmartDispatcher = false }
                )
            }

            val showReferralHubModal by viewModel.showReferralHubModal.collectAsState()
            if (showReferralHubModal) {
                ReferralHubModal(
                    viewModel = viewModel,
                    onClose = { viewModel.showReferralHubModal.value = false }
                )
            }
        }
    }
}
