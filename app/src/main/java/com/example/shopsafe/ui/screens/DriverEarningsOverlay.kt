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
import com.example.shopsafe.ui.ShopSafeViewModel

@Composable
fun DriverEarningsOverlay(
    viewModel: ShopSafeViewModel,
    onBackToMap: () -> Unit,
    onRequestInstantPayout: () -> Unit
) {
    val totalTaxDeduction by viewModel.totalMileageTaxDeduction.collectAsState()
    val totalMiles by viewModel.totalDeliveryMiles.collectAsState()
    val todayMiles by viewModel.todayDeliveryMileage.collectAsState()
    val payoutHistory by viewModel.payoutHistory.collectAsState()

    var selectedPeriod by remember { mutableStateOf("TODAY") } // TODAY, WEEK, MONTH, LIFETIME

    val earningsAmount = when (selectedPeriod) {
        "TODAY" -> 142.50
        "WEEK" -> 892.00
        "MONTH" -> 3420.00
        else -> 14250.00
    }

    val deliveriesCount = when (selectedPeriod) {
        "TODAY" -> 6
        "WEEK" -> 38
        "MONTH" -> 144
        else -> 590
    }

    val tipsAmount = when (selectedPeriod) {
        "TODAY" -> 42.00
        "WEEK" -> 268.00
        "MONTH" -> 1020.00
        else -> 4210.00
    }

    val surgeBonusAmount = when (selectedPeriod) {
        "TODAY" -> 18.00
        "WEEK" -> 114.00
        "MONTH" -> 440.00
        else -> 1850.00
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
                            color = Color(0xFF10B981),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Driver Earnings & Payouts",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "100% Tips + Automated IRS Mileage Deduction",
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
                        modifier = Modifier.testTag("earnings_back_to_map_button")
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BACK TO MAP", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Period Selector Tabs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("TODAY", "WEEK", "MONTH", "LIFETIME").forEach { period ->
                        val isSelected = selectedPeriod == period
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF10B981) else Color.Transparent)
                                .clickable { selectedPeriod = period }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Total Earnings Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "$selectedPeriod EARNINGS",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format(java.util.Locale.US, "%.2f", earningsAmount)}",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$deliveriesCount Completed Deliveries • $tipsAmount Customer Tips",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Instant Cash Out CTA Button
                        Button(
                            onClick = onRequestInstantPayout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("instant_cashout_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("INSTANT CASHOUT VIA STRIPE CONNECT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }

            // Breakdown Grid (Base, Tips, Surge)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("100% Tips", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("+$${String.format(java.util.Locale.US, "%.2f", tipsAmount)}", color = Color(0xFF34D399), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Surge Bonuses", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("+$${String.format(java.util.Locale.US, "%.2f", surgeBonusAmount)}", color = Color(0xFFF59E0B), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Automated IRS Mileage Tax Deduction Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF38BDF8))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("IRS Mileage Tax Deduction ($0.67/mi)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Today's Delivery Miles", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("${String.format(java.util.Locale.US, "%.1f", todayMiles)} mi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Column {
                                Text("Total Logged Delivery Miles", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("${String.format(java.util.Locale.US, "%.1f", totalMiles)} mi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Column {
                                Text("Tax Deduction Value", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("$${String.format(java.util.Locale.US, "%.2f", totalTaxDeduction)}", color = Color(0xFF34D399), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // Recent Payout Transactions
            item {
                Text("Recent Payout Transfers", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            items(payoutHistory) { payout ->
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(payout.method, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Stripe ID: ${payout.stripePayoutId.take(12)} • Paid", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                        Text(
                            text = "+$${String.format(java.util.Locale.US, "%.2f", payout.netPayoutAmount)}",
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
