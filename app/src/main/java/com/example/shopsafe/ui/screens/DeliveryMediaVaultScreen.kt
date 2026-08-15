package com.example.shopsafe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.shopsafe.data.models.DeliveryMediaFile
import com.example.shopsafe.data.models.DeliveryMediaType
import com.example.shopsafe.data.models.DeliveryOrderVault
import com.example.shopsafe.ui.ShopSafeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryMediaVaultScreen(
    viewModel: ShopSafeViewModel,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("ALL") } // ALL, PICKUP, RECEIPT, DROPOFF, DAMAGE
    var searchQuery by remember { mutableStateOf("") }
    var selectedMediaFile by remember { mutableStateOf<DeliveryMediaFile?>(null) }
    var showRetentionConfigDialog by remember { mutableStateOf(false) }
    var currentRetentionPeriod by remember { mutableStateOf("365 Days (1 Year)") }

    // Seeded Sample Vault Data organized by Order -> Date -> File Type
    val sampleVaultOrders = remember {
        listOf(
            DeliveryOrderVault(
                orderId = "SS-ORD-9021",
                orderNumber = "#SS-10582",
                customerName = "Sarah Jenkins",
                storeName = "Whole Foods Market",
                deliveryDate = "Aug 15, 2026",
                mediaFiles = listOf(
                    DeliveryMediaFile(
                        orderId = "SS-ORD-9021",
                        orderNumber = "#SS-10582",
                        fileType = DeliveryMediaType.PICKUP_CONFIRMATION,
                        fileUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=600",
                        uploadDate = "Aug 15, 2026 10:24 AM",
                        storeOrCustomerName = "Whole Foods - Aisle 4",
                        notes = "Fresh groceries confirmed packed in thermal cold bags."
                    ),
                    DeliveryMediaFile(
                        orderId = "SS-ORD-9021",
                        orderNumber = "#SS-10582",
                        fileType = DeliveryMediaType.CHECKOUT_REGISTER,
                        fileUrl = "https://images.unsplash.com/photo-1556742049-0a67e557b6f6?w=600",
                        uploadDate = "Aug 15, 2026 10:31 AM",
                        storeOrCustomerName = "Register #4 POS Terminal",
                        notes = "POS total: $42.66 matches card authorization."
                    ),
                    DeliveryMediaFile(
                        orderId = "SS-ORD-9021",
                        orderNumber = "#SS-10582",
                        fileType = DeliveryMediaType.STORE_RECEIPT,
                        fileUrl = "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=600",
                        uploadDate = "Aug 15, 2026 10:32 AM",
                        storeOrCustomerName = "Whole Foods Market #102",
                        notes = "Original paper receipt preserved with tax breakdown."
                    ),
                    DeliveryMediaFile(
                        orderId = "SS-ORD-9021",
                        orderNumber = "#SS-10582",
                        fileType = DeliveryMediaType.DROPOFF_PROOF,
                        fileUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600",
                        uploadDate = "Aug 15, 2026 10:52 AM",
                        storeOrCustomerName = "Customer Front Porch",
                        notes = "Package placed on table beside front door, recipient notified."
                    )
                )
            ),
            DeliveryOrderVault(
                orderId = "SS-ORD-8840",
                orderNumber = "#SS-10499",
                customerName = "David Chen",
                storeName = "Best Buy Electronics",
                deliveryDate = "Aug 14, 2026",
                mediaFiles = listOf(
                    DeliveryMediaFile(
                        orderId = "SS-ORD-8840",
                        orderNumber = "#SS-10499",
                        fileType = DeliveryMediaType.PICKUP_CONFIRMATION,
                        fileUrl = "https://images.unsplash.com/photo-1526738549149-8e07eca6c147?w=600",
                        uploadDate = "Aug 14, 2026 03:15 PM",
                        storeOrCustomerName = "Best Buy Pickup Counter",
                        notes = "4K Monitor sealed in factory box with security tag removed."
                    ),
                    DeliveryMediaFile(
                        orderId = "SS-ORD-8840",
                        orderNumber = "#SS-10499",
                        fileType = DeliveryMediaType.DROPOFF_PROOF,
                        fileUrl = "https://images.unsplash.com/photo-1584438784894-089d6a62b8fa?w=600",
                        uploadDate = "Aug 14, 2026 03:40 PM",
                        storeOrCustomerName = "Building Concierge Desk",
                        notes = "Handed directly to Concierge desk per building protocol."
                    ),
                    DeliveryMediaFile(
                        orderId = "SS-ORD-8840",
                        orderNumber = "#SS-10499",
                        fileType = DeliveryMediaType.EXCEPTION_DAMAGE,
                        fileUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=600",
                        uploadDate = "Aug 14, 2026 03:41 PM",
                        storeOrCustomerName = "Outer Carton Corner",
                        notes = "Pre-existing outer box crease noted before handover. Electronics untouched."
                    )
                )
            )
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color(0xFF0F172A),
                shadowElevation = 4.dp
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
                            IconButton(onClick = onClose) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Delivery Media Vault",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFF10B981),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "AES-256 Encrypted",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Secure authenticated storage for delivery proof & receipts",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = { showRetentionConfigDialog = true }) {
                            Icon(Icons.Default.Policy, contentDescription = "Retention Policy", tint = Color(0xFF38BDF8))
                        }
                    }

                    // Search and Filter Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by Order #, Store, or Customer...", color = Color(0xFF64748B), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Type Filter Pills
                    ScrollableTabRow(
                        selectedTabIndex = when (selectedTab) {
                            "PICKUP" -> 1
                            "RECEIPT" -> 2
                            "DROPOFF" -> 3
                            "DAMAGE" -> 4
                            else -> 0
                        },
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color(0xFF38BDF8),
                        edgePadding = 16.dp,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == "ALL",
                            onClick = { selectedTab = "ALL" },
                            text = { Text("All Records", fontSize = 12.sp, color = if (selectedTab == "ALL") Color(0xFF38BDF8) else Color(0xFF94A3B8)) }
                        )
                        Tab(
                            selected = selectedTab == "PICKUP",
                            onClick = { selectedTab = "PICKUP" },
                            text = { Text("Pickup Photos", fontSize = 12.sp, color = if (selectedTab == "PICKUP") Color(0xFF38BDF8) else Color(0xFF94A3B8)) }
                        )
                        Tab(
                            selected = selectedTab == "RECEIPT",
                            onClick = { selectedTab = "RECEIPT" },
                            text = { Text("Receipts & Registers", fontSize = 12.sp, color = if (selectedTab == "RECEIPT") Color(0xFF38BDF8) else Color(0xFF94A3B8)) }
                        )
                        Tab(
                            selected = selectedTab == "DROPOFF",
                            onClick = { selectedTab = "DROPOFF" },
                            text = { Text("Dropoff Proof", fontSize = 12.sp, color = if (selectedTab == "DROPOFF") Color(0xFF38BDF8) else Color(0xFF94A3B8)) }
                        )
                        Tab(
                            selected = selectedTab == "DAMAGE",
                            onClick = { selectedTab = "DAMAGE" },
                            text = { Text("Exceptions & Damage", fontSize = 12.sp, color = if (selectedTab == "DAMAGE") Color(0xFF38BDF8) else Color(0xFF94A3B8)) }
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF020617)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(sampleVaultOrders) { orderVault ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Order Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        orderVault.orderNumber,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color(0xFF1E293B),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            orderVault.deliveryDate,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    "${orderVault.storeName} → ${orderVault.customerName}",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Surface(
                                color = Color(0xFF065F46),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Verified Proof", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Divider(color = Color(0xFF1E293B))

                        // Files Grid in Order
                        val files = orderVault.mediaFiles.filter { file ->
                            when (selectedTab) {
                                "PICKUP" -> file.fileType == DeliveryMediaType.PICKUP_CONFIRMATION
                                "RECEIPT" -> file.fileType in listOf(DeliveryMediaType.STORE_RECEIPT, DeliveryMediaType.CHECKOUT_REGISTER)
                                "DROPOFF" -> file.fileType == DeliveryMediaType.DROPOFF_PROOF
                                "DAMAGE" -> file.fileType == DeliveryMediaType.EXCEPTION_DAMAGE
                                else -> true
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            files.forEach { mediaFile ->
                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedMediaFile = mediaFile }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(mediaFile.fileUrl),
                                            contentDescription = mediaFile.fileType.name,
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val badgeColor = when (mediaFile.fileType) {
                                                    DeliveryMediaType.PICKUP_CONFIRMATION -> Color(0xFF0284C7)
                                                    DeliveryMediaType.CHECKOUT_REGISTER -> Color(0xFFF59E0B)
                                                    DeliveryMediaType.STORE_RECEIPT -> Color(0xFF10B981)
                                                    DeliveryMediaType.DROPOFF_PROOF -> Color(0xFF8B5CF6)
                                                    DeliveryMediaType.EXCEPTION_DAMAGE -> Color(0xFFEF4444)
                                                    else -> Color(0xFF64748B)
                                                }
                                                Surface(
                                                    color = badgeColor.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        mediaFile.fileType.name.replace("_", " "),
                                                        color = badgeColor,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                mediaFile.storeOrCustomerName,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                mediaFile.uploadDate,
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp
                                            )
                                        }

                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = "View",
                                            tint = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Full-Resolution Media Viewer & Signed Verification
    selectedMediaFile?.let { media ->
        AlertDialog(
            onDismissRequest = { selectedMediaFile = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delivery Proof Record", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(media.fileUrl),
                            contentDescription = "Original Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• Order Number: ${media.orderNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("• Type: ${media.fileType.name.replace("_", " ")}", fontSize = 12.sp)
                            Text("• Capture Time: ${media.uploadDate}", fontSize = 12.sp)
                            Text("• Driver ID: ${media.driverId} (Verified)", fontSize = 12.sp)
                            Text("• Retention: Retained securely for ${media.retentionExpiryDays} days", fontSize = 11.sp, color = Color(0xFF64748B))
                            if (media.notes.isNotBlank()) {
                                Text("• Notes: ${media.notes}", fontSize = 12.sp, color = Color(0xFF0F172A))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedMediaFile = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        // Download/Print record
                        selectedMediaFile = null
                    }
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export / Print")
                }
            }
        )
    }

    // Modal: Retention Configuration
    if (showRetentionConfigDialog) {
        AlertDialog(
            onDismissRequest = { showRetentionConfigDialog = false },
            title = { Text("Vault Retention & Compliance", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Configure legal retention schedule for delivery photos, register receipts, and dispute documentation.", fontSize = 13.sp)
                    val options = listOf("90 Days (Disputes)", "365 Days (1 Year)", "7 Years (Tax & Accounting)")
                    options.forEach { opt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentRetentionPeriod = opt }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = currentRetentionPeriod == opt, onClick = { currentRetentionPeriod = opt })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(opt, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showRetentionConfigDialog = false }) {
                    Text("Save Policy")
                }
            }
        )
    }
}
