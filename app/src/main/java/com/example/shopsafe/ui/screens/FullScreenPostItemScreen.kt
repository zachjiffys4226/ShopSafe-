package com.example.shopsafe.ui.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.shopsafe.data.models.ItemCategory
import com.example.shopsafe.data.models.ItemCondition
import com.example.shopsafe.data.models.MarketplaceItem
import com.example.shopsafe.ui.ShopSafeViewModel
import kotlinx.coroutines.launch

enum class PostingStep(val stepNumber: Int, val title: String) {
    PHOTOS(1, "Photos & Media"),
    DETAILS(2, "Item Details"),
    CATEGORY_CONDITION(3, "Category & Condition"),
    PRICING_QUANTITY(4, "Price & Inventory"),
    DELIVERY_OPTIONS(5, "Delivery & Pickup"),
    MARKETPLACES(6, "Publish Destinations"),
    REVIEW(7, "Review & Publish")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenPostItemScreen(
    viewModel: ShopSafeViewModel,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableStateOf(PostingStep.PHOTOS) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Form State
    val postTitle by viewModel.postTitle.collectAsState()
    val postCategory by viewModel.postCategory.collectAsState()
    val postCondition by viewModel.postCondition.collectAsState()
    val postPrice by viewModel.postPrice.collectAsState()
    val postPickupLocation by viewModel.postPickupLocation.collectAsState()
    val postDescription by viewModel.postDescription.collectAsState()
    val isEstimatingPrice by viewModel.isEstimatingPrice.collectAsState()
    val postDimensions by viewModel.postDimensions.collectAsState()
    val postWeightLbs by viewModel.postWeightLbs.collectAsState()
    val postRequiredVehicle by viewModel.postRequiredVehicle.collectAsState()
    val postUploadedImages by viewModel.postUploadedImages.collectAsState()
    val postVideoUrl by viewModel.postVideoUrl.collectAsState()

    // Additional Detailed Selling Attributes
    var brand by remember { mutableStateOf("") }
    var modelNumber by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }
    var enableShopSafeDelivery by remember { mutableStateOf(true) }
    var deliveryFee by remember { mutableStateOf("4.99") }
    var coverPhotoIndex by remember { mutableIntStateOf(0) }

    // Cross-posting destination selection
    var publishToShopSafe by remember { mutableStateOf(true) }
    var publishToFacebook by remember { mutableStateOf(true) }
    var publishToEbay by remember { mutableStateOf(false) }

    // Account connection states
    var isFacebookLinked by remember { mutableStateOf(true) }
    var isEbayLinked by remember { mutableStateOf(false) }
    var showConnectFacebookDialog by remember { mutableStateOf(false) }
    var showConnectEbayDialog by remember { mutableStateOf(false) }
    var showImportListingsDialog by remember { mutableStateOf(false) }
    var isAiAssisting by remember { mutableStateOf(false) }
    var aiFeedbackMessage by remember { mutableStateOf<String?>(null) }

    // Sample default photo if none uploaded
    val activePhotos = if (postUploadedImages.isNotEmpty()) postUploadedImages else listOf(
        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600",
        "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=600"
    )

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (currentStep.stepNumber > 1) {
                                        val prev = PostingStep.values().firstOrNull { it.stepNumber == currentStep.stepNumber - 1 }
                                        if (prev != null) currentStep = prev
                                    } else {
                                        onDismiss()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentStep == PostingStep.PHOTOS) Icons.Default.Close else Icons.Default.ArrowBack,
                                    contentDescription = "Back or Close"
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "Create Listing",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Step ${currentStep.stepNumber} of 7: ${currentStep.title}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { showImportListingsDialog = true },
                                modifier = Modifier.testTag("import_marketplace_listings_button")
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Linear Step Indicator Bar
                    LinearProgressIndicator(
                        progress = { currentStep.stepNumber / 7f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep != PostingStep.PHOTOS) {
                        OutlinedButton(
                            onClick = {
                                val prev = PostingStep.values().firstOrNull { it.stepNumber == currentStep.stepNumber - 1 }
                                if (prev != null) currentStep = prev
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Previous", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = {
                            if (currentStep == PostingStep.REVIEW) {
                                // Final Publish
                                viewModel.postNewMarketplaceItem()
                                onDismiss()
                            } else {
                                val next = PostingStep.values().firstOrNull { it.stepNumber == currentStep.stepNumber + 1 }
                                if (next != null) currentStep = next
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentStep == PostingStep.REVIEW) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("next_step_button"),
                        enabled = when (currentStep) {
                            PostingStep.PHOTOS -> true
                            PostingStep.DETAILS -> postTitle.isNotBlank()
                            PostingStep.CATEGORY_CONDITION -> true
                            PostingStep.PRICING_QUANTITY -> postPrice.isNotBlank()
                            PostingStep.DELIVERY_OPTIONS -> postPickupLocation.isNotBlank()
                            PostingStep.MARKETPLACES -> publishToShopSafe || publishToFacebook || publishToEbay
                            PostingStep.REVIEW -> postTitle.isNotBlank() && postPrice.isNotBlank()
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (currentStep == PostingStep.REVIEW) "Publish Listing" else "Continue",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (currentStep == PostingStep.REVIEW) Icons.Default.CheckCircle else Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (currentStep) {
                // STEP 1: MEDIA FIRST
                PostingStep.PHOTOS -> {
                    Text(
                        text = "Add Photos & Video",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "First photo will be your cover thumbnail. Listings with 3+ clear photos sell 2.5x faster.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Smart AI Assistant Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Smart AI Listing Assistant", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF065F46))
                                Text("Auto-generate title, description, category, and keywords from your photos.", fontSize = 12.sp, color = Color(0xFF047857))
                            }
                            Button(
                                onClick = {
                                    isAiAssisting = true
                                    viewModel.postTitle.value = "Sony WH-1000XM4 Wireless Noise-Canceling Headphones"
                                    viewModel.postCategory.value = ItemCategory.ELECTRONICS.name
                                    viewModel.postCondition.value = ItemCondition.USED_LIKE_NEW.name
                                    viewModel.postPrice.value = "179.00"
                                    viewModel.postDescription.value = "Premium over-ear noise canceling headphones in pristine condition. Includes original travel case, USB-C charging cable, and audio jack. Battery holds full 30-hour charge."
                                    brand = "Sony"
                                    modelNumber = "WH-1000XM4"
                                    color = "Midnight Black"
                                    isAiAssisting = false
                                    aiFeedbackMessage = "AI populated title, category, price, and specs!"
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Analyze", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (aiFeedbackMessage != null) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(aiFeedbackMessage ?: "", fontSize = 12.sp, color = Color(0xFF166534), fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    // Main Media Grid / Carousel
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Photos (${activePhotos.size}/12)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Tap photo to set as cover", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(activePhotos) { index, photoUrl ->
                                Box(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(
                                            width = if (coverPhotoIndex == index) 3.dp else 1.dp,
                                            color = if (coverPhotoIndex == index) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { coverPhotoIndex = index }
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(photoUrl),
                                        contentDescription = "Uploaded Photo $index",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    if (coverPhotoIndex == index) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(bottomEnd = 8.dp),
                                            modifier = Modifier.align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                "COVER",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    // Action buttons overlay
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                    ) {
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.6f),
                                            shape = CircleShape,
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clickable {
                                                    aiFeedbackMessage = "Rotated photo 90°"
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // Add Media Button
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .size(130.dp)
                                        .clickable {
                                            viewModel.postUploadedImages.value = activePhotos + listOf(
                                                "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=600"
                                            )
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = "Add Media", tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Add Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text("Camera / Gallery", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    // Optional Video Link
                    OutlinedTextField(
                        value = postVideoUrl,
                        onValueChange = { viewModel.postVideoUrl.value = it },
                        label = { Text("Product Video URL (Optional)") },
                        placeholder = { Text("YouTube / Vimeo / Cloud link") },
                        leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // STEP 2: ITEM DETAILS
                PostingStep.DETAILS -> {
                    Text("Item Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Provide specific details to help buyers find your item accurately.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { viewModel.postTitle.value = it },
                        label = { Text("Listing Title *") },
                        placeholder = { Text("e.g. Apple iPad Air 5th Gen 64GB Space Gray") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("listing_title_input"),
                        leadingIcon = { Icon(Icons.Default.Sell, contentDescription = null) }
                    )

                    OutlinedTextField(
                        value = postDescription,
                        onValueChange = { viewModel.postDescription.value = it },
                        label = { Text("Description") },
                        placeholder = { Text("Describe condition, included accessories, usage history...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        maxLines = 6
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Brand") },
                            placeholder = { Text("e.g. Sony, Apple") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = modelNumber,
                            onValueChange = { modelNumber = it },
                            label = { Text("Model #") },
                            placeholder = { Text("e.g. WH-1000XM4") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = size,
                            onValueChange = { size = it },
                            label = { Text("Size / Capacity") },
                            placeholder = { Text("e.g. Medium, 256GB") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = color,
                            onValueChange = { color = it },
                            label = { Text("Color") },
                            placeholder = { Text("e.g. Space Gray") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("Custom SKU / Inventory ID (Optional)") },
                        placeholder = { Text("e.g. SS-INV-0042") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) }
                    )
                }

                // STEP 3: CATEGORY & CONDITION
                PostingStep.CATEGORY_CONDITION -> {
                    Text("Category & Condition", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Select the category that best represents your product.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Text("Category", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    val categories = listOf(
                        ItemCategory.ELECTRONICS.name to "Electronics & Tech",
                        ItemCategory.APPAREL.name to "Clothing & Shoes",
                        ItemCategory.HOME_GARDEN.name to "Home & Garden",
                        ItemCategory.SPORTING_GOODS.name to "Sporting Goods",
                        ItemCategory.VEHICLES.name to "Vehicles & Auto",
                        ItemCategory.NEW_ITEMS.name to "General Merchandise"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { (catKey, catLabel) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (postCategory == catKey) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (postCategory == catKey) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.postCategory.value = catKey }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        catLabel,
                                        fontWeight = if (postCategory == catKey) FontWeight.Bold else FontWeight.Medium,
                                        color = if (postCategory == catKey) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (postCategory == catKey) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Condition", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    val conditions = listOf(
                        ItemCondition.NEW.name to ("Brand New" to "Unopened with original packaging and tags"),
                        ItemCondition.USED_LIKE_NEW.name to ("Used - Like New" to "Flawless, virtually no signs of wear"),
                        ItemCondition.USED_GOOD.name to ("Used - Good" to "Minor cosmetic wear, fully functional"),
                        ItemCondition.USED_FAIR.name to ("Used - Fair" to "Noticeable wear, fully operational")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        conditions.forEach { (condKey, pair) ->
                            val (condTitle, condDesc) = pair
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (postCondition == condKey) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (postCondition == condKey) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.postCondition.value = condKey }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = postCondition == condKey,
                                        onClick = { viewModel.postCondition.value = condKey }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(condTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(condDesc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                // STEP 4: PRICING & QUANTITY
                PostingStep.PRICING_QUANTITY -> {
                    Text("Price & Quantity", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Set your listing price and stock availability.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = postPrice,
                        onValueChange = { viewModel.postPrice.value = it },
                        label = { Text("Listing Price ($) *") },
                        placeholder = { Text("0.00") },
                        leadingIcon = { Text("$", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("listing_price_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = salePrice,
                        onValueChange = { salePrice = it },
                        label = { Text("Original / Strikethrough Price (Optional)") },
                        placeholder = { Text("e.g. 249.99 (Shows discount)") },
                        leadingIcon = { Text("$", fontWeight = FontWeight.Medium, fontSize = 16.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Smart Price Estimator Button
                    OutlinedButton(
                        onClick = { viewModel.triggerAiPriceEstimate() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isEstimatingPrice) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Estimating market value...")
                        } else {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Smart Price Suggestion (Recent Marketplace Sales)")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Quantity in Stock", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Available units for sale", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = "$quantity",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            IconButton(
                                onClick = { quantity++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // STEP 5: DELIVERY OPTIONS & LOCATION
                PostingStep.DELIVERY_OPTIONS -> {
                    Text("Delivery & Pickup", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Choose fulfillment methods and set your pickup location.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = postPickupLocation,
                        onValueChange = { viewModel.postPickupLocation.value = it },
                        label = { Text("Pickup Location / Address *") },
                        placeholder = { Text("e.g. 742 Market St, San Francisco, CA") },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFFEF4444)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Enable ShopSafe Courier Delivery", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Nearby verified ShopSafe drivers can pickup and deliver to buyer with live GPS tracking.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = enableShopSafeDelivery, onCheckedChange = { enableShopSafeDelivery = it })
                            }

                            if (enableShopSafeDelivery) {
                                OutlinedTextField(
                                    value = deliveryFee,
                                    onValueChange = { deliveryFee = it },
                                    label = { Text("Base Local Delivery Fee ($)") },
                                    leadingIcon = { Text("$") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    // Dimensions & Weight
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = postDimensions,
                            onValueChange = { viewModel.postDimensions.value = it },
                            label = { Text("Dimensions") },
                            placeholder = { Text("12x12x12 in") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = postWeightLbs,
                            onValueChange = { viewModel.postWeightLbs.value = it },
                            label = { Text("Weight (lbs)") },
                            placeholder = { Text("15") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Required vehicle selector
                    Text("Required Vehicle Type", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    val vehicleTypes = listOf("Sedan", "SUV", "Pickup Truck", "Cargo Van")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        vehicleTypes.forEach { veh ->
                            FilterChip(
                                selected = postRequiredVehicle == veh,
                                onClick = { viewModel.postRequiredVehicle.value = veh },
                                label = { Text(veh, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // STEP 6: PUBLISH DESTINATIONS & MARKETPLACES
                PostingStep.MARKETPLACES -> {
                    Text("Publish Destinations", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Post directly to ShopSafe and cross-post to connected marketplaces simultaneously.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Destination 1: ShopSafe Marketplace
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, if (publishToShopSafe) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0284C7),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Shield, contentDescription = "ShopSafe", tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("ShopSafe Marketplace", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(6.dp)) {
                                        Text("Primary", color = Color(0xFF166534), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Text("Guaranteed local courier delivery, safe escrows, zero scam buyer protection.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Checkbox(checked = publishToShopSafe, onCheckedChange = { publishToShopSafe = it })
                        }
                    }

                    // Destination 2: Facebook Marketplace
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, if (publishToFacebook && isFacebookLinked) Color(0xFF1877F2) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF1877F2),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("f", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Facebook Marketplace", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (isFacebookLinked) {
                                            Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(6.dp)) {
                                                Text("Linked", color = Color(0xFF166534), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Text("Official Graph API sync • Auto two-way inventory updates", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isFacebookLinked) {
                                    Checkbox(checked = publishToFacebook, onCheckedChange = { publishToFacebook = it })
                                }
                            }

                            if (!isFacebookLinked) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { showConnectFacebookDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Continue with Facebook", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Destination 3: eBay
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, if (publishToEbay && isEbayLinked) Color(0xFFE53238) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0064D2),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("ebay", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("eBay Marketplace", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (isEbayLinked) {
                                            Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(6.dp)) {
                                                Text("Linked", color = Color(0xFF166534), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Text("Reach national buyers • Automatic inventory synchronization", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isEbayLinked) {
                                    Checkbox(checked = publishToEbay, onCheckedChange = { publishToEbay = it })
                                }
                            }

                            if (!isEbayLinked) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = { showConnectEbayDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Connect eBay Account", fontWeight = FontWeight.Bold, color = Color(0xFF0064D2))
                                }
                            }
                        }
                    }
                }

                // STEP 7: REVIEW & PUBLISH
                PostingStep.REVIEW -> {
                    Text("Review Listing", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Verify all details before publishing across selected marketplaces.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Live Card Preview
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(activePhotos.getOrNull(coverPhotoIndex) ?: activePhotos.firstOrNull()),
                                    contentDescription = postTitle,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Surface(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        "${activePhotos.size} Photos",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$${postPrice.ifBlank { "0.00" }}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF10B981)
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            postCondition.replace("_", " "),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = postTitle.ifBlank { "Untitled Listing" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )

                                Text(
                                    text = postDescription.ifBlank { "No description provided." },
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3
                                )

                                Divider(modifier = Modifier.padding(vertical = 4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(postPickupLocation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ShopSafe Courier Delivery Available ($$deliveryFee base)", fontSize = 12.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Publishing Destinations Summary
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Publishing Destinations:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (publishToShopSafe) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ShopSafe Marketplace (Instant Active)", fontSize = 12.sp)
                                }
                            }
                            if (publishToFacebook && isFacebookLinked) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Facebook Marketplace (Auto-sync enabled)", fontSize = 12.sp)
                                }
                            }
                            if (publishToEbay && isEbayLinked) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF0064D2), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("eBay (Fixed price listing sync)", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Connect Facebook Account
    if (showConnectFacebookDialog) {
        AlertDialog(
            onDismissRequest = { showConnectFacebookDialog = false },
            title = { Text("Connect Facebook Account", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Authorize ShopSafe to publish and synchronize eligible listings with Facebook Marketplace using Meta's official Graph API.", fontSize = 14.sp)
                    Text("• Minimum permissions requested\n• Your Facebook password is never stored\n• Two-way inventory quantity synchronization", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isFacebookLinked = true
                        publishToFacebook = true
                        showConnectFacebookDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
                ) {
                    Text("Authorize Facebook")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConnectFacebookDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Connect eBay Account
    if (showConnectEbayDialog) {
        AlertDialog(
            onDismissRequest = { showConnectEbayDialog = false },
            title = { Text("Connect eBay Account", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Link your official eBay seller account to cross-post and import listings automatically.", fontSize = 14.sp)
                    Text("• Official eBay OAuth authentication\n• Two-way stock level sync\n• Import existing active listings with 1-tap", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isEbayLinked = true
                        publishToEbay = true
                        showConnectEbayDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0064D2))
                ) {
                    Text("Authorize eBay")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConnectEbayDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Import Existing Listings
    if (showImportListingsDialog) {
        AlertDialog(
            onDismissRequest = { showImportListingsDialog = false },
            title = { Text("Import Marketplace Listings", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select a connected marketplace to import your active listings into ShopSafe:", fontSize = 14.sp)

                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Simulate Facebook import
                                viewModel.postTitle.value = "Samsung 55-inch 4K Crystal UHD Smart TV"
                                viewModel.postPrice.value = "290.00"
                                viewModel.postCategory.value = ItemCategory.ELECTRONICS.name
                                viewModel.postCondition.value = ItemCondition.USED_LIKE_NEW.name
                                viewModel.postDescription.value = "Imported from Facebook Marketplace: Perfect condition smart TV with HDR and remote included."
                                showImportListingsDialog = false
                            }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color(0xFF1877F2), shape = RoundedCornerShape(6.dp), modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text("f", color = Color.White, fontWeight = FontWeight.Bold) }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Facebook Marketplace", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Import 4 active listings", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                    }

                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Simulate eBay import
                                viewModel.postTitle.value = "Vintage Leather Messenger Bag"
                                viewModel.postPrice.value = "65.00"
                                viewModel.postCategory.value = ItemCategory.APPAREL.name
                                viewModel.postCondition.value = ItemCondition.USED_GOOD.name
                                viewModel.postDescription.value = "Imported from eBay: Handcrafted genuine full grain leather briefcase bag."
                                showImportListingsDialog = false
                            }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color(0xFF0064D2), shape = RoundedCornerShape(6.dp), modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text("ebay", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("eBay Listings", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Import 7 active products", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImportListingsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
