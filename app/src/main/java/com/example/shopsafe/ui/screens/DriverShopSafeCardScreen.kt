package com.example.shopsafe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopsafe.data.models.StripeAuthStatus
import com.example.shopsafe.data.models.StripeCardStatus
import com.example.shopsafe.data.models.StripeIssuingCard
import com.example.shopsafe.ui.ShopSafeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Driver-Only Screen: SHOPSAFE CARD
 * 
 * Displays the driver's official ShopSafe Stripe Issuing Virtual Purchasing Card,
 * real-time order spending allowance, card security controls, and recent authorizations.
 * Full sensitive details are protected via simulated Stripe Issuing ephemeral keys.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverShopSafeCardScreen(
    viewModel: ShopSafeViewModel,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val driverCard by viewModel.driverIssuingCard.collectAsState()
    val authorizations by viewModel.issuingAuthorizations.collectAsState()
    val activeOffer by viewModel.activeDriverOffer.collectAsState()
    val tokenizationState by viewModel.stripeTokenizationState.collectAsState()

    var isDetailsRevealed by remember { mutableStateOf(false) }
    var revealCountdownSeconds by remember { mutableIntStateOf(0) }
    var showFreezeDialog by remember { mutableStateOf(false) }
    var showReportLostDialog by remember { mutableStateOf(false) }
    var showReplacementSuccessDialog by remember { mutableStateOf(false) }
    var showGooglePayProvisionedDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Ephemeral Key Secure Reveal Timer
    LaunchedEffect(isDetailsRevealed) {
        if (isDetailsRevealed) {
            revealCountdownSeconds = 15
            while (revealCountdownSeconds > 0) {
                delay(1000L)
                revealCountdownSeconds--
            }
            isDetailsRevealed = false
            viewModel.clearSensitiveTokenizedData()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color(0xFF0F172A),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onClose,
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ShopSafe Purchase Card",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFF6366F1).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "STRIPE ISSUING",
                                        color = Color(0xFF818CF8),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Dedicated In-Store Order Purchasing",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val refreshed = viewModel.refreshDriverIssuingCard()
                                snackbarMessage = if (refreshed) "Card limits & status refreshed from Stripe." else "Card up to date."
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF38BDF8))
                    }
                }
            }
        },
        containerColor = Color(0xFF0B1120)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // Snackbar notification if any
            snackbarMessage?.let { msg ->
                item {
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(msg, color = Color.White, fontSize = 12.sp)
                            }
                            IconButton(
                                onClick = { snackbarMessage = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 1. Virtual Card Visualizer
            item {
                VirtualCardWidget(
                    card = driverCard,
                    tokenizedDetails = tokenizationState.tokenizedDetails,
                    isDetailsRevealed = isDetailsRevealed,
                    countdownSeconds = revealCountdownSeconds,
                    isTokenizing = tokenizationState.isTokenizing,
                    onToggleReveal = {
                        if (!isDetailsRevealed) {
                            coroutineScope.launch {
                                viewModel.revealCardDetailsSecurely()
                                isDetailsRevealed = true
                            }
                        } else {
                            isDetailsRevealed = false
                            viewModel.clearSensitiveTokenizedData()
                        }
                    }
                )
            }

            // 2. Active Order Spending Allowance Card
            item {
                ActiveOrderSpendingAllowanceCard(
                    card = driverCard,
                    activeOfferOrderId = activeOffer?.orderId,
                    storeName = activeOffer?.storeName
                )
            }

            // 3. Google Pay Digital Wallet Push Provisioning & Tokenization
            item {
                GooglePayPushTokenizationCard(
                    card = driverCard,
                    tokenizationState = tokenizationState,
                    onTokenizeForGooglePay = {
                        coroutineScope.launch {
                            val res = viewModel.tokenizeCardForGooglePay()
                            if (res.isSuccess) {
                                showGooglePayProvisionedDialog = true
                            } else {
                                snackbarMessage = "Google Pay tokenization failed: ${res.exceptionOrNull()?.message}"
                            }
                        }
                    }
                )
            }

            // 4. Quick Card Controls Action Row
            item {
                CardControlsActionRow(
                    card = driverCard,
                    onToggleFreeze = { showFreezeDialog = true },
                    onReportLost = { showReportLostDialog = true },
                    onReplaceCard = {
                        coroutineScope.launch {
                            viewModel.requestReplacementIssuingCard()
                            showReplacementSuccessDialog = true
                        }
                    }
                )
            }

            // 5. Secure Purchasing Protections & Tokenization Policy Info
            item {
                CardSecurityPolicyCard()
            }

            // 5. Recent Authorizations & Transactions Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Card Authorizations",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${authorizations.size} Records",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            // 6. List of recent authorizations
            if (authorizations.isEmpty()) {
                item {
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No recent card authorizations", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(authorizations) { auth ->
                    AuthorizationLogItem(auth)
                }
            }
        }
    }

    // Freeze Confirmation Dialog
    if (showFreezeDialog) {
        val isCurrentlyFrozen = driverCard?.isFrozenByDriver == true
        AlertDialog(
            onDismissRequest = { showFreezeDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text(
                    text = if (isCurrentlyFrozen) "Unfreeze Card?" else "Freeze ShopSafe Card?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isCurrentlyFrozen)
                        "Unfreezing will immediately reactivate your commercial purchasing card for active grocery orders."
                    else
                        "Freezing your card will temporarily block all store authorization requests until you unfreeze it. Use this if you misplaced your card.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFreezeDialog = false
                        coroutineScope.launch {
                            viewModel.toggleDriverCardFreeze(!isCurrentlyFrozen)
                            snackbarMessage = if (!isCurrentlyFrozen) "Card frozen successfully." else "Card unfrozen and ready for orders."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentlyFrozen) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                ) {
                    Text(if (isCurrentlyFrozen) "Unfreeze Now" else "Freeze Card")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFreezeDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Report Lost/Stolen Dialog
    if (showReportLostDialog) {
        AlertDialog(
            onDismissRequest = { showReportLostDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("Report Card Lost or Stolen", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will immediately permanently deactivate your current virtual card (•••• ${driverCard?.last4 ?: "4242"}). You will be issued a new instant virtual card for current order fulfillment.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReportLostDialog = false
                        coroutineScope.launch {
                            viewModel.reportDriverCardLostStolen()
                            snackbarMessage = "Card reported and deactivated. New virtual card generated."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Deactivate & Replace")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportLostDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Replacement Success Dialog
    if (showReplacementSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showReplacementSuccessDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("New Virtual Card Issued", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Your new ShopSafe Stripe Issuing Virtual Card (•••• ${driverCard?.last4 ?: "4242"}) is active and ready for your current grocery order.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showReplacementSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Got It")
                }
            }
        )
    }

    // Google Pay Provisioning Success Dialog
    if (showGooglePayProvisionedDialog) {
        val payload = tokenizationState.pushProvisioningPayload
        AlertDialog(
            onDismissRequest = { showGooglePayProvisionedDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google Pay Tokenized", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Your ShopSafe card (•••• ${driverCard?.last4 ?: "4242"}) has been securely tokenized with the Stripe Issuing Push Provisioning API for Google Pay contactless in-store payments.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("PUSH TOKEN: ${payload?.pushToken ?: "pushtok_verified"}", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("SECURITY: Ephemeral Key Signed (AES-256)", color = Color(0xFF10B981), fontSize = 10.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showGooglePayProvisionedDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    Text("Done", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Interactive Virtual Purchasing Card Component
 */
@Composable
private fun VirtualCardWidget(
    card: StripeIssuingCard?,
    tokenizedDetails: com.example.shopsafe.data.models.StripeCardTokenizedDetails?,
    isDetailsRevealed: Boolean,
    countdownSeconds: Int,
    isTokenizing: Boolean,
    onToggleReveal: () -> Unit
) {
    val isFrozen = card?.isFrozenByDriver == true || card?.status == StripeCardStatus.FROZEN

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(18.dp)),
        color = Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (isFrozen) Color(0xFFEF4444).copy(alpha = 0.6f) else Color(0xFF38BDF8).copy(alpha = 0.4f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isFrozen) {
                        Brush.linearGradient(
                            listOf(Color(0xFF3B0764), Color(0xFF1E1B4B), Color(0xFF0F172A))
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(Color(0xFF0284C7), Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    }
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Brand + Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ShopSafe Commercial",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Surface(
                        color = if (isFrozen) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isFrozen) Color(0xFFEF4444) else Color(0xFF10B981)
                        )
                    ) {
                        Text(
                            text = if (isFrozen) "FROZEN" else "ACTIVE",
                            color = if (isFrozen) Color(0xFFF87171) else Color(0xFF34D399),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Middle: Chip & Contactless & Card Number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Chip Emblem
                        Surface(
                            color = Color(0xFFFBBF24),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .width(32.dp)
                                .height(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(3.dp)
                                    .border(0.5.dp, Color(0xFF78350F), RoundedCornerShape(2.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            Icons.Default.Contactless,
                            contentDescription = "Contactless NFC",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Masked PAN or Ephemeral Reveal
                    Text(
                        text = if (isDetailsRevealed) {
                            tokenizedDetails?.fullPan ?: "4000 1234 5678 ${card?.last4 ?: "4242"}"
                        } else {
                            "•••• •••• •••• ${card?.last4 ?: "4242"}"
                        },
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                // Bottom row: Cardholder Name, Expiry, CVV & Reveal Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "CARDHOLDER",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = (card?.driverName ?: "DAVID CHEN").uppercase(),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = "EXPIRES",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isDetailsRevealed && tokenizedDetails != null) {
                                "${tokenizedDetails.expMonth}/${tokenizedDetails.expYear.toString().takeLast(2)}"
                            } else {
                                "${card?.expMonth ?: 12}/${card?.expYear?.toString()?.takeLast(2) ?: "29"}"
                            },
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = "CVC",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isDetailsRevealed) (tokenizedDetails?.cvc ?: "842") else "•••",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = if (isDetailsRevealed) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { onToggleReveal() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isTokenizing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    if (isDetailsRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = if (isDetailsRevealed) Color(0xFF0F172A) else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDetailsRevealed) "${countdownSeconds}s" else "Reveal",
                                color = if (isDetailsRevealed) Color(0xFF0F172A) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Google Pay Push Provisioning & Contactless Tokenization Card
 */
@Composable
private fun GooglePayPushTokenizationCard(
    card: StripeIssuingCard?,
    tokenizationState: com.example.shopsafe.data.models.StripeTokenizationState,
    onTokenizeForGooglePay: () -> Unit
) {
    val isProvisioned = tokenizationState.pushProvisioningPayload != null
    val isTokenizing = tokenizationState.isTokenizing

    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Nfc,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.padding(6.dp).size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Google Pay & Contactless POS",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Stripe Issuing Push Provisioning",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    color = if (isProvisioned) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF0284C7).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isProvisioned) "TOKENIZED" else "READY TO PUSH",
                        color = if (isProvisioned) Color(0xFF34D399) else Color(0xFF38BDF8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Push tokenize your physical or virtual ShopSafe purchasing card directly to Google Wallet. Uses server-side ephemeral keys and OPCS encryption with zero plaintext PAN stored on device.",
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onTokenizeForGooglePay,
                enabled = !isTokenizing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isProvisioned) Color(0xFF047857) else Color(0xFF0284C7)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isTokenizing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tokenizing via Stripe API...", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        if (isProvisioned) Icons.Default.Check else Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isProvisioned) "Re-Tokenize / Sync with Wallet" else "Add to Google Pay",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Active Order Spending Allowance Card
 */
@Composable
private fun ActiveOrderSpendingAllowanceCard(
    card: StripeIssuingCard?,
    activeOfferOrderId: String?,
    storeName: String?
) {
    val hasActiveOrder = activeOfferOrderId != null || card?.currentOrderBoundId != null
    val currentOrderId = activeOfferOrderId ?: card?.currentOrderBoundId ?: "SS-ORD-9021"
    val allowance = if (card != null && card.activeSpendingAllowance > 0) card.activeSpendingAllowance else 95.00

    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Storefront,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Order Spending Allowance",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    color = Color(0xFF0284C7).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = currentOrderId,
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Authorized Spending Limit",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$${String.format("%.2f", allowance)}",
                        color = Color(0xFF10B981),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Store Bound",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                    Text(
                        text = storeName ?: "Safeway #1492",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Includes 15% automatic substitution cushion for price variances.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Card Quick Actions Row (Freeze, Report Lost, Replace)
 */
@Composable
private fun CardControlsActionRow(
    card: StripeIssuingCard?,
    onToggleFreeze: () -> Unit,
    onReportLost: () -> Unit,
    onReplaceCard: () -> Unit
) {
    val isFrozen = card?.isFrozenByDriver == true

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Freeze/Unfreeze
        Button(
            onClick = onToggleFreeze,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFrozen) Color(0xFF10B981) else Color(0xFF334155)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    if (isFrozen) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isFrozen) "Unfreeze" else "Freeze Card",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Report Lost
        Button(
            onClick = onReportLost,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.ReportProblem,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Report Lost",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Add to Digital Wallet / Google Pay
        Button(
            onClick = onReplaceCard,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Replace",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Security Safeguards and Anti-Fraud Policy Card
 */
@Composable
private fun CardSecurityPolicyCard() {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stripe Issuing Safeguards", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "• Card is active only during accepted ShopSafe courier trips.\n" +
                "• Merchant Category locked to Grocery, Supermarkets, and Pharmacies.\n" +
                "• Automatic POS Receipt Photo matching required immediately after checkout.",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Single Authorization Log Item
 */
@Composable
private fun AuthorizationLogItem(auth: com.example.shopsafe.data.models.StripeIssuingAuthorization) {
    val isApproved = auth.status == StripeAuthStatus.APPROVED

    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    color = if (isApproved) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isApproved) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isApproved) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(auth.merchantName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        "${auth.timestampFormatted} • ${auth.orderId}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                    if (!isApproved && auth.declineReason != null) {
                        Text(
                            "Declined: ${auth.declineReason}",
                            color = Color(0xFFF87171),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", auth.amountRequested)}",
                    color = if (isApproved) Color.White else Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = auth.status.name,
                    color = if (isApproved) Color(0xFF10B981) else Color(0xFFEF4444),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
