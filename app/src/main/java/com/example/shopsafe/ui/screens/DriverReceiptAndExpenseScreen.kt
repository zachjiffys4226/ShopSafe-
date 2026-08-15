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
import com.example.shopsafe.data.models.DriverExpense
import com.example.shopsafe.data.models.DriverExpenseCategory
import com.example.shopsafe.data.models.ReceiptOcrResult
import com.example.shopsafe.ui.ShopSafeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverReceiptAndExpenseScreen(
    viewModel: ShopSafeViewModel,
    onClose: () -> Unit,
    onOpenTaxCenter: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<DriverExpenseCategory?>(null) }
    var selectedPeriodFilter by remember { mutableStateOf("ALL") } // ALL, THIS_WEEK, THIS_MONTH, THIS_YEAR

    var showScanReceiptModal by remember { mutableStateOf(false) }
    var selectedExpenseDetail by remember { mutableStateOf<DriverExpense?>(null) }
    var showPrintReportModal by remember { mutableStateOf(false) }
    var showDuplicateWarningModal by remember { mutableStateOf(false) }

    // Seeded Sample Driver Expenses with Untouched Original Receipts & QuickBooks IDs
    val sampleExpenses = remember {
        mutableStateListOf(
            DriverExpense(
                id = "exp_1",
                merchant = "Chevron Gas Station #4102",
                dateFormatted = "Aug 15, 2026",
                amount = 48.50,
                subtotal = 44.80,
                salesTax = 3.70,
                paymentMethod = "ShopSafe Card (...4021)",
                category = DriverExpenseCategory.FUEL,
                isPotentialTaxWriteOff = true,
                taxDisclaimerNote = "Potential Business Expense (Schedule C) - Consult Tax Advisor",
                notes = "Filled tank during morning lunch delivery shift in Downtown SF.",
                originalReceiptUri = "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=600",
                receiptNumber = "CHV-993810",
                quickbooksSyncStatus = "SYNCED",
                quickbooksRecordId = "QB-EXP-88402"
            ),
            DriverExpense(
                id = "exp_2",
                merchant = "Firestone Auto Care",
                dateFormatted = "Aug 12, 2026",
                amount = 120.00,
                subtotal = 110.00,
                salesTax = 10.00,
                paymentMethod = "Visa Debit (...9120)",
                category = DriverExpenseCategory.VEHICLE_MAINTENANCE,
                isPotentialTaxWriteOff = true,
                taxDisclaimerNote = "Potential Business Expense - Actual Expense Method",
                notes = "Synthetic oil change and tire rotation for courier vehicle.",
                originalReceiptUri = "https://images.unsplash.com/photo-1556742049-0a67e557b6f6?w=600",
                receiptNumber = "FST-10492",
                quickbooksSyncStatus = "SYNCED",
                quickbooksRecordId = "QB-EXP-88403"
            ),
            DriverExpense(
                id = "exp_3",
                merchant = "Golden Gate Bridge Toll Plaza",
                dateFormatted = "Aug 10, 2026",
                amount = 9.75,
                subtotal = 9.75,
                salesTax = 0.0,
                paymentMethod = "FasTrak Auto-Pay",
                category = DriverExpenseCategory.TOLLS,
                isPotentialTaxWriteOff = true,
                taxDisclaimerNote = "Potential Business Expense - Directly tied to North Bay delivery",
                notes = "Toll crossing during Order #SS-10499 dropoff route.",
                originalReceiptUri = "https://images.unsplash.com/photo-1584438784894-089d6a62b8fa?w=600",
                receiptNumber = "FTK-29402",
                quickbooksSyncStatus = "SYNCED",
                quickbooksRecordId = "QB-EXP-88404"
            ),
            DriverExpense(
                id = "exp_4",
                merchant = "Amazon Commercial Supply",
                dateFormatted = "Aug 05, 2026",
                amount = 34.99,
                subtotal = 32.25,
                salesTax = 2.74,
                paymentMethod = "ShopSafe Card (...4021)",
                category = DriverExpenseCategory.DELIVERY_SUPPLIES,
                isPotentialTaxWriteOff = true,
                taxDisclaimerNote = "Potential Business Expense - Courier Equipment",
                notes = "2x Commercial insulated thermal catering delivery bags.",
                originalReceiptUri = "https://images.unsplash.com/photo-1526738549149-8e07eca6c147?w=600",
                receiptNumber = "AMZ-881290",
                quickbooksSyncStatus = "SYNCED",
                quickbooksRecordId = "QB-EXP-88405"
            ),
            DriverExpense(
                id = "exp_5",
                merchant = "Verizon Wireless",
                dateFormatted = "Aug 01, 2026",
                amount = 75.00,
                subtotal = 70.00,
                salesTax = 5.00,
                paymentMethod = "ShopSafe Card (...4021)",
                category = DriverExpenseCategory.PHONE_SERVICE,
                isPotentialTaxWriteOff = true,
                taxDisclaimerNote = "Potential Business Expense - Courier Mobile Data Allocation",
                notes = "50% business allocation for courier navigation & dispatch app.",
                originalReceiptUri = "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600",
                receiptNumber = "VRZ-55019",
                quickbooksSyncStatus = "SYNCED",
                quickbooksRecordId = "QB-EXP-88406"
            )
        )
    }

    // Calculated Dashboard Metrics
    val totalYearExpenses = sampleExpenses.sumOf { it.amount }
    val totalMonthExpenses = sampleExpenses.filter { it.dateFormatted.contains("Aug") }.sumOf { it.amount }
    val totalWeekExpenses = 168.50
    val potentialWriteOffs = sampleExpenses.filter { it.isPotentialTaxWriteOff }.sumOf { it.amount }

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
                                Text(
                                    text = "Receipts & Expense Tracker",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "Original receipt preservation & tax categorization",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showPrintReportModal = true }) {
                                Icon(Icons.Default.Print, contentDescription = "Print Report", tint = Color(0xFF38BDF8))
                            }
                            IconButton(onClick = onOpenTaxCenter) {
                                Icon(Icons.Default.AccountBalance, contentDescription = "Tax Center", tint = Color(0xFF10B981))
                            }
                        }
                    }

                    // Dashboard Summary Metric Cards
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("This Month", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("$${String.format("%.2f", totalMonthExpenses)}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("This Year", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("$${String.format("%.2f", totalYearExpenses)}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Surface(
                            color = Color(0xFF064E3B),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Potential Write-Offs", color = Color(0xFF6EE7B7), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text("$${String.format("%.2f", potentialWriteOffs)}", color = Color(0xFF34D399), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    // Tax Treatment Explanatory Banner
                    Surface(
                        color = Color(0xFF1E293B).copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "💡 Tax treatment depends on applicable tax rules and your individual circumstances (Standard Mileage vs Actual Expense method).",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }

                    // Search and Filter Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by Merchant, Amount, or Category...", color = Color(0xFF64748B), fontSize = 13.sp) },
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
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showScanReceiptModal = true },
                icon = { Icon(Icons.Default.DocumentScanner, contentDescription = "Scan Receipt", tint = Color.White) },
                text = { Text("Scan Receipt", fontWeight = FontWeight.Bold, color = Color.White) },
                containerColor = Color(0xFF10B981),
                modifier = Modifier.testTag("driver_scan_receipt_fab")
            )
        },
        containerColor = Color(0xFF020617)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val filtered = sampleExpenses.filter { exp ->
                (searchQuery.isBlank() || exp.merchant.contains(searchQuery, ignoreCase = true) || exp.notes.contains(searchQuery, ignoreCase = true) || exp.category.displayName.contains(searchQuery, ignoreCase = true)) &&
                (selectedCategoryFilter == null || exp.category == selectedCategoryFilter)
            }

            items(filtered) { expense ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedExpenseDetail = expense }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Original Receipt Thumbnail
                        Image(
                            painter = rememberAsyncImagePainter(expense.originalReceiptUri),
                            contentDescription = "Original Receipt",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = expense.merchant,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "$${String.format("%.2f", expense.amount)}",
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        expense.category.displayName,
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(expense.dateFormatted, color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Original Photo Saved", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                }

                                Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(4.dp)) {
                                    Text("QB: ${expense.quickbooksRecordId ?: "SYNCED"}", color = Color(0xFF38BDF8), fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Receipt Scanner & AI Extraction
    if (showScanReceiptModal) {
        var scannedMerchant by remember { mutableStateOf("Shell Gas Station #904") }
        var scannedTotal by remember { mutableStateOf("52.40") }
        var scannedSubtotal by remember { mutableStateOf("48.50") }
        var scannedTax by remember { mutableStateOf("3.90") }
        var scannedCategory by remember { mutableStateOf(DriverExpenseCategory.FUEL) }
        var scannedNotes by remember { mutableStateOf("Fuel refill for evening courier deliveries.") }
        var isSimulatingOcr by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showScanReceiptModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Driver Receipt Scanner", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Take or upload a photo of your receipt. The original photo is permanently preserved, while Gemini extracts accounting fields.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Receipt Camera Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter("https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=600"),
                            contentDescription = "Receipt Snapshot",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            color = Color(0xFF10B981),
                            shape = RoundedCornerShape(bottomStart = 8.dp),
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text("ORIGINAL PRESERVED", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    OutlinedTextField(
                        value = scannedMerchant,
                        onValueChange = { scannedMerchant = it },
                        label = { Text("Merchant / Business") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = scannedTotal,
                            onValueChange = { scannedTotal = it },
                            label = { Text("Total ($)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = scannedTax,
                            onValueChange = { scannedTax = it },
                            label = { Text("Tax ($)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Text("Expense Category", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    val categories = listOf(DriverExpenseCategory.FUEL, DriverExpenseCategory.VEHICLE_MAINTENANCE, DriverExpenseCategory.PARKING, DriverExpenseCategory.TOLLS, DriverExpenseCategory.DELIVERY_SUPPLIES, DriverExpenseCategory.PHONE_SERVICE)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = scannedCategory == cat,
                                onClick = { scannedCategory = cat },
                                label = { Text(cat.displayName, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = scannedNotes,
                        onValueChange = { scannedNotes = it },
                        label = { Text("Notes / Purpose") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✓ Categorized as Potential Tax Write-Off (Review during annual tax filing)",
                            color = Color(0xFF166534),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Check for duplicates
                        val isDuplicate = sampleExpenses.any { it.merchant.equals(scannedMerchant, ignoreCase = true) && it.amount == (scannedTotal.toDoubleOrNull() ?: 0.0) }
                        if (isDuplicate) {
                            showDuplicateWarningModal = true
                        } else {
                            sampleExpenses.add(
                                0,
                                DriverExpense(
                                    merchant = scannedMerchant,
                                    amount = scannedTotal.toDoubleOrNull() ?: 50.0,
                                    salesTax = scannedTax.toDoubleOrNull() ?: 0.0,
                                    category = scannedCategory,
                                    notes = scannedNotes,
                                    originalReceiptUri = "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=600"
                                )
                            )
                            showScanReceiptModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Save Expense Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScanReceiptModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Expense Detail & Print/Download Original
    selectedExpenseDetail?.let { exp ->
        AlertDialog(
            onDismissRequest = { selectedExpenseDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Expense & Original Receipt", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(exp.originalReceiptUri),
                            contentDescription = "Original Receipt Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Merchant: ${exp.merchant}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Total: $${String.format("%.2f", exp.amount)} (Tax: $${String.format("%.2f", exp.salesTax)})", fontSize = 13.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                            Text("Date: ${exp.dateFormatted}", fontSize = 12.sp)
                            Text("Category: ${exp.category.displayName}", fontSize = 12.sp)
                            Text("Classification: ${exp.taxDisclaimerNote}", fontSize = 11.sp, color = Color(0xFF0284C7))
                            Text("QuickBooks Record ID: ${exp.quickbooksRecordId}", fontSize = 11.sp, color = Color(0xFF64748B))
                            if (exp.notes.isNotBlank()) {
                                Text("Driver Notes: ${exp.notes}", fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedExpenseDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        // Print/Download receipt with original photo
                        selectedExpenseDetail = null
                    }
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print Receipt")
                }
            }
        )
    }

    // Modal: Print Expense Report
    if (showPrintReportModal) {
        AlertDialog(
            onDismissRequest = { showPrintReportModal = false },
            title = { Text("Export & Print Expense Report", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Generate a comprehensive summary report with attached original receipt images for your tax advisor or personal accounting.", fontSize = 13.sp)
                    Text("• Total Expenses: $${String.format("%.2f", totalYearExpenses)}\n• Potential Write-Offs: $${String.format("%.2f", potentialWriteOffs)}\n• Total Receipts Attached: ${sampleExpenses.size} items\n• Formats: PDF & CSV Export", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrintReportModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Download PDF Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrintReportModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Duplicate Receipt Warning
    if (showDuplicateWarningModal) {
        AlertDialog(
            onDismissRequest = { showDuplicateWarningModal = false },
            title = { Text("⚠️ Potential Duplicate Receipt", fontWeight = FontWeight.Bold) },
            text = {
                Text("An expense with this identical merchant and total amount was already recorded recently. Would you like to proceed anyway?", fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDuplicateWarningModal = false
                        showScanReceiptModal = false
                    }
                ) {
                    Text("Save Anyway")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDuplicateWarningModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
