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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shopsafe.data.models.DriverProfile
import com.example.shopsafe.data.models.PayoutTransaction
import com.example.shopsafe.ui.ShopSafeViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * RequestPayoutScreen provides a dedicated screen for drivers to initiate secure,
 * automated transfers of their total earnings via Stripe Connect API with real-time
 * Cloud Firestore balance status updates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestPayoutScreen(
    viewModel: ShopSafeViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.driverProfile.collectAsStateWithLifecycle()
    val localPayouts by viewModel.payoutHistory.collectAsStateWithLifecycle()
    val firestorePayouts by viewModel.firestorePayoutHistory.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessingPayout.collectAsStateWithLifecycle()
    val processingStep by viewModel.payoutProcessingStep.collectAsStateWithLifecycle()
    val lastPayout by viewModel.lastPayoutResult.collectAsStateWithLifecycle()
    val showSuccessModal by viewModel.showPayoutSuccessModal.collectAsStateWithLifecycle()
    val selectedMethod by viewModel.selectedPayoutMethod.collectAsStateWithLifecycle()
    val customAmountText by viewModel.customPayoutAmountText.collectAsStateWithLifecycle()
    val errorMessage by viewModel.payoutErrorMessage.collectAsStateWithLifecycle()

    val availableBalance = profile?.currentBalance ?: 164.80
    val balanceStatus = profile?.balanceStatus ?: "AVAILABLE"
    val isPendingBalance = balanceStatus.equals("PENDING", ignoreCase = true)
    val pendingAmount = profile?.pendingTransferAmount ?: 0.0

    // Combine and deduplicate payouts
    val allPayouts = remember(localPayouts, firestorePayouts) {
        (firestorePayouts + localPayouts).distinctBy { it.id }.sortedByDescending { it.timestamp }
    }

    var transferAmount by remember(availableBalance) {
        mutableStateOf(if (availableBalance > 0) availableBalance else 164.80)
    }
    var selectedReceiptPayout by remember { mutableStateOf<PayoutTransaction?>(null) }

    val isInstantMethod = selectedMethod.contains("Instant", ignoreCase = true) || selectedMethod.contains("Debit", ignoreCase = true)
    val feeRate = if (isInstantMethod) 0.015 else 0.0
    val estimatedFee = if (isInstantMethod) (transferAmount * feeRate).coerceAtLeast(0.50) else 0.0
    val netTransferAmount = (transferAmount - estimatedFee).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Request Driver Payout",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFF6366F1),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "STRIPE CONNECT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Automated Direct Transfer",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("payout_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Firestore Sync Indicator
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFF0284C7),
                                shape = CircleShape,
                                modifier = Modifier.size(6.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Firestore Sync",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Total Earnings & Balance Status Hero Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F172A)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AVAILABLE FOR PAYOUT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.sp
                            )

                            // Status Badge
                            Surface(
                                color = if (isPendingBalance) Color(0xFFFEF3C7) else Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (isPendingBalance) Color(0xFFD97706) else Color(0xFF16A34A),
                                        shape = CircleShape,
                                        modifier = Modifier.size(6.dp)
                                    ) {}
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isPendingBalance) "PENDING TRANSFER" else "STATUS: AVAILABLE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPendingBalance) Color(0xFFB45309) else Color(0xFF15803D)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Large Balance Display
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", availableBalance)}",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.testTag("payout_available_balance_text")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Driver: ${profile?.name ?: "Alex Rivera"} • Stripe ID: ${profile?.stripeConnectAccountId ?: "acct_1Nzk4hShopSafeDriver"}",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )

                        // If balance is in Pending state, show Firestore sync banner
                        if (isPendingBalance && pendingAmount > 0.0) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = Color(0xFF78350F).copy(alpha = 0.4f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.HourglassTop,
                                        contentDescription = "Payout transfer in progress status indicator",
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Transfer in progress: $${String.format(Locale.US, "%.2f", pendingAmount)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFFFDE68A)
                                        )
                                        Text(
                                            text = "Status updated to 'Pending' in Cloud Firestore. Automated Stripe Connect routing in transit.",
                                            fontSize = 11.sp,
                                            color = Color(0xFFFDE68A).copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 1.5 Visual Earnings Breakdown Bar Chart Widget
            item {
                DriverEarningsSummaryWidget(
                    profile = profile,
                    onCashoutClick = { }
                )
            }

            // 2. Stripe Connect Account Verification Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF6366F1).copy(alpha = 0.15f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.CreditCard,
                                            contentDescription = "Stripe account card icon",
                                            tint = Color(0xFF6366F1),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Stripe Connect Custom Account",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Verified for Instant & ACH Transfers",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Connected ID", fontSize = 11.sp, color = Color.Gray)
                                Text("acct_1Nzk4hShopSafe", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("Default Bank", fontSize = 11.sp, color = Color.Gray)
                                Text("Chase Bank (•••• 8821)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("Instant Card", fontSize = 11.sp, color = Color.Gray)
                                Text("Visa (•••• 4242)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // 3. Amount to Transfer Selection (Preset 100% Total Earnings or custom)
            item {
                Text(
                    text = "Select Transfer Amount",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Quick Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 100% Total Earnings Button
                        Surface(
                            modifier = Modifier
                                .weight(1.3f)
                                .clickable {
                                    transferAmount = availableBalance
                                    viewModel.customPayoutAmountText.value = ""
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (transferAmount == availableBalance && customAmountText.isBlank()) Color(0xFF15803D) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (transferAmount == availableBalance && customAmountText.isBlank()) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Selected 100 percent payout option indicator",
                                        tint = if (transferAmount == availableBalance && customAmountText.isBlank()) Color(0xFFFEF08A) else Color(0xFF15803D),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "100% Total Earnings",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (transferAmount == availableBalance && customAmountText.isBlank()) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", availableBalance)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (transferAmount == availableBalance && customAmountText.isBlank()) Color.White else Color(0xFF15803D)
                                )
                            }
                        }

                        // $100 preset
                        if (availableBalance >= 100.0) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        transferAmount = 100.0
                                        viewModel.customPayoutAmountText.value = ""
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (transferAmount == 100.0 && customAmountText.isBlank()) Color(0xFF0F172A) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$100.00",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (transferAmount == 100.0 && customAmountText.isBlank()) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Preset",
                                        fontSize = 10.sp,
                                        color = if (transferAmount == 100.0 && customAmountText.isBlank()) Color(0xFF94A3B8) else Color.Gray
                                    )
                                }
                            }
                        }

                        // $50 preset
                        if (availableBalance >= 50.0) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        transferAmount = 50.0
                                        viewModel.customPayoutAmountText.value = ""
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (transferAmount == 50.0 && customAmountText.isBlank()) Color(0xFF0F172A) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$50.00",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (transferAmount == 50.0 && customAmountText.isBlank()) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Preset",
                                        fontSize = 10.sp,
                                        color = if (transferAmount == 50.0 && customAmountText.isBlank()) Color(0xFF94A3B8) else Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    // Custom Amount Text Field
                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = { input ->
                            viewModel.customPayoutAmountText.value = input
                            val parsed = input.toDoubleOrNull()
                            if (parsed != null && parsed > 0) {
                                transferAmount = parsed.coerceAtMost(availableBalance)
                            }
                        },
                        label = { Text("Or Enter Custom Amount ($)") },
                        leadingIcon = { Text("$", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                        trailingIcon = {
                            if (customAmountText.isNotEmpty()) {
                                IconButton(onClick = {
                                    viewModel.customPayoutAmountText.value = ""
                                    transferAmount = availableBalance
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_payout_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // 4. Payout Method Options (Stripe Connect Integrations)
            item {
                Text(
                    text = "Transfer Method & Destination",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val payoutMethods = listOf(
                Triple(
                    "Instant Cashout (Debit Card)",
                    "1.5% Fee • 1-2 Minutes via Stripe Instant Payouts",
                    "Visa Debit Card (•••• 4242)"
                ),
                Triple(
                    "Weekly Direct Deposit (ACH)",
                    "FREE (0% Fee) • 1-2 Business Days via Stripe ACH",
                    "Chase Bank Checking (•••• 8821)"
                ),
                Triple(
                    "ShopSafe Driver SafeCard",
                    "0% Fee • Instant Load to safe card balance",
                    "ShopSafe Card (•••• 9901)"
                )
            )

            items(payoutMethods) { (title, subtitle, destination) ->
                val isSelected = selectedMethod == title
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectedPayoutMethod.value = title }
                        .testTag("payout_method_${title.take(7)}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF0284C7)) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.selectedPayoutMethod.value = title }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (title.contains("Instant")) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "⚡ FASTEST",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFB45309),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = subtitle,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "To: $destination",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                }
            }

            // 5. Transfer Breakdown & Fee Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Transfer Summary & Stripe Settlement",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gross Payout Amount", fontSize = 12.sp, color = Color.Gray)
                            Text("$${String.format(Locale.US, "%.2f", transferAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Stripe Transfer Fee", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = if (estimatedFee > 0) "-$${String.format(Locale.US, "%.2f", estimatedFee)}" else "FREE ($0.00)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (estimatedFee > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Net Amount to Your Account",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", netTransferAmount)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                }
            }

            // Error notice if any
            if (errorMessage != null) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // 6. Action Button: Initiate Stripe Automated Transfer
            item {
                if (isProcessing) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = processingStep.ifBlank { "Executing automated payout via Stripe Connect API..." },
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.requestPayout(transferAmount, selectedMethod)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("initiate_payout_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16A34A)
                        ),
                        enabled = transferAmount > 0.0 && availableBalance > 0.0
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Initiate Payout ($${String.format(Locale.US, "%.2f", transferAmount)})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 7. Payout Transfer History & Stripe Receipts
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Payout History & Receipts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${allPayouts.size} Transfers",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            if (allPayouts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No previous payouts yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Initiate your first transfer above to see Stripe Connect receipts.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                items(allPayouts) { payout ->
                    PayoutTransactionItemCard(
                        payout = payout,
                        onClick = { selectedReceiptPayout = payout },
                        onSimulateSettlement = {
                            viewModel.simulateStripeSettlement(payout.id)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Success Confirmation Modal
    if (showSuccessModal && lastPayout != null) {
        StripePayoutSuccessModal(
            payout = lastPayout!!,
            onDismiss = { viewModel.dismissPayoutSuccessModal() }
        )
    }

    // Detailed Itemized Receipt Modal
    if (selectedReceiptPayout != null) {
        StripeReceiptDetailModal(
            payout = selectedReceiptPayout!!,
            onDismiss = { selectedReceiptPayout = null }
        )
    }
}

/**
 * PayoutTransactionItemCard displays individual Stripe transfer details and status badges.
 */
@Composable
fun PayoutTransactionItemCard(
    payout: PayoutTransaction,
    onClick: () -> Unit,
    onSimulateSettlement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPending = payout.status.equals("PENDING", ignoreCase = true)
    val sdf = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.US)
    val dateStr = sdf.format(Date(payout.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("payout_item_${payout.id.take(6)}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPending) Icons.Default.HourglassTop else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isPending) Color(0xFFD97706) else Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = payout.method,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", payout.amount)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = if (isPending) Color(0xFFFEF3C7) else Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isPending) "PENDING" else "COMPLETED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPending) Color(0xFFB45309) else Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stripe ID: ${payout.stripeTransferId.take(16)}...",
                    fontSize = 11.sp,
                    color = Color(0xFF6366F1),
                    fontWeight = FontWeight.Medium
                )

                if (isPending) {
                    TextButton(
                        onClick = onSimulateSettlement,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Simulate Bank Settlement", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("View Receipt", fontSize = 11.sp, color = Color.Gray)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    }
                }
            }
        }
    }
}

/**
 * StripePayoutSuccessModal pops up immediately after a payout transfer is initiated.
 */
@Composable
fun StripePayoutSuccessModal(
    payout: PayoutTransaction,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF15803D))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Payout Initiated!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Your transfer of $${String.format(Locale.US, "%.2f", payout.amount)} is processing via Stripe Connect.",
                    fontSize = 13.sp
                )
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("• Stripe Transfer ID: ${payout.stripeTransferId}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text("• Destination: ${payout.destinationAccount}", fontSize = 11.sp)
                        Text("• Firestore Status: 'Pending'", fontSize = 11.sp, color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                        Text("• Net Transfer: $${String.format(Locale.US, "%.2f", payout.netPayoutAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * StripeReceiptDetailModal displays the complete itemized transaction receipt.
 */
@Composable
fun StripeReceiptDetailModal(
    payout: PayoutTransaction,
    onDismiss: () -> Unit
) {
    val sdf = SimpleDateFormat("MMMM dd, yyyy • h:mm:ss a z", Locale.US)
    val dateStr = sdf.format(Date(payout.timestamp))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Stripe Payout Receipt", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Transfer Amount", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", payout.amount)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Status: ${payout.status}", fontSize = 11.sp, color = if (payout.status == "PENDING") Color(0xFFFBBF24) else Color(0xFF4ADE80), fontWeight = FontWeight.Bold)
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ReceiptRow(label = "Transfer Method", value = payout.method)
                    ReceiptRow(label = "Destination Account", value = payout.destinationAccount)
                    ReceiptRow(label = "Stripe Transfer ID", value = payout.stripeTransferId)
                    ReceiptRow(label = "Stripe Payout ID", value = payout.stripePayoutId)
                    ReceiptRow(label = "Processing Fee", value = if (payout.fee > 0) "$${String.format(Locale.US, "%.2f", payout.fee)}" else "FREE ($0.00)")
                    ReceiptRow(label = "Net Deposited", value = "$${String.format(Locale.US, "%.2f", payout.netPayoutAmount)}")
                    ReceiptRow(label = "Date & Time", value = dateStr)
                    ReceiptRow(label = "Firestore Backend", value = "driver_payouts collection (Synced)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
            ) {
                Text("Close Receipt")
            }
        }
    )
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
    }
}
