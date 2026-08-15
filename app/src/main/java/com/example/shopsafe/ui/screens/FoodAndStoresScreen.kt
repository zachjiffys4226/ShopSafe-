package com.example.shopsafe.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.shopsafe.data.models.ItemCategory
import com.example.shopsafe.data.models.Storefront
import com.example.shopsafe.ui.ShopSafeViewModel

@Composable
fun FoodAndStoresScreen(
    viewModel: ShopSafeViewModel
) {
    val storefronts by viewModel.storefronts.collectAsState()
    val selectedStore by viewModel.selectedStorefront.collectAsState()
    val showAddStoreModal by viewModel.showAddStoreModal.collectAsState()
    var selectedCat by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredStores = storefronts.filter { store ->
        val matchesSearch = searchQuery.isBlank() || store.name.contains(searchQuery, ignoreCase = true) || store.category.contains(searchQuery, ignoreCase = true) || store.address.contains(searchQuery, ignoreCase = true) || store.cityState.contains(searchQuery, ignoreCase = true)
        val matchesCat = when (selectedCat) {
            "ALL" -> true
            "PHARMACY" -> store.category == "PHARMACY" || store.name.contains("CVS", ignoreCase = true)
            "FAST_FOOD" -> store.category == ItemCategory.FAST_FOOD.name
            "GROCERY" -> store.category == ItemCategory.GROCERY.name
            "ShopSafe" -> store.isShopSafeEmployeeHub
            else -> true
        }
        matchesSearch && matchesCat
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ShopSafe Header Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = "ShopSafe Express Drop-off",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Deliver Now",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "123 Main St, Apt 4B, San Francisco, CA",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Surface(
                        color = Color(0xFFDC2626),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "ShopSafe Engine",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search fast food, groceries, storefront hubs...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Icons
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val cats = listOf(
                        "ALL" to "All",
                        "PHARMACY" to "CVS Pharmacy",
                        "FAST_FOOD" to "Fast Food",
                        "GROCERY" to "Groceries",
                        "ShopSafe" to "Storefront Hubs"
                    )
                    items(cats) { (key, label) ->
                        val isSelected = selectedCat == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCat = key },
                            label = { Text(label, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (key) {
                                        "PHARMACY" -> Icons.Default.LocalPharmacy
                                        "FAST_FOOD" -> Icons.Default.Fastfood
                                        "GROCERY" -> Icons.Default.LocalGroceryStore
                                        "ShopSafe" -> Icons.Default.Store
                                        else -> Icons.Default.Restaurant
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        // Stores List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Hero Promotional Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = Color(0xFF0284C7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "FAST IN-PERSON DROP-OFF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ShopSafe Employee Personal Shoppers",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Shop for customer items at local storefronts with real-time drop-off.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Featured Restaurants & Storefront Hubs",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { viewModel.showAddStoreModal.value = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Store", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(filteredStores) { store ->
                StorefrontCard(
                    store = store,
                    onClick = { viewModel.selectedStorefront.value = store }
                )
            }
        }
    }

    selectedStore?.let { currentStore ->
        StoreDetailModal(
            store = currentStore,
            viewModel = viewModel,
            onDismiss = { viewModel.selectedStorefront.value = null }
        )
    }

    if (showAddStoreModal) {
        AddStoreLocationModal(
            viewModel = viewModel,
            onDismiss = { viewModel.showAddStoreModal.value = false }
        )
    }
}

@Composable
fun StorefrontCard(
    store: Storefront,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        if (store.isShopSafeEmployeeHub) Color(0xFF0F172A) else Color(0xFFB91C1C)
                    )
            ) {
                val storeImageUrl = remember(store) {
                    when {
                        store.category.uppercase() == "PHARMACY" -> "https://images.unsplash.com/photo-1631549916768-4119b2e55c26?w=800&auto=format&fit=crop&q=90"
                        store.category.uppercase() == "FAST_FOOD" -> "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=90"
                        store.category.uppercase() == "GROCERY" -> "https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&auto=format&fit=crop&q=90"
                        else -> "https://images.unsplash.com/photo-1521503862198-2ae9a997bbc9?w=800&auto=format&fit=crop&q=90"
                    }
                }
                Image(
                    painter = coil.compose.rememberAsyncImagePainter(storeImageUrl),
                    contentDescription = store.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (store.isShopSafeEmployeeHub) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp),
                        color = Color(0xFF0284C7),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ShopSafe Employee Personal Shopper Storefront",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = store.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFEAB308),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${store.rating}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${store.estimatedMins} mins • ${store.distanceMiles} mi",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (store.deliveryFee == 0.0) "FREE Delivery" else "$${String.format(java.util.Locale.US, "%.2f", store.deliveryFee)} Delivery",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (store.deliveryFee == 0.0) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
