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
import com.example.shopsafe.data.models.BusinessExpense
import com.example.shopsafe.data.models.BusinessExpenseCategory
import com.example.shopsafe.ui.ShopSafeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessExpenseAccountingScreen(
    viewModel: ShopSafeViewModel,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<BusinessExpenseCategory?>(null) }
    var showAddExpenseModal by remember { mutableStateOf(false) }
    var selectedExpenseDetail by remember { mutableStateOf<BusinessExpense?>(null) }

    val sampleBusinessExpenses = remember {
        mutableStateListOf(
            BusinessExpense(
                id = "biz_1",
                companyEntity = "ShopSafe Technologies Inc.",
                merchant = "Google Cloud Platform",
                dateFormatted = "Aug 14, 2026",
                amount = 412.80,
                category = BusinessExpenseCategory.SOFTWARE_SAAS,
                notes = "Gemini Pro multimodal API calls and Maps Directions backend servers.",
                originalReceiptUri = "https://images.unsplash.com/photo-1556742049-0a67e557b6f6?w=600",
                quickbooksSyncStatus = "SYNCED",
                quickbooksRecordId = "QB-BIZ-10492"
            ),
            BusinessExpense(
                id = "biz_2",
                companyEntity = "ShopSafe Technologies Inc.",
                merchant = "Meta Advertising Platforms",
                dateFormatted = "Aug 10, 2026",
                amount = 650.00,
                category = BusinessExpenseCategory.ADVERTISING_MARKETING,
                notes = "Customer acquisition campaign in SF Bay Area & Facebook Marketplace cross-promotions.",
                originalReceiptUri = "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=600",
                quickbooksSyncStatus = "SYNCED",
                quickbooksRecordId = "QB-BIZ-10493"
            ),
            BusinessExpense(
                id = "biz_3",
                companyEntity = "ShopSafe Technologies Inc.",
                merchant = "Zebra Barcode Technologies",
                dateFormatted = "Aug 02, 2026",
                amount = 890.00,
                category = BusinessExpenseCategory.EQUIPMENT_HARDWARE,
                notes = "Hardware barcode & NFC scanning terminals for merchant pass partner stores.",
                originalReceiptUri = "https://images.unsplash.com/photo-1526738549149-8e07eca6c147?w=600",
                quickbooksSyncStatus = "SYNCED",
                quickbooksRecordId = "QB-BIZ-10494"
            )
        )
    }

    val totalExpenses = sampleBusinessExpenses.sumOf { it.amount }

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
                                        text = "Business Accounting & QuickBooks",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Text(
                                    text = "Corporate expense ledger & receipt archives",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFF22C55E),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("QuickBooks Online", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Total Corporate Expense Card
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Corporate Operating Expenses", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("$${String.format("%.2f", totalExpenses)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Button(
                                onClick = { showAddExpenseModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Expense", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleBusinessExpenses) { expense ->
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
                        Image(
                            painter = rememberAsyncImagePainter(expense.originalReceiptUri),
                            contentDescription = "Original Receipt",
                            modifier = Modifier
                                .size(56.dp)
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
                                Text(expense.merchant, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("$${String.format("%.2f", expense.amount)}", color = Color(0xFF34D399), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(expense.category.displayName, color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(expense.dateFormatted, color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text("QB Record: ${expense.quickbooksRecordId}", color = Color(0xFF22C55E), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // Modal: Add Corporate Expense
    if (showAddExpenseModal) {
        var bizMerchant by remember { mutableStateOf("") }
        var bizAmount by remember { mutableStateOf("") }
        var bizCategory by remember { mutableStateOf(BusinessExpenseCategory.SOFTWARE_SAAS) }
        var bizNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddExpenseModal = false },
            title = { Text("Add Business Operating Expense", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = bizMerchant,
                        onValueChange = { bizMerchant = it },
                        label = { Text("Vendor / Merchant") },
                        placeholder = { Text("e.g. AWS Cloud Services") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = bizAmount,
                        onValueChange = { bizAmount = it },
                        label = { Text("Amount ($)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = bizNotes,
                        onValueChange = { bizNotes = it },
                        label = { Text("Business Purpose / Memo") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bizMerchant.isNotBlank() && bizAmount.isNotBlank()) {
                            sampleBusinessExpenses.add(
                                0,
                                BusinessExpense(
                                    merchant = bizMerchant,
                                    amount = bizAmount.toDoubleOrNull() ?: 100.0,
                                    category = bizCategory,
                                    notes = bizNotes,
                                    originalReceiptUri = "https://images.unsplash.com/photo-1556742049-0a67e557b6f6?w=600"
                                )
                            )
                            showAddExpenseModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Record & Sync QB")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Expense Detail & QuickBooks View
    selectedExpenseDetail?.let { exp ->
        AlertDialog(
            onDismissRequest = { selectedExpenseDetail = null },
            title = { Text("Corporate Expense Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(exp.originalReceiptUri),
                            contentDescription = "Original Corporate Receipt",
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
                            Text("Vendor: ${exp.merchant}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Total: $${String.format("%.2f", exp.amount)}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF10B981))
                            Text("Category: ${exp.category.displayName}", fontSize = 12.sp)
                            Text("QuickBooks Record ID: ${exp.quickbooksRecordId}", fontSize = 11.sp, color = Color(0xFF0284C7))
                            Text("Notes: ${exp.notes}", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedExpenseDetail = null }) {
                    Text("Done")
                }
            }
        )
    }
}
