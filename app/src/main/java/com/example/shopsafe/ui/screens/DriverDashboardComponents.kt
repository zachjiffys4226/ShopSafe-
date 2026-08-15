package com.example.shopsafe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.shopsafe.data.models.CompletedDelivery
import com.example.shopsafe.data.models.DriverProfile
import com.example.shopsafe.ui.ShopSafeViewModel
import com.example.shopsafe.ui.components.GoogleMapView
import java.text.SimpleDateFormat
import java.util.*

/**
 * DriverEarningsTotalHeroCard displays total lifetime earnings, current balance,
 * today & week metrics, and live Cloud Firestore sync status.
 */
@Composable
fun DriverEarningsTotalHeroCard(
    profile: DriverProfile?,
    totalLifetime: Double,
    completedCount: Int,
    onCashoutClick: () -> Unit,
    onSimulateDeliveryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0B1329),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        tonalElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Firestore Live Status Badge & Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DRIVER EARNINGS DASHBOARD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                }

                // Live Firestore Cloud Storage Tag
                Surface(
                    color = Color(0xFF0369A1).copy(alpha = 0.35f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF38BDF8),
                            shape = CircleShape,
                            modifier = Modifier.size(7.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Cloud Firestore Live",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7DD3FC)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Total Lifetime Earnings Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Total Lifetime Earnings",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", if (totalLifetime > 0) totalLifetime else (profile?.lifetimeEarned ?: 1420.50))}",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.testTag("driver_total_earnings_text")
                    )
                }

                // Completed Trips Counter Badge
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$completedCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Completed Trips",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Grid: Available Balance, Today's Earned, This Week
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Available to Cashout", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        if (profile?.balanceStatus == "PENDING") {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(color = Color(0xFFD97706), shape = RoundedCornerShape(4.dp)) {
                                Text("PENDING", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp))
                            }
                        }
                    }
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", profile?.currentBalance ?: 164.80)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (profile?.balanceStatus == "PENDING") Color(0xFFFBBF24) else Color(0xFF4ADE80)
                    )
                }
                Column {
                    Text("Earned Today", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", profile?.todayEarned ?: 87.55)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column {
                    Text("This Week", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", profile?.weekEarned ?: 438.00)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Request Payout (Stripe Connect)
                Button(
                    onClick = onCashoutClick,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("driver_cashout_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Request Payout (Stripe)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Simulate Test Delivery to Cloud Firestore
                OutlinedButton(
                    onClick = onSimulateDeliveryClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("driver_simulate_delivery_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Log Delivery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * DriverEarningsSummaryWidget displays a visual breakdown of daily, weekly, and
 * instant payout earnings using an interactive Compose bar chart.
 */
@Composable
fun DriverEarningsSummaryWidget(
    profile: DriverProfile?,
    onCashoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedViewMode by remember { mutableStateOf("WEEKLY") } // "WEEKLY" or "CATEGORIES"
    var selectedBarIndex by remember { mutableIntStateOf(4) } // Default to Fri/Today

    val todayEarned = profile?.todayEarned ?: 87.55
    val weekEarned = profile?.weekEarned ?: 438.00
    val currentBalance = profile?.currentBalance ?: 164.80

    // Sample weekly daily breakdown data
    val dailyData = remember(todayEarned) {
        listOf(
            BarDataPoint("Mon", 52.40, "3 trips • $12.00 tips", false),
            BarDataPoint("Tue", 68.20, "4 trips • $15.50 tips", false),
            BarDataPoint("Wed", 94.80, "6 trips • $22.10 tips", false),
            BarDataPoint("Thu", 47.05, "3 trips • $9.00 tips", false),
            BarDataPoint("Fri", todayEarned, "Today • Active shift", true),
            BarDataPoint("Sat", 88.00, "Estimated • Peak weekend", false),
            BarDataPoint("Sun", 0.00, "Scheduled off", false)
        )
    }

    // Category comparison data (Daily vs Weekly vs Instant Payouts)
    val categoryData = remember(todayEarned, weekEarned, currentBalance) {
        listOf(
            BarDataPoint("Daily (Today)", todayEarned, "Today's Net Earnings", true, Color(0xFF38BDF8)),
            BarDataPoint("This Week", weekEarned, "Weekly Aggregate Pay", false, Color(0xFF10B981)),
            BarDataPoint("Instant Payout", currentBalance, "Available to Transfer", false, Color(0xFFF59E0B))
        )
    }

    val activeList = if (selectedViewMode == "WEEKLY") dailyData else categoryData
    val maxVal = activeList.maxOfOrNull { it.amount }?.coerceAtLeast(100.0) ?: 100.0

    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("driver_earnings_summary_widget")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Widget Title Bar & View Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "EARNINGS SUMMARY",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Daily • Weekly • Instant Payouts",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Toggle Pills (Weekly vs Overview)
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        Surface(
                            color = if (selectedViewMode == "WEEKLY") Color(0xFF0284C7) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { selectedViewMode = "WEEKLY"; selectedBarIndex = 4 }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Daily/Week", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Surface(
                            color = if (selectedViewMode == "CATEGORIES") Color(0xFF0284C7) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { selectedViewMode = "CATEGORIES"; selectedBarIndex = 0 }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Overview", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3 KPI Breakdown Cards: Daily, Weekly, Instant Payout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Daily KPI
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color(0xFF0284C7), shape = CircleShape, modifier = Modifier.size(6.dp)) {}
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Daily", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", todayEarned)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                        Text("Today", fontSize = 9.sp, color = Color(0xFF64748B))
                    }
                }

                // Weekly KPI
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color(0xFF10B981), shape = CircleShape, modifier = Modifier.size(6.dp)) {}
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Weekly", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", weekEarned)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                        Text("This Week", fontSize = 9.sp, color = Color(0xFF64748B))
                    }
                }

                // Instant Payout KPI
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .clickable { onCashoutClick() }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = "Instant Payout available indicator", tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Payout", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", currentBalance)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                        Text("Instant Available", fontSize = 9.sp, color = Color(0xFF64748B))
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // BAR CHART CONTAINER
            Surface(
                color = Color(0xFF030712),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Bar Chart Canvas / Flex Rows
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        activeList.forEachIndexed { index, point ->
                            val heightFraction = (point.amount / maxVal).toFloat().coerceIn(0.08f, 1f)
                            val isSelected = index == selectedBarIndex
                            val barColor = point.customColor ?: if (point.isHighlight) Color(0xFF38BDF8) else Color(0xFF334155)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedBarIndex = index }
                            ) {
                                // Amount floating above bar
                                Text(
                                    text = if (point.amount > 0) "$${point.amount.toInt()}" else "-",
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF64748B)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // The Bar graphic
                                Box(
                                    modifier = Modifier
                                        .width(if (selectedViewMode == "CATEGORIES") 36.dp else 22.dp)
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(
                                            if (isSelected) {
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        barColor,
                                                        barColor.copy(alpha = 0.65f)
                                                    )
                                                )
                                            } else {
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        barColor.copy(alpha = 0.85f),
                                                        barColor.copy(alpha = 0.45f)
                                                    )
                                                )
                                            }
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                        )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // X-Axis Label
                                Text(
                                    text = point.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Active Selected Bar Detail Banner
                    val activePoint = activeList.getOrNull(selectedBarIndex) ?: activeList.first()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = activePoint.customColor ?: if (activePoint.isHighlight) Color(0xFF0284C7) else Color(0xFF64748B),
                                shape = CircleShape,
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${activePoint.label}: $${String.format(Locale.US, "%.2f", activePoint.amount)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = activePoint.subtitle,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

data class BarDataPoint(
    val label: String,
    val amount: Double,
    val subtitle: String,
    val isHighlight: Boolean = false,
    val customColor: Color? = null
)

/**
 * DriverDashboardSegmentedTab navigation for switching between Live Dispatches, Completed Deliveries, Stripe Payouts, and Mileage Tracker.
 */
@Composable
fun DriverDashboardSegmentedTab(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    completedCount: Int,
    availableOffersCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(3.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Live Dispatches Tab
            Surface(
                color = if (selectedTab == "DISPATCHES") Color(0xFF0284C7) else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
                    .clickable { onTabSelected("DISPATCHES") }
                    .testTag("tab_dispatches")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = "Live Dispatches and Offers tab",
                        tint = if (selectedTab == "DISPATCHES") Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Orders (${availableOffersCount})",
                        color = if (selectedTab == "DISPATCHES") Color.White else Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    )
                }
            }

            // Completed Deliveries Tab
            Surface(
                color = if (selectedTab == "DELIVERIES") Color(0xFF0284C7) else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
                    .clickable { onTabSelected("DELIVERIES") }
                    .testTag("tab_completed_deliveries")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Completed Deliveries history tab",
                        tint = if (selectedTab == "DELIVERIES") Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "History (${completedCount})",
                        color = if (selectedTab == "DELIVERIES") Color.White else Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    )
                }
            }

            // Automated Mileage Tracker Tab
            Surface(
                color = if (selectedTab == "MILEAGE") Color(0xFF059669) else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
                    .clickable { onTabSelected("MILEAGE") }
                    .testTag("tab_mileage")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = "Automated Mileage Tracker",
                        tint = if (selectedTab == "MILEAGE") Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Mileage",
                        color = if (selectedTab == "MILEAGE") Color.White else Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    )
                }
            }

            // Payouts & Stripe Connect Tab
            Surface(
                color = if (selectedTab == "PAYOUTS") Color(0xFF6366F1) else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
                    .clickable { onTabSelected("PAYOUTS") }
                    .testTag("tab_payouts")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = "Payouts and Stripe Connect account tab",
                        tint = if (selectedTab == "PAYOUTS") Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Payouts",
                        color = if (selectedTab == "PAYOUTS") Color.White else Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    )
                }
            }
        }
    }
}

/**
 * CompletedDeliveriesDashboardView displays search, period filtering chips,
 * and the list of completed deliveries synced from Cloud Firestore.
 */
@Composable
fun CompletedDeliveriesDashboardView(
    deliveries: List<CompletedDelivery>,
    filterPeriod: String,
    onPeriodSelected: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDeliveryClick: (CompletedDelivery) -> Unit,
    onSimulateDeliveryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search and filter period chips
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search by merchant, customer, or address...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_completed_deliveries_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Period Filtering Chips (All, Today, This Week)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val periods = listOf("ALL" to "All Deliveries", "TODAY" to "Today", "WEEK" to "This Week")
            periods.forEach { (key, label) ->
                val isSelected = filterPeriod == key
                FilterChip(
                    selected = isSelected,
                    onClick = { onPeriodSelected(key) },
                    label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_period_$key")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Deliveries Count Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${deliveries.size} Completed Deliveries",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            val totalInFilter = deliveries.sumOf { it.totalEarnings }
            Text(
                text = "Period Total: $${String.format(Locale.US, "%.2f", totalInFilter)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF16A34A)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (deliveries.isEmpty()) {
            // Empty state
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No completed deliveries found",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Deliveries you finish will be synced and stored in Cloud Firestore.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSimulateDeliveryClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Sample Completed Delivery", fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(deliveries, key = { it.id }) { delivery ->
                    CompletedDeliveryCard(
                        delivery = delivery,
                        onClick = { onDeliveryClick(delivery) }
                    )
                }
            }
        }
    }
}

/**
 * CompletedDeliveryCard displays a single completed delivery with full payout breakdown,
 * route locations, timestamp, and ratings.
 */
@Composable
fun CompletedDeliveryCard(
    delivery: CompletedDelivery,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("EEE, MMM d • h:mm a", Locale.US) }
    val formattedDate = remember(delivery.timestamp) { dateFormatter.format(Date(delivery.timestamp)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("completed_delivery_card_${delivery.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Merchant & Payout Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFE0F2FE),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Storefront,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = delivery.storeOrSellerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Total Payout Badge
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "+$${String.format(Locale.US, "%.2f", delivery.totalEarnings)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Items Summary
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = delivery.itemsSummary,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payout breakdown pills: Base Pay, Tip, Peak Bonus
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Base Pay
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Base: $${String.format(Locale.US, "%.2f", delivery.basePay)}",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Tip Amount
                if (delivery.tipAmount > 0) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Tip: +$${String.format(Locale.US, "%.2f", delivery.tipAmount)}",
                            fontSize = 11.sp,
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Peak Pay Bonus
                if (delivery.peakBonus > 0) {
                    Surface(
                        color = Color(0xFFEDE9FE),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Peak: +$${String.format(Locale.US, "%.2f", delivery.peakBonus)}",
                            fontSize = 11.sp,
                            color = Color(0xFF6D28D9),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Distance & Duration
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${delivery.distanceMiles} mi • ${delivery.durationMinutes}m",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Dropoff Location and Rating Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${delivery.customerName} • ${delivery.dropoffAddress}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${delivery.customerRating}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                }
            }
        }
    }
}

/**
 * CompletedDeliveryDetailSheet displays the full itemized trip receipt,
 * proof photo, route information, and Cloud Firestore storage metadata.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedDeliveryDetailModal(
    delivery: CompletedDelivery?,
    onDismiss: () -> Unit
) {
    if (delivery == null) return

    val dateFormatter = remember { SimpleDateFormat("EEEE, MMMM d, yyyy • h:mm a", Locale.US) }
    val formattedDate = remember(delivery.timestamp) { dateFormatter.format(Date(delivery.timestamp)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Modal Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Delivery Receipt & Breakdown",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${delivery.id.take(12)}...",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "✓ DELIVERED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Earnings Hero Banner in Sheet
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Payout for this Trip", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", delivery.totalEarnings)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF38BDF8)
                            )
                        }
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "100% Tips Kept",
                                fontSize = 11.sp,
                                color = Color(0xFF4ADE80),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Itemized Financial Rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Base Pay (ShopSafe Standard)", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        Text("$${String.format(Locale.US, "%.2f", delivery.basePay)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Customer Tip (100% to Driver)", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        Text("+$${String.format(Locale.US, "%.2f", delivery.tipAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE047))
                    }
                    if (delivery.peakBonus > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Peak Hour Incentive Bonus", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                            Text("+$${String.format(Locale.US, "%.2f", delivery.peakBonus)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC084FC))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Route & Address Details Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Pickup
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(delivery.storeOrSellerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(delivery.pickupAddress, fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Dropoff
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Customer: ${delivery.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(delivery.dropoffAddress, fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Google Maps Delivery Route View
            GoogleMapView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                storeName = delivery.storeOrSellerName,
                pickupTitle = delivery.pickupAddress,
                dropoffTitle = delivery.dropoffAddress,
                showDriverMarker = false,
                showTurnByTurnHud = false,
                showRouteOptions = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Trip Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Date & Time", fontSize = 11.sp, color = Color.Gray)
                    Text(formattedDate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Trip Specs", fontSize = 11.sp, color = Color.Gray)
                    Text("${delivery.distanceMiles} miles • ${delivery.durationMinutes} mins", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Proof photo thumbnail if present
            if (delivery.proofPhotoUrl.isNotBlank()) {
                Text("Dropoff Confirmation Photo", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                AsyncImage(
                    model = delivery.proofPhotoUrl,
                    contentDescription = "Proof Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Cloud Firestore Verification Pill
            Surface(
                color = Color(0xFFF0FDF4),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Data securely synced to Cloud Firestore database collection 'driver_completed_deliveries'.",
                        fontSize = 11.sp,
                        color = Color(0xFF166534)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close Receipt")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * DriverMileageDashboardView displays automated mileage logs, IRS tax deduction totals ($0.67/mile),
 * active trip live odometer ticker, and commute exclusion guarantees.
 */
@Composable
fun DriverMileageDashboardView(
    viewModel: ShopSafeViewModel,
    modifier: Modifier = Modifier
) {
    val mileageLogs by viewModel.driverMileageLogs.collectAsState()
    val totalMiles by viewModel.totalDeliveryMiles.collectAsState()
    val totalTaxDeduction by viewModel.totalMileageTaxDeduction.collectAsState()
    val isTrackingActive by viewModel.isMileageTrackingActive.collectAsState()
    val currentTripMiles by viewModel.currentTripMiles.collectAsState()
    val activeOffer by viewModel.activeDriverOffer.collectAsState()
    val activeTripId by viewModel.activeTripOrderId.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showExportConfirm by remember { mutableStateOf(false) }

    val filteredLogs = remember(mileageLogs, searchQuery) {
        if (searchQuery.isBlank()) mileageLogs
        else mileageLogs.filter {
            it.orderId.contains(searchQuery, ignoreCase = true) ||
            it.storeOrSellerName.contains(searchQuery, ignoreCase = true) ||
            it.dropoffAddress.contains(searchQuery, ignoreCase = true)
        }
    }

    val todayMiles = remember(mileageLogs) {
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        mileageLogs.filter { it.startTimestamp >= startOfToday }.sumOf { it.milesDriven }
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    if (showExportConfirm) {
        AlertDialog(
            onDismissRequest = { showExportConfirm = false },
            title = { Text("Export IRS Mileage Log", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Mileage report generated! ${mileageLogs.size} trips totaling ${String.format(Locale.US, "%.1f", totalMiles)} delivery miles (${String.format(Locale.US, "%.2f", totalTaxDeduction)} deduction value) formatted for Schedule C tax preparation.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportConfirm = false
                        android.widget.Toast.makeText(context, "IRS Tax Report exported to driver downloads.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Download Summary")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportConfirm = false }) {
                    Text("Close")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card: Total Delivery Miles & Tax Savings
        item {
            Surface(
                color = Color(0xFF064E3B),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AUTOMATED MILEAGE TRACKER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFA7F3D0),
                                letterSpacing = 1.sp
                            )
                        }

                        Surface(
                            color = if (isTrackingActive) Color(0xFF10B981) else Color(0xFF047857),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (isTrackingActive) Color.White else Color(0xFFA7F3D0),
                                    shape = CircleShape,
                                    modifier = Modifier.size(6.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTrackingActive) "RECORDING" else "STANDBY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", totalMiles)} mi",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text("Total Logged Delivery Miles", fontSize = 12.sp, color = Color(0xFFA7F3D0))
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${String.format(Locale.US, "%.2f", totalTaxDeduction)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6EE7B7)
                            )
                            Text("IRS Tax Deduction ($0.67/mi)", fontSize = 11.sp, color = Color(0xFFA7F3D0))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF047857))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Today: ${String.format(Locale.US, "%.1f", todayMiles)} mi", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("${mileageLogs.size} Active Trips Logged", fontSize = 12.sp, color = Color(0xFFA7F3D0))
                    }
                }
            }
        }

        // Commute Filter Policy Notice Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Automated Commute Exclusion Active", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF065F46))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Travel to and from the driver's home is strictly excluded from logs. Tracking automatically activates once an order is picked up from the store and drops off at the customer.",
                            fontSize = 11.5.sp,
                            color = Color(0xFF047857),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Live Active Trip Odometer (if currently tracking)
        if (isTrackingActive) {
            item {
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF10B981),
                                    shape = CircleShape,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("LIVE DELIVERY IN PROGRESS", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text("Order #$activeTripId", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Trip Odometer:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", currentTripMiles)} mi",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Accruing Deduction:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = "+${String.format(Locale.US, "%.2f", currentTripMiles * 0.67)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }

                        if (activeOffer != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("📍 From: ${activeOffer!!.pickupAddress}", fontSize = 11.sp, color = Color(0xFFCBD5E1), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("🏠 To: ${activeOffer!!.dropoffAddress}", fontSize = 11.sp, color = Color(0xFFCBD5E1), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        // Search & Export Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search mileage logs...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Button(
                    onClick = { showExportConfirm = true },
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Delivery Trip Logs (${filteredLogs.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (mileageLogs.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearAllMileageLogs() }) {
                        Text("Clear Logs", fontSize = 11.sp, color = Color(0xFFEF4444))
                    }
                }
            }
        }

        if (filteredLogs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Delivery Mileage Logs Found", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "When you accept and deliver orders, every customer drop-off mile is automatically captured here with instant tax deduction calculations.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredLogs, key = { it.id }) { log ->
                val dateStr = remember(log.startTimestamp) {
                    val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)
                    sdf.format(Date(log.startTimestamp))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = log.storeOrSellerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(text = dateStr, fontSize = 11.sp, color = Color.Gray)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", log.milesDriven)} mi",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = Color(0xFF15803D),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "+$${String.format(Locale.US, "%.2f", log.taxDeductionValue)} tax ded.",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pickup: ${log.pickupAddress}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dropoff: ${log.dropoffAddress}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Order #${log.orderId} • Active Trip Only",
                                    fontSize = 10.sp,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteMileageLog(log.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete log", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * DriverMileageTripCompletedDialog popup dialog shown when a delivery step is finished,
 * celebrating automated miles logged and tax deductions.
 */
@Composable
fun DriverMileageTripCompletedDialog(
    tripMiles: Double,
    taxDeduction: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delivery Miles Automatically Logged!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Your delivery trip from store pickup to customer dropoff has been automatically logged and calculated.",
                    fontSize = 13.sp
                )

                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Miles Recorded:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${String.format(Locale.US, "%.1f", tripMiles)} miles", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF15803D))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tax Deduction ($0.67/mi):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("+$${String.format(Locale.US, "%.2f", taxDeduction)}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF059669))
                        }
                    }
                }

                Text(
                    "🛡️ Smart Commute Exclusion: Mileage to/from your home was not counted.",
                    fontSize = 11.sp,
                    color = Color(0xFF047857),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
            ) {
                Text("Awesome!")
            }
        }
    )
}
