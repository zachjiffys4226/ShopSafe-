package com.example.shopsafe.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopsafe.data.models.Order
import com.example.shopsafe.data.models.OrderStatus
import com.example.shopsafe.ui.CustomerTab
import com.example.shopsafe.ui.ShopSafeViewModel
import com.example.shopsafe.ui.components.GoogleMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    viewModel: ShopSafeViewModel
) {
    val orders by viewModel.customerPastOrders.collectAsState()
    val activeTrackedOrder by viewModel.activeTrackedOrder.collectAsState()

    var reviewTargetOrder by remember { mutableStateOf<Order?>(null) }

    if (reviewTargetOrder != null) {
        ReviewOrderDialog(
            order = reviewTargetOrder!!,
            viewModel = viewModel,
            onDismiss = { reviewTargetOrder = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ShopSafe Live Orders & Tracking", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (activeTrackedOrder != null) {
                // Active Live Tracker View
                val order = activeTrackedOrder!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Order #${order.id.take(8)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = order.storeOrSellerName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { viewModel.activeTrackedOrderId.value = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Back to List")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Order Progress Stepper Timeline
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Status: ${order.status.replace("_", " ")}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7)
                                )
                                Text(
                                    text = if (order.status == OrderStatus.DELIVERED.name) "Delivered ✓" else "ETA ~${order.estimatedDeliveryMins} mins",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Timeline Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OrderStatusStepIcon("Placed", order.status != OrderStatus.PLACED.name)
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                                OrderStatusStepIcon("Preparing", order.status == OrderStatus.SHOPPING_OR_PREPARING.name || order.status == OrderStatus.DRIVER_ASSIGNED.name || order.status == OrderStatus.ON_THE_WAY.name || order.status == OrderStatus.APPROACHING.name || order.status == OrderStatus.DELIVERED.name)
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                                OrderStatusStepIcon("On Way", order.status == OrderStatus.ON_THE_WAY.name || order.status == OrderStatus.APPROACHING.name || order.status == OrderStatus.DELIVERED.name)
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                                OrderStatusStepIcon("Approaching", order.status == OrderStatus.APPROACHING.name || order.status == OrderStatus.DELIVERED.name)
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                                OrderStatusStepIcon("Delivered", order.status == OrderStatus.DELIVERED.name)
                            }
                        }
                    }

                    if (order.status == OrderStatus.APPROACHING.name) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "DRIVER IS APPROACHING YOUR LOCATION!",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        "${order.driverName} is ~2 minutes away at ${order.dropoffAddress}. Real-time push alert dispatched!",
                                        fontSize = 12.sp,
                                        color = Color(0xFF78350F)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { viewModel.triggerDriverApproachingNotification(order.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulate Driver Approaching Push Notification", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Google Map Live Navigation Canvas
                    GoogleMapView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        storeName = order.storeOrSellerName,
                        pickupTitle = order.pickupAddress,
                        dropoffTitle = order.dropoffAddress,
                        showDriverMarker = order.status != OrderStatus.DELIVERED.name,
                        driverName = order.driverName
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Driver Profile & Review Bar
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = CircleShape,
                                        color = Color(0xFF0284C7)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = order.driverName.take(1),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = order.driverName,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Text(
                                            text = "★ ${order.driverRating} • ShopSafe Courier",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = { viewModel.startCall(order.driverName, order.driverPhone) },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFF16A34A), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Call Driver", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }

                                    IconButton(
                                        onClick = { viewModel.customerTab.value = CustomerTab.MESSAGES },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFF0284C7), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = "Message Driver", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }

                            if (order.status == OrderStatus.DELIVERED.name) {
                                Spacer(modifier = Modifier.height(12.dp))
                                if (order.hasBeenReviewed) {
                                    Surface(
                                        color = Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "★".repeat(order.customerRating) + "☆".repeat(5 - order.customerRating),
                                                    color = Color(0xFFD97706),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Your Review Submitted",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF92400E)
                                                )
                                            }
                                            if (order.customerFeedback.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "\"${order.customerFeedback}\"",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF78350F)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { reviewTargetOrder = order },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Rate Driver & Leave Review", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // List of Orders
                if (orders.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active or previous ShopSafe orders yet.")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orders) { order ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                onClick = { viewModel.activeTrackedOrderId.value = order.id }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = order.storeOrSellerName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            color = if (order.status == OrderStatus.DELIVERED.name) Color(0xFFDCFCE7) else Color(0xFFE0F2FE),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = order.status.replace("_", " "),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (order.status == OrderStatus.DELIVERED.name) Color(0xFF15803D) else Color(0xFF0369A1),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = order.itemsSummary,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Total: $${String.format("%.2f", order.total)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF16A34A)
                                        )
                                        TextButton(onClick = { viewModel.activeTrackedOrderId.value = order.id }) {
                                            Text("Track Live Route")
                                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                                        }
                                    }

                                    if (order.status == OrderStatus.DELIVERED.name) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        if (order.hasBeenReviewed) {
                                            Surface(
                                                color = Color(0xFFFEF3C7),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "★".repeat(order.customerRating) + "☆".repeat(5 - order.customerRating),
                                                        color = Color(0xFFD97706),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = if (order.customerFeedback.isNotBlank()) "\"${order.customerFeedback}\"" else "Reviewed",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF92400E)
                                                    )
                                                }
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = { reviewTargetOrder = order },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Rate Driver & Leave Review", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
    }
}

@Composable
fun ReviewOrderDialog(
    order: Order,
    viewModel: ShopSafeViewModel,
    onDismiss: () -> Unit
) {
    var selectedRating by remember { mutableStateOf(5) }
    var feedbackText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Rate Delivery & Driver", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "How was your delivery with ${order.driverName}?",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Star Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = { selectedRating = i }) {
                            Icon(
                                imageVector = if (i <= selectedRating) Icons.Default.Star else Icons.Outlined.Star,
                                contentDescription = "$i stars",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Feedback / Compliment (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.submitOrderReview(order.id, selectedRating, feedbackText)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Submit Review", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun OrderStatusStepIcon(label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = if (isActive) Color(0xFF16A34A) else Color.LightGray,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isActive) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = if (isActive) Color.Black else Color.Gray)
    }
}
