package com.example.shopsafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import com.example.shopsafe.data.ml.DriverExperienceTier
import com.example.shopsafe.data.ml.SmartOrderDispatcherEngine
import com.example.shopsafe.ui.ShopSafeViewModel

@Composable
fun SmartDispatcherModal(
    viewModel: ShopSafeViewModel,
    onClose: () -> Unit
) {
    var trafficIndex by remember { mutableStateOf(1.25) }
    var orderValue by remember { mutableStateOf(185.0) } // Default to high-value order simulation
    var selectedOrderType by remember { mutableStateOf("Express Grocery & Retail (4 items)") }
    
    val rankedDrivers = remember(trafficIndex, orderValue) {
        SmartOrderDispatcherEngine.rankDriversForOrder(
            orderPickupLocation = selectedOrderType,
            baseTrafficCongestionIndex = trafficIndex,
            orderValue = orderValue
        )
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                            Surface(
                                color = Color(0xFF0284C7).copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = "ML Engine Icon",
                                        tint = Color(0xFF0284C7)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "ML Smart Order Dispatcher",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Predictive Matching & Experience Weight Matrix",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close Dispatcher Modal", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Controls card for Traffic & Order Value Simulation
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Traffic Index Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Traffic Index: ${String.format("%.2f", trafficIndex)}x",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { trafficIndex = 1.0 },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                                    ) {
                                        Text("Light", fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { trafficIndex = 1.5 },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                                    ) {
                                        Text("Heavy", fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Order Value Simulation Row (High-Value vs Standard)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Order Value: ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "$${orderValue.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = if (orderValue >= 80.0) Color(0xFF0284C7) else Color(0xFF475569)
                                    )
                                    if (orderValue >= 80.0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            color = Color(0xFFE0F2FE),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                "HIGH VALUE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0369A1),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = orderValue < 80.0,
                                        onClick = { orderValue = 35.0 },
                                        label = { Text("$35 Std", fontSize = 10.sp) },
                                        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                                    )
                                    FilterChip(
                                        selected = orderValue >= 80.0,
                                        onClick = { orderValue = 185.0 },
                                        label = { Text("$185 High-Val", fontSize = 10.sp) },
                                        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ML Model applies weighted experience tier curves (up to +20 pts) & high-value order priority bonuses (+12 pts) for top-rated veteran drivers.",
                                fontSize = 10.5.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "AI Ranked Driver Candidates (${rankedDrivers.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // List of ranked drivers
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(rankedDrivers) { index, driver ->
                            val isOptimal = index == 0
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isOptimal) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isOptimal) 4.dp else 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = if (isOptimal) Color(0xFF16A34A) else Color(0xFF0284C7),
                                                shape = CircleShape,
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "#${index + 1}",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = driver.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "${driver.vehicleType} • ",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                    Text(
                                                        text = driver.experienceTier.label,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when(driver.experienceTier) {
                                                            DriverExperienceTier.MASTER -> Color(0xFF7C3AED)
                                                            DriverExperienceTier.VETERAN -> Color(0xFF0284C7)
                                                            DriverExperienceTier.PRO -> Color(0xFF16A34A)
                                                            DriverExperienceTier.ROOKIE -> Color(0xFF64748B)
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // ML Match Score Badge
                                        Surface(
                                            color = if (isOptimal) Color(0xFFDCFCE7) else Color(0xFFE0F2FE),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "${String.format("%.1f", driver.mlMatchScore)}% Match",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp,
                                                color = if (isOptimal) Color(0xFF16A34A) else Color(0xFF0284C7),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Metrics breakdown including Experience Weight
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Proximity", fontSize = 10.sp, color = Color.Gray)
                                            Text("${driver.distanceMiles} mi", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("Rating", fontSize = 10.sp, color = Color.Gray)
                                            Text("★ ${driver.historicalRating}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (driver.isTopRated) Color(0xFFD97706) else Color.Unspecified)
                                        }
                                        Column {
                                            Text("Deliveries (Exp)", fontSize = 10.sp, color = Color.Gray)
                                            Text("${driver.completedDeliveries} (${String.format("%.1f", driver.experienceScore)}p)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                        }
                                        Column {
                                            Text("Punctuality", fontSize = 10.sp, color = Color.Gray)
                                            Text("${(driver.punctualityScore * 100).toInt()}%", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("High-Val Bonus", fontSize = 10.sp, color = Color.Gray)
                                            val bonus = driver.highValuePriorityBonus
                                            Text(
                                                text = if (bonus > 0) "+${String.format("%.1f", bonus)}p" else if (bonus < 0) "${String.format("%.1f", bonus)}p" else "0p",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (bonus > 0) Color(0xFF16A34A) else if (bonus < 0) Color(0xFFDC2626) else Color.Gray
                                            )
                                        }
                                    }

                                    if (driver.highValuePriorityBonus > 0 && driver.isHighValueOrder) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = Color(0xFFF0FDF4),
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Verified,
                                                    contentDescription = "Verified top-rated driver bonus",
                                                    tint = Color(0xFF16A34A),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Top-Rated Veteran Driver: High-Value Order Priority Weight Applied",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D)
                                                )
                                            }
                                        }
                                    }

                                    if (isOptimal) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = Color(0xFFBBF7D0).copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Optimal candidate checkmark", tint = Color(0xFF15803D), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Optimal Driver Selected by ML Engine (Highest composite experience & rating score)",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF14532D)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Text("Close Dispatcher", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
