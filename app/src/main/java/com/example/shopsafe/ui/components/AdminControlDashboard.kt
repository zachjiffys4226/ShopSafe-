package com.example.shopsafe.ui.components

import androidx.compose.foundation.background
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
import com.example.shopsafe.data.models.MarketplaceItem
import com.example.shopsafe.data.models.Storefront
import com.example.shopsafe.data.models.FoodItem
import com.example.shopsafe.data.models.Order
import com.example.shopsafe.data.models.DriverProfile
import com.example.shopsafe.ui.ShopSafeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminControlDashboard(
    viewModel: ShopSafeViewModel,
    onClose: () -> Unit
) {
    var selectedSection by remember { mutableStateOf("METRICS") } // METRICS, MARKETPLACE, STORES, DRIVERS, ORDERS

    val marketplaceItems by viewModel.marketplaceItems.collectAsState()
    val storefronts by viewModel.storefronts.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val driverProfile by viewModel.driverProfile.collectAsState()

    // Driver Modification temporary inputs
    var overrideBalance by remember { mutableStateOf("") }
    var overrideTodayEarned by remember { mutableStateOf("") }
    var overrideWeekEarned by remember { mutableStateOf("") }
    var overrideCompletedToday by remember { mutableStateOf("") }

    LaunchedEffect(driverProfile) {
        driverProfile?.let {
            overrideBalance = it.currentBalance.toString()
            overrideTodayEarned = it.todayEarned.toString()
            overrideWeekEarned = it.weekEarned.toString()
            overrideCompletedToday = it.completedToday.toString()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A) // Sleek Premium Slate Dark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Admin Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Master Admin Panel",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ShopSafe Master Admin",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Enforced Encryption • Complete DB Override",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF334155))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Panel", tint = Color.White)
                }
            }

            // Quick navigation horizontal tab bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val sections = listOf(
                    "METRICS" to "Overview",
                    "MARKETPLACE" to "Products",
                    "STORES" to "Stores",
                    "DRIVERS" to "Drivers",
                    "ORDERS" to "Orders"
                )

                sections.forEach { (sec, label) ->
                    val isSel = selectedSection == sec
                    Surface(
                        color = if (isSel) Color(0xFFF59E0B) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedSection = sec }
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) Color(0xFF0F172A) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // Central scrollable workspace
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedSection) {
                    "METRICS" -> {
                        // System Metrics Overview Card
                        Text("System Dashboard & Global Controls", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Products", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text("${marketplaceItems.size}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Stores", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text("${storefronts.size}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Active Orders", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text("${orders.size}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Danger Zone Control Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("⚠️ System Override Controls", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "Perform direct memory/disk clearing and reset all customer and driver counts to clean slate.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )

                                Button(
                                    onClick = { viewModel.resetAllCountsToZero() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reset counts", tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reset Driver Profile & Pay Counts to Zero", color = Color.White)
                                }

                                Button(
                                    onClick = { 
                                        viewModel.logoutUser()
                                        onClose()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = "Sign out", tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Force Administrator Session Sign Out", color = Color.White)
                                }
                            }
                        }
                    }

                    "MARKETPLACE" -> {
                        Text("Active Marketplace Catalog (${marketplaceItems.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        
                        marketplaceItems.forEach { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Seller: ${item.sellerName} • Category: ${item.category}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        }
                                        Text("$${String.format("%.2f", item.price)}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.adminDeleteMarketplaceItem(item.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete item", tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Delete Item", fontSize = 11.sp, color = Color.White)
                                        }

                                        var showPriceDialog by remember { mutableStateOf(false) }
                                        var newPriceInput by remember { mutableStateOf(item.price.toString()) }

                                        Button(
                                            onClick = { showPriceDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit price", tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Set Price", fontSize = 11.sp, color = Color.White)
                                        }

                                        if (showPriceDialog) {
                                            AlertDialog(
                                                onDismissRequest = { showPriceDialog = false },
                                                title = { Text("Set New Price") },
                                                text = {
                                                    OutlinedTextField(
                                                        value = newPriceInput,
                                                        onValueChange = { newPriceInput = it },
                                                        label = { Text("Price ($)") },
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                },
                                                confirmButton = {
                                                    Button(onClick = {
                                                        val pr = newPriceInput.toDoubleOrNull() ?: item.price
                                                        viewModel.adminUpdateMarketplaceItemPrice(item, pr)
                                                        showPriceDialog = false
                                                    }) { Text("Update") }
                                                },
                                                dismissButton = {
                                                    TextButton(onClick = { showPriceDialog = false }) { Text("Cancel") }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "STORES" -> {
                        Text("Active Storefronts (${storefronts.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        
                        storefronts.forEach { store ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(store.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Category: ${store.category} • Address: ${store.address}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        }
                                        Surface(
                                            color = if (store.isOpen) Color(0xFF10B981) else Color(0xFFEF4444),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (store.isOpen) "Open" else "Closed",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.adminDeleteStorefront(store.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Delete Store", fontSize = 11.sp, color = Color.White)
                                        }

                                        Button(
                                            onClick = { viewModel.adminUpdateStorefrontOpenStatus(store.id, !store.isOpen) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(if (store.isOpen) "Close Store" else "Open Store", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "DRIVERS" -> {
                        Text("Override Live Driver Profile metrics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Driver Identity: ${driverProfile?.name ?: "No Live Profile Active"}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                                OutlinedTextField(
                                    value = overrideBalance,
                                    onValueChange = { overrideBalance = it },
                                    label = { Text("Driver Balance ($)") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedLabelColor = Color(0xFFF59E0B),
                                        unfocusedLabelColor = Color.LightGray
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = overrideTodayEarned,
                                    onValueChange = { overrideTodayEarned = it },
                                    label = { Text("Today's Earnings ($)") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedLabelColor = Color(0xFFF59E0B),
                                        unfocusedLabelColor = Color.LightGray
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = overrideWeekEarned,
                                    onValueChange = { overrideWeekEarned = it },
                                    label = { Text("Weekly Earnings ($)") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedLabelColor = Color(0xFFF59E0B),
                                        unfocusedLabelColor = Color.LightGray
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = overrideCompletedToday,
                                    onValueChange = { overrideCompletedToday = it },
                                    label = { Text("Completed Trips Today") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedLabelColor = Color(0xFFF59E0B),
                                        unfocusedLabelColor = Color.LightGray
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        val curProf = driverProfile ?: DriverProfile()
                                        val updated = curProf.copy(
                                            currentBalance = overrideBalance.toDoubleOrNull() ?: 0.0,
                                            todayEarned = overrideTodayEarned.toDoubleOrNull() ?: 0.0,
                                            weekEarned = overrideWeekEarned.toDoubleOrNull() ?: 0.0,
                                            completedToday = overrideCompletedToday.toIntOrNull() ?: 0
                                        )
                                        viewModel.adminOverrideDriverProfile(updated)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Apply Override to Driver Profile", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    "ORDERS" -> {
                        Text("Live Orders in DB (${orders.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        
                        orders.forEach { order ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Order: ${order.storeOrSellerName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Items: ${order.itemsSummary}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        }
                                        Text("$${String.format("%.2f", order.total)}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    // Display and edit status
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Status: ${order.status}", color = Color(0xFFBAE6FD), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            val statuses = listOf("PLACED", "ON_THE_WAY", "DELIVERED")
                                            statuses.forEach { st ->
                                                Surface(
                                                    color = if (order.status == st) Color(0xFF0284C7) else Color(0xFF334155),
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier.clickable {
                                                        viewModel.adminUpdateOrderStatus(order.id, st)
                                                    }
                                                ) {
                                                    Text(st, color = Color.White, fontSize = 8.sp, modifier = Modifier.padding(4.dp))
                                                }
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.adminDeleteOrder(order.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth().height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Delete Order Record", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
