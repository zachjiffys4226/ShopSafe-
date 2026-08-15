package com.example.shopsafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopsafe.data.models.FoodItem
import com.example.shopsafe.data.models.Storefront
import com.example.shopsafe.data.models.isFastFood
import com.example.shopsafe.ui.ShopSafeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailModal(
    store: Storefront,
    viewModel: ShopSafeViewModel,
    onDismiss: () -> Unit
) {
    val foodItems by viewModel.storeFoodItems.collectAsState()
    val context = LocalContext.current
    var isSubscribed by remember { mutableStateOf(false) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            if (!store.isOpen) Color.DarkGray
                            else if (store.isShopSafeEmployeeHub) Color(0xFF0F172A)
                            else Color(0xFFDC2626)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (store.isOpen) store.name else "${store.name} (Closed)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "📍 ${store.address} • 📞 ${store.phoneNumber}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.95f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "★ ${store.rating} • ${store.estimatedMins} mins • $${String.format(java.util.Locale.US, "%.2f", store.deliveryFee)} delivery • Est. Tax ${(store.taxRate * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                if (!store.isOpen) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "This store is currently closed.",
                                color = Color(0xFF991B1B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    isSubscribed = true
                                    viewModel.subscribeToStoreOpenNotification(context, store.id, store.name)
                                },
                                enabled = !isSubscribed,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSubscribed) Color(0xFF16A34A) else Color(0xFFDC2626)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = if (isSubscribed) Icons.Default.Check else Icons.Default.Notifications,
                                    contentDescription = if (isSubscribed) "Subscription Active Checkmark" else "Notification Bell Icon",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isSubscribed) "Subscribed for Updates" else "Notify Me When Open")
                            }
                        }
                    }
                } else if (store.isShopSafeEmployeeHub) {
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = "Employee Badge Icon",
                                tint = Color(0xFF0284C7)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ShopSafe In-Person Personal Shopper Hub. Employees shop for your items in real-time!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0369A1)
                            )
                        }
                    }
                }

                // AI Photo Shelf / Menu Scanner Action Bar
                Surface(
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (store.isFastFood) "🍔 Fast Food Live Menu Board" else "🛒 Live Store Inventory",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = {
                                viewModel.scanningStoreId.value = store.id
                                viewModel.scanningStoreName.value = store.name
                                viewModel.isFastFoodScan.value = store.isFastFood
                                viewModel.showDriverPhotoScannerModal.value = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Camera Icon for Menu or Shelf Scanning", tint = Color(0xFF0F172A), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (store.isFastFood) "Snap Menu Photo" else "Snap Shelf Photo",
                                color = Color(0xFF0F172A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Menu Items List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(foodItems) { food ->
                        FoodItemRow(
                            food = food,
                            storeOpen = store.isOpen,
                            onAddToCart = { viewModel.addToCart(food, store) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FoodItemRow(
    food: FoodItem,
    storeOpen: Boolean = true,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = food.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = food.aisle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$${String.format(java.util.Locale.US, "%.2f", food.price)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF16A34A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (food.inStock) "• In Stock" else "• Out of Stock",
                        fontSize = 10.sp,
                        color = if (food.inStock) Color(0xFF16A34A) else Color(0xFFDC2626),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onAddToCart,
                enabled = storeOpen && food.inStock,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }
    }
}
