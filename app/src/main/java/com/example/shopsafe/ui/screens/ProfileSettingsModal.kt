package com.example.shopsafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.shopsafe.data.util.AppLanguage
import com.example.shopsafe.data.util.LocalizedStrings
import com.example.shopsafe.ui.AppMode
import com.example.shopsafe.ui.ShopSafeViewModel

@Composable
fun ProfileSettingsModal(
    viewModel: ShopSafeViewModel,
    onClose: () -> Unit,
    onOpenSmartDispatcher: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val appMode by viewModel.appMode.collectAsState()
    val driverProfile by viewModel.driverProfile.collectAsState()
    val savedAddresses by viewModel.savedAddresses.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    var isDriverModeEnabled by remember { mutableStateOf(appMode == AppMode.DRIVER_PORTAL) }
    var newAddressInput by remember { mutableStateOf("") }
    var showProfileMenu by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(appMode) {
        isDriverModeEnabled = (appMode == AppMode.DRIVER_PORTAL)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                Surface(
                                    color = Color(0xFF0284C7).copy(alpha = 0.1f),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable { showProfileMenu = true }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "User profile icon (Tap for options)",
                                            tint = Color(0xFF0284C7)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = showProfileMenu,
                                    onDismissRequest = { showProfileMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Log Out", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
                                        onClick = {
                                            showProfileMenu = false
                                            viewModel.logoutUser()
                                            onClose()
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFDC2626))
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = LocalizedStrings.get("profile_settings", currentLanguage),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = currentUser?.email ?: "member@shopsafe.com",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close modal", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Scrollable Settings Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Multi-Language App Localization Card
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        color = Color(0xFF7C3AED).copy(alpha = 0.15f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Language,
                                                contentDescription = "Language settings icon",
                                                tint = Color(0xFF7C3AED),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = LocalizedStrings.get("app_language", currentLanguage),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = LocalizedStrings.get("select_language_subtitle", currentLanguage),
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            lineHeight = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Language Selection Grid (2 columns)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val languages = AppLanguage.values()
                                    val chunkedLangs = languages.toList().chunked(2)
                                    chunkedLangs.forEach { rowLangs ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowLangs.forEach { lang ->
                                                val isSelected = currentLanguage == lang
                                                Surface(
                                                    onClick = { viewModel.setAppLanguage(lang) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (isSelected) Color(0xFF7C3AED) else Color.White,
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        width = if (isSelected) 2.dp else 1.dp,
                                                        color = if (isSelected) Color(0xFF7C3AED) else Color(0xFFCBD5E1)
                                                    ),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .defaultMinSize(minHeight = 48.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(lang.flagEmoji, fontSize = 16.sp)
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Column {
                                                                Text(
                                                                    text = lang.nativeName,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 12.sp,
                                                                    color = if (isSelected) Color.White else Color(0xFF0F172A)
                                                                )
                                                                Text(
                                                                    text = lang.englishName,
                                                                    fontSize = 10.sp,
                                                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray
                                                                )
                                                            }
                                                        }
                                                        if (isSelected) {
                                                            Icon(
                                                                Icons.Default.CheckCircle,
                                                                contentDescription = "Selected language indicator",
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
                            }
                        }

                        // Refer a Friend & Delivery Fee Credits Card
                        val availableCredits by viewModel.availableDeliveryCredits.collectAsState()
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
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
                                            color = Color(0xFF16A34A).copy(alpha = 0.15f),
                                            shape = CircleShape,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.CardGiftcard,
                                                    contentDescription = "Referral icon",
                                                    tint = Color(0xFF16A34A),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Refer a Friend",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "Get $5.00 delivery credit per referral",
                                                fontSize = 11.sp,
                                                color = Color(0xFF166534)
                                            )
                                        }
                                    }

                                    Surface(
                                        color = Color(0xFF16A34A),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(
                                            text = "$${String.format("%.2f", availableCredits)} Credits",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        onClose()
                                        viewModel.showReferralHubModal.value = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                ) {
                                    Icon(Icons.Default.Redeem, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open Referral Hub", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        // Driver Mode Toggle Section
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = Color(0xFF16A34A).copy(alpha = 0.15f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.LocalShipping,
                                                contentDescription = "Driver mode icon",
                                                tint = Color(0xFF16A34A)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = LocalizedStrings.get("driver_mode", currentLanguage),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = LocalizedStrings.get("driver_mode_desc", currentLanguage),
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            lineHeight = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Switch(
                                    checked = isDriverModeEnabled,
                                    onCheckedChange = { enabled ->
                                        isDriverModeEnabled = enabled
                                        if (enabled) {
                                            viewModel.switchAppMode(AppMode.DRIVER_PORTAL)
                                            viewModel.toggleDriverOnline(true)
                                        } else {
                                            viewModel.switchAppMode(AppMode.CUSTOMER_SHOPPING)
                                        }
                                        onClose()
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF16A34A)
                                    )
                                )
                            }
                        }

                        // Account Details Card
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = LocalizedStrings.get("account_details", currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(LocalizedStrings.get("account_name", currentLanguage), fontSize = 12.sp, color = Color.Gray)
                                    Text(currentUser?.name ?: "ShopSafe Member", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(LocalizedStrings.get("membership_status", currentLanguage), fontSize = 12.sp, color = Color.Gray)
                                    Text("Verified VIP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(LocalizedStrings.get("stripe_payouts", currentLanguage), fontSize = 12.sp, color = Color.Gray)
                                    Text(driverProfile?.stripeConnectAccountId ?: "acct_1Nzk4hShopSafeDriver", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                }
                            }
                        }

                        // Saved Delivery Addresses Card (DataStore Cached)
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = LocalizedStrings.get("saved_addresses", currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = LocalizedStrings.get("saved_addresses_desc", currentLanguage),
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                savedAddresses.forEach { address ->
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = address,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF0F172A)
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.removeSavedAddress(address) },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete address", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newAddressInput,
                                        onValueChange = { newAddressInput = it },
                                        placeholder = { Text(LocalizedStrings.get("add_address_placeholder", currentLanguage), fontSize = 11.sp) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .defaultMinSize(minHeight = 48.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (newAddressInput.isNotBlank()) {
                                                viewModel.addSavedAddress(newAddressInput.trim())
                                                newAddressInput = ""
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                        contentPadding = PaddingValues(horizontal = 12.dp),
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add address button icon", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(LocalizedStrings.get("save", currentLanguage), fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // ML Smart Order Dispatcher Button
                        OutlinedButton(
                            onClick = {
                                onClose()
                                onOpenSmartDispatcher()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7))
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = "ML Dispatcher icon", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(LocalizedStrings.get("open_ml_dispatcher", currentLanguage), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Actions (Log Out & Done)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.logoutUser()
                                onClose()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Button(
                            onClick = onClose,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Text(LocalizedStrings.get("done", currentLanguage), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
