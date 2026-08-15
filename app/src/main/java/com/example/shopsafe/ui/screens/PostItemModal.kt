package com.example.shopsafe.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
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
import com.example.shopsafe.data.models.ItemCategory
import com.example.shopsafe.data.models.ItemCondition
import com.example.shopsafe.ui.ShopSafeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostItemModal(
    viewModel: ShopSafeViewModel,
    onDismiss: () -> Unit
) {
    val postTitle by viewModel.postTitle.collectAsState()
    val postCategory by viewModel.postCategory.collectAsState()
    val postCondition by viewModel.postCondition.collectAsState()
    val postPrice by viewModel.postPrice.collectAsState()
    val postPickupLocation by viewModel.postPickupLocation.collectAsState()
    val postDescription by viewModel.postDescription.collectAsState()
    val postIsFacebookCrosspost by viewModel.postIsFacebookCrosspost.collectAsState()
    val isEstimatingPrice by viewModel.isEstimatingPrice.collectAsState()
    val postDimensions by viewModel.postDimensions.collectAsState()
    val postWeightLbs by viewModel.postWeightLbs.collectAsState()
    val postRequiredVehicle by viewModel.postRequiredVehicle.collectAsState()

    val weightNum = postWeightLbs.toDoubleOrNull() ?: 0.0
    val isVehicleCategory = postCategory == ItemCategory.VEHICLES.name || postCategory.equals("VEHICLES", ignoreCase = true)
    val isHeavyItem = !isVehicleCategory && (postRequiredVehicle in listOf("Pickup Truck", "Flatbed Trailer", "Cargo Van") || weightNum >= 50.0)

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Post Item for Sale",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ShopSafe Marketplace & FB Crosspost",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                OutlinedTextField(
                    value = postTitle,
                    onValueChange = { viewModel.postTitle.value = it },
                    label = { Text("Item Title") },
                    placeholder = { Text("e.g. Sony Wireless Headphones") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Sell, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Product Media (Photos & Videos) Section
                Text("Product Media (Real-Life Photos & Videos)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Upload real-life product captures. Unlimited photos supported.", fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(8.dp))

                // Photos & Videos List Grid
                val uploadedImages by viewModel.postUploadedImages.collectAsState()
                val videoUrl by viewModel.postVideoUrl.collectAsState()
                var showCameraModal by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Upload button
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .size(72.dp)
                            .clickable { showCameraModal = true }
                            .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Add Media", fontSize = 9.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Display list of photos / videos
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (videoUrl.isNotBlank()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(2.dp, Color(0xFFEAB308), RoundedCornerShape(12.dp))
                                ) {
                                    // Video thumbnail or indicator
                                    Image(
                                        painter = coil.compose.rememberAsyncImagePainter("https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600&auto=format&fit=crop&q=90"),
                                        contentDescription = null,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    // Remove button
                                    IconButton(
                                        onClick = { viewModel.postVideoUrl.value = "" },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(18.dp)
                                            .background(Color.Red, CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }

                        items(uploadedImages.size) { index ->
                            val url = uploadedImages[index]
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Image(
                                    painter = coil.compose.rememberAsyncImagePainter(url),
                                    contentDescription = null,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Remove button
                                IconButton(
                                    onClick = {
                                        viewModel.postUploadedImages.value = uploadedImages.filterIndexed { i, _ -> i != index }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(18.dp)
                                        .background(Color.Red, CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }

                if (showCameraModal) {
                    com.example.shopsafe.ui.components.CameraDialog(
                        title = "Add Product Photo/Video",
                        isSelfie = false,
                        allowVideo = true,
                        onMediaCaptured = { path, isVideo ->
                            if (isVideo) {
                                viewModel.postVideoUrl.value = path
                            } else {
                                viewModel.postUploadedImages.value = uploadedImages + path
                            }
                            showCameraModal = false
                        },
                        onDismiss = { showCameraModal = false }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // AI Price & Description Assistant Button
                OutlinedButton(
                    onClick = { viewModel.triggerAiPriceEstimate() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF0284C7)
                    )
                ) {
                    if (isEstimatingPrice) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini Estimating Price...")
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Smart Price & Description Assistant")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category & Condition Selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var categoryExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = postCategory.replace("_", " "),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            listOf(
                                ItemCategory.ELECTRONICS.name,
                                ItemCategory.VEHICLES.name,
                                ItemCategory.APPAREL.name,
                                ItemCategory.HOME_GARDEN.name,
                                ItemCategory.SPORTING_GOODS.name,
                                ItemCategory.NEW_ITEMS.name
                            ).forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.replace("_", " ")) },
                                    onClick = {
                                        viewModel.postCategory.value = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    var conditionExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = conditionExpanded,
                        onExpandedChange = { conditionExpanded = !conditionExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = postCondition.replace("_", " "),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Condition") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = conditionExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = conditionExpanded,
                            onDismissRequest = { conditionExpanded = false }
                        ) {
                            listOf(
                                ItemCondition.NEW.name,
                                ItemCondition.USED_LIKE_NEW.name,
                                ItemCondition.USED_GOOD.name,
                                ItemCondition.USED_FAIR.name
                            ).forEach { cond ->
                                DropdownMenuItem(
                                    text = { Text(cond.replace("_", " ")) },
                                    onClick = {
                                        viewModel.postCondition.value = cond
                                        conditionExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price
                OutlinedTextField(
                    value = postPrice,
                    onValueChange = { viewModel.postPrice.value = it },
                    label = { Text("Price ($ USD)") },
                    placeholder = { Text("45.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Item Size & Weight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = postDimensions,
                        onValueChange = { viewModel.postDimensions.value = it },
                        label = { Text("Size / Dimensions") },
                        placeholder = { Text("36x24x18 in") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) }
                    )

                    OutlinedTextField(
                        value = postWeightLbs,
                        onValueChange = { viewModel.postWeightLbs.value = it },
                        label = { Text("Weight (lbs)") },
                        placeholder = { Text("15.0") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Vehicle Type & Requirements to Haul Away Dropdown
                var vehicleExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = vehicleExpanded,
                    onExpandedChange = { vehicleExpanded = !vehicleExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = postRequiredVehicle,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vehicle Required to Haul Away") },
                        leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = vehicleExpanded,
                        onDismissRequest = { vehicleExpanded = false }
                    ) {
                        listOf("Sedan", "SUV", "Pickup Truck", "Flatbed Trailer", "Cargo Van").forEach { veh ->
                            DropdownMenuItem(
                                text = { Text(veh) },
                                onClick = {
                                    viewModel.postRequiredVehicle.value = veh
                                    vehicleExpanded = false
                                }
                            )
                        }
                    }
                }

                // HEAVY Badge Banner Preview if applicable
                if (isHeavyItem) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFFDC2626),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "HEAVY ITEM BADGE (AUTOMATIC)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Marked clearly in bright red in ad. Requires Truck or Trailer delivery.",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // REQUIRED Pickup Location
                OutlinedTextField(
                    value = postPickupLocation,
                    onValueChange = { viewModel.postPickupLocation.value = it },
                    label = { Text("Pick Up Location (REQUIRED)") },
                    placeholder = { Text("Address or Safe Exchange Spot") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFFDC2626)) },
                    isError = postPickupLocation.isBlank(),
                    supportingText = {
                        if (postPickupLocation.isBlank()) {
                            Text("ShopSafe requires a verified pick up address for all items.", color = Color(0xFFDC2626))
                        } else {
                            Text("Buyers can inspect in person or order ShopSafe Courier delivery.", color = Color.Gray)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = postDescription,
                    onValueChange = { viewModel.postDescription.value = it },
                    label = { Text("Description") },
                    placeholder = { Text("Describe condition, accessories included, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Facebook Marketplace Crosspost Toggle
                Surface(
                    color = Color(0xFF1877F2).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color(0xFF1877F2)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Facebook Marketplace Crosspost",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1877F2)
                                )
                                Text(
                                    text = "Auto-list on FB Marketplace simultaneously",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = postIsFacebookCrosspost,
                            onCheckedChange = { viewModel.postIsFacebookCrosspost.value = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Post Button
                Button(
                    onClick = { viewModel.postNewMarketplaceItem() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = postTitle.isNotBlank() && postPickupLocation.isNotBlank()
                ) {
                    Icon(imageVector = Icons.Default.Publish, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish to ShopSafe Marketplace", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
