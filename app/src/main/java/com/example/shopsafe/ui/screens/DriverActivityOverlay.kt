package com.example.shopsafe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopsafe.data.models.CompletedDelivery
import com.example.shopsafe.ui.ShopSafeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverActivityOverlay(
    viewModel: ShopSafeViewModel,
    onBackToMap: () -> Unit
) {
    val completedDeliveries by viewModel.completedDeliveries.collectAsState()
    val mileageLogs by viewModel.allMileageLogs.collectAsState()
    val realOrdersPending by viewModel.realOrdersPendingDispatch.collectAsState()

    var activeSubTab by remember { mutableStateOf(if (realOrdersPending.isNotEmpty()) "REAL_ORDERS" else "DELIVERIES") } // REAL_ORDERS, DELIVERIES, MILEAGE_LOG
    var searchQuery by remember { mutableStateOf("") }
    var selectedDeliveryReceipt by remember { mutableStateOf<CompletedDelivery?>(null) }

    val filteredDeliveries = remember(completedDeliveries, searchQuery) {
        if (searchQuery.isBlank()) completedDeliveries
        else completedDeliveries.filter {
            it.storeOrSellerName.contains(searchQuery, ignoreCase = true) ||
            it.dropoffAddress.contains(searchQuery, ignoreCase = true) ||
            it.itemsSummary.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            Surface(
                color = Color(0xFF1E293B),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF0284C7),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.History, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Activity & Delivery Logs",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${completedDeliveries.size} Completed Trips Logged",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Persistent "BACK TO MAP" Button
                    Button(
                        onClick = onBackToMap,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("activity_back_to_map_button")
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BACK TO MAP", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Sub Tab Selector (Real Orders vs Completed Deliveries vs Mileage Logs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeSubTab == "REAL_ORDERS") Color(0xFF0284C7) else Color.Transparent)
                        .clickable { activeSubTab = "REAL_ORDERS" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Real Orders",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        if (realOrdersPending.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = Color(0xFF10B981),
                                shape = CircleShape,
                                modifier = Modifier.size(16.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${realOrdersPending.size}",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeSubTab == "DELIVERIES") Color(0xFF0284C7) else Color.Transparent)
                        .clickable { activeSubTab = "DELIVERIES" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Trips (${completedDeliveries.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeSubTab == "MILEAGE_LOG") Color(0xFF0284C7) else Color.Transparent)
                        .clickable { activeSubTab = "MILEAGE_LOG" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("IRS Log (${mileageLogs.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (activeSubTab == "REAL_ORDERS") {
                if (realOrdersPending.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "All Real Orders Dispatched",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Gemini AI is actively listening for new customer orders placed in the app.",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(realOrdersPending) { order ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "REAL ORDER",
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = order.storeOrSellerName,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        }

                                        Text(
                                            text = "$${String.format(java.util.Locale.US, "%.2f", order.total)}",
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Items: ${order.itemsSummary}",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 12.sp,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Drop-off: ${order.dropoffAddress.ifBlank { "123 Main St, San Francisco, CA" }}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.processRealOrderWithAi(order)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("AI Assessment", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.dispatchRealOrderWithAi(order)
                                                onBackToMap()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("AI Dispatch", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (activeSubTab == "DELIVERIES") {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by merchant, address, or items...", color = Color(0xFF64748B), fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredDeliveries) { delivery ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDeliveryReceipt = delivery },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(delivery.storeOrSellerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "+$${String.format(java.util.Locale.US, "%.2f", delivery.totalEarnings)}",
                                        color = Color(0xFF34D399),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${delivery.itemsSummary} • ${delivery.distanceMiles} mi (${delivery.durationMinutes} min)", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Drop-off: ${delivery.dropoffAddress.take(28)}...", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                    Text("Tip: +$${String.format(java.util.Locale.US, "%.2f", delivery.tipAmount)}", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            } else {
                // Mileage Log Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(mileageLogs) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(log.storeOrSellerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${String.format(java.util.Locale.US, "%.2f", log.milesDriven)} miles • ${log.durationMinutes} min", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text("Pickup: ${log.pickupAddress.take(24)}... ➡️ ${log.dropoffAddress.take(24)}...", color = Color(0xFF64748B), fontSize = 10.sp)
                                }
                                Surface(
                                    color = Color(0xFF065F46),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "+$${String.format(java.util.Locale.US, "%.2f", log.taxDeductionValue)} Tax Deduction",
                                        color = Color(0xFF6EE7B7),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detailed Delivery Receipt Bottom Sheet
    if (selectedDeliveryReceipt != null) {
        val receipt = selectedDeliveryReceipt!!
        ModalBottomSheet(
            onDismissRequest = { selectedDeliveryReceipt = null },
            containerColor = Color(0xFF0F172A),
            contentColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text("Delivery Receipt & Payout Breakdown", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(receipt.storeOrSellerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(receipt.itemsSummary, color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Base Pay", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Text("$${String.format(java.util.Locale.US, "%.2f", receipt.basePay)}", color = Color.White, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Customer Tip", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Text("+$${String.format(java.util.Locale.US, "%.2f", receipt.tipAmount)}", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        if (receipt.peakBonus > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Peak Surge Bonus", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                Text("+$${String.format(java.util.Locale.US, "%.2f", receipt.peakBonus)}", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFF334155))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Driver Earnings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("$${String.format(java.util.Locale.US, "%.2f", receipt.totalEarnings)}", color = Color(0xFF34D399), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { selectedDeliveryReceipt = null },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close Receipt")
                }
            }
        }
    }
}
