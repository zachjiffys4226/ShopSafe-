package com.example.shopsafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.shopsafe.ui.CustomerTab
import com.example.shopsafe.ui.ShopSafeViewModel
import com.example.shopsafe.ui.components.GoogleMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartAndCheckoutScreen(
    viewModel: ShopSafeViewModel
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState(0.0)
    val deliveryFee by viewModel.cartDeliveryFee.collectAsState(0.0)
    val serviceFee by viewModel.cartServiceFee.collectAsState(0.0)
    val selectedTip by viewModel.selectedTipAmount.collectAsState()
    val total by viewModel.cartTotal.collectAsState(0.0)
    val dropoffAddress by viewModel.dropoffAddress.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val savedAddresses by viewModel.savedAddresses.collectAsState()
    val recentAddresses by viewModel.recentAddresses.collectAsState()

    val availableCredits by viewModel.availableDeliveryCredits.collectAsState()
    val appliedCredit by viewModel.appliedDeliveryCredit.collectAsState()
    val useDeliveryCredits by viewModel.useDeliveryCredits.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ShopSafe Express Checkout", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCartCheckout,
                        contentDescription = "Empty Shopping Cart Illustration",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your ShopSafe cart is empty",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.customerTab.value = CustomerTab.FOOD_STORES },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Explore Restaurants & Storefronts")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Drop-off Location with Google Map Preview
                    Text(
                        text = "Drop-off Location",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dropoffAddress,
                        onValueChange = { viewModel.dropoffAddress.value = it },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = "Address Location Icon", tint = Color(0xFFDC2626)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "⚡ Quick Select Saved / Recent Addresses",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0284C7)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val combinedAddresses = (savedAddresses + recentAddresses).distinct()
                    if (combinedAddresses.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            combinedAddresses.forEach { addr ->
                                val isCurrent = dropoffAddress.equals(addr, ignoreCase = true)
                                FilterChip(
                                    selected = isCurrent,
                                    onClick = {
                                        viewModel.dropoffAddress.value = addr
                                        viewModel.cacheRecentLocation(addr)
                                    },
                                    label = { Text(addr, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (savedAddresses.contains(addr)) Icons.Default.Bookmark else Icons.Default.History,
                                            contentDescription = if (savedAddresses.contains(addr)) "Saved Address Bookmark" else "Recent Address History Icon",
                                            modifier = Modifier.size(14.dp),
                                            tint = if (isCurrent) Color(0xFF16A34A) else Color.Gray
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GoogleMapView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        pickupTitle = cartItems.firstOrNull()?.storeName ?: "Store/Pickup Location",
                        dropoffTitle = dropoffAddress,
                        showDriverMarker = false
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Order Items Summary
                    Text(
                        text = "Order Summary (${cartItems.size} items)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    cartItems.forEach { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = item.storeName,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "$${String.format("%.2f", item.price * item.quantity)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.removeFromCart(item.id) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove item from cart", tint = Color.Gray, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Delivery Instructions
                    val deliveryInstructions by viewModel.checkoutDeliveryInstructions.collectAsState()
                    OutlinedTextField(
                        value = deliveryInstructions,
                        onValueChange = { viewModel.checkoutDeliveryInstructions.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Delivery Instructions / Special Requests") },
                        placeholder = { Text("e.g. Leave at door, extra napkins") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ShopSafe Driver Tip Selector
                    Text(
                        text = "Courier Driver Tip (100% goes to driver)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(3.00, 4.00, 5.00, 7.00).forEach { tipVal ->
                            val isSelected = selectedTip == tipVal
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectedTipAmount.value = tipVal },
                                label = { Text("$${String.format("%.2f", tipVal)}") },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = "Selected Tip Indicator", modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Payment Method Selector
                    Text(
                        text = "Payment Method",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val options = listOf(
                                "ShopSafe Pay Balance ($120.00)",
                                "Credit Card (•••• 4242)",
                                "Google Pay",
                                "Cash on Delivery"
                            )
                            options.forEach { option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = paymentMethod == option,
                                        onClick = { viewModel.paymentMethod.value = option }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = option,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Referral Delivery Fee Credits Selector
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
                                    Icon(
                                        imageVector = Icons.Default.CardGiftcard,
                                        contentDescription = "Referral Gift Card Icon",
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Referral Delivery Credits",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "$${String.format("%.2f", availableCredits)} available in account",
                                            fontSize = 12.sp,
                                            color = Color(0xFF166534)
                                        )
                                    }
                                }

                                Switch(
                                    checked = useDeliveryCredits && availableCredits > 0.0,
                                    onCheckedChange = { viewModel.toggleUseDeliveryCredits() },
                                    enabled = availableCredits > 0.0,
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF16A34A))
                                )
                            }

                            if (appliedCredit > 0.0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🎉 -$${String.format("%.2f", appliedCredit)} referral credit discount applied to delivery fee!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF15803D),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ShopSafe Itemized Price Breakdown
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Itemized Receipt",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal", fontSize = 13.sp, color = Color.Gray)
                                Text("$${String.format("%.2f", subtotal)}", fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery Fee", fontSize = 13.sp, color = Color.Gray)
                                Text("$${String.format("%.2f", deliveryFee)}", fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            if (appliedCredit > 0.0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Redeem, contentDescription = "Referral Credit Gift Icon", tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Referral Delivery Credit", fontSize = 13.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Medium)
                                    }
                                    Text("-$${String.format("%.2f", appliedCredit)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Service Fee (5%)", fontSize = 13.sp, color = Color.Gray)
                                Text("$${String.format("%.2f", serviceFee)}", fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Driver Tip", fontSize = 13.sp, color = Color.Gray)
                                Text("$${String.format("%.2f", selectedTip)}", fontSize = 13.sp)
                            }

                            Divider(modifier = Modifier.padding(vertical = 10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Charged", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("$${String.format("%.2f", total)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF16A34A))
                            }
                        }
                    }
                }

                // Place Order Button
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.placeOrder() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Order Confirmation Icon")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Place ShopSafe Order • $${String.format("%.2f", total)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
