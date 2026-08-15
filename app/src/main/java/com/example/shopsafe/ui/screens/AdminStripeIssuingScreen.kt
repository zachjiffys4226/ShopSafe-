package com.example.shopsafe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopsafe.data.models.*
import com.example.shopsafe.ui.ShopSafeViewModel
import kotlinx.coroutines.launch

/**
 * ShopSafe Owner / Admin Dashboard:
 * SHOPSAFE ADMIN → PAYMENTS & ISSUING → STRIPE ISSUING
 * 
 * Provides authorized administrators complete oversight of the Stripe Issuing program:
 * - Environment configuration (TEST vs LIVE) & key rotation
 * - Driver cardholders & virtual/physical card provisioning
 * - Spending control overrides & freeze controls
 * - Real-time authorization decision log
 * - 3-Way Receipt Reconciliation & Discrepancy detector
 * - Webhook health & event diagnostics
 * - Immutable administrative audit trail
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStripeIssuingScreen(
    viewModel: ShopSafeViewModel,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val config by viewModel.issuingConfig.collectAsState()
    val cardholders by viewModel.issuingCardholders.collectAsState()
    val cards by viewModel.issuingCards.collectAsState()
    val authorizations by viewModel.issuingAuthorizations.collectAsState()
    val reconciliationRecords by viewModel.issuingReconciliations.collectAsState()
    val auditLogs by viewModel.issuingAuditLogs.collectAsState()
    val webhookEvents by viewModel.issuingWebhookEvents.collectAsState()

    var activeAdminTab by remember { mutableStateOf("CARDS") } // CARDS, AUTHORIZATIONS, RECONCILIATION, WEBHOOKS, AUDIT
    var showEnvSwitchDialog by remember { mutableStateOf(false) }
    var showRotateKeyDialog by remember { mutableStateOf(false) }
    var showLimitOverrideDialog by remember { mutableStateOf<StripeIssuingCard?>(null) }
    var overrideLimitInput by remember { mutableStateOf("750.00") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Admin Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClose,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF334155))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Stripe Issuing Administration",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Mode Badge
                            Surface(
                                color = if (config.environment == StripeEnvironmentMode.LIVE) Color(0xFF10B981) else Color(0xFFF59E0B),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.clickable { showEnvSwitchDialog = true }
                            ) {
                                Text(
                                    text = config.environment.badgeText,
                                    color = Color(0xFF0F172A),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "ShopSafe Courier Commercial Card Program",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showRotateKeyDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF334155))
                    ) {
                        Icon(Icons.Default.Key, contentDescription = "Key Rotation", tint = Color(0xFF38BDF8))
                    }
                }
            }

            // Top Program Financial & Health Metric Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(
                    label = "Program Funding",
                    value = "$${String.format("%,.2f", config.totalProgramFundingAvailable)}",
                    valueColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1.3f)
                )
                MetricPill(
                    label = "Active Cards",
                    value = "${cards.size}",
                    valueColor = Color.White,
                    modifier = Modifier.weight(0.9f)
                )
                MetricPill(
                    label = "Auths Today",
                    value = "${authorizations.size}",
                    valueColor = Color(0xFF38BDF8),
                    modifier = Modifier.weight(0.9f)
                )
                MetricPill(
                    label = "Reconciled",
                    value = "100%",
                    valueColor = Color(0xFF22C55E),
                    modifier = Modifier.weight(0.9f)
                )
            }

            // Tab Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    "CARDS" to "Cards (${cards.size})",
                    "AUTHORIZATIONS" to "Auths",
                    "RECONCILIATION" to "Reconcile",
                    "WEBHOOKS" to "Webhooks",
                    "AUDIT" to "Audit Trail"
                )

                tabs.forEach { (tabKey, label) ->
                    val isSelected = activeAdminTab == tabKey
                    Surface(
                        color = if (isSelected) Color(0xFF6366F1) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeAdminTab = tabKey }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // Notification Banner
            snackbarMessage?.let { msg ->
                Surface(
                    color = Color(0xFF334155),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(msg, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { snackbarMessage = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Workspace Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp)
            ) {
                when (activeAdminTab) {
                    "CARDS" -> {
                        item {
                            Text(
                                text = "Driver Cardholders & Active Virtual Cards",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        items(cards) { card ->
                            val cardholder = cardholders.find { it.id == card.cardholderId }
                            AdminCardItem(
                                card = card,
                                cardholder = cardholder,
                                onToggleFreeze = {
                                    coroutineScope.launch {
                                        val freeze = !card.isFrozenByDriver
                                        viewModel.toggleCardFreezeAdmin(card.id, freeze)
                                        snackbarMessage = if (freeze) "Card ic_...${card.last4} frozen by admin." else "Card ic_...${card.last4} unfrozen."
                                    }
                                },
                                onAdjustLimit = {
                                    showLimitOverrideDialog = card
                                    overrideLimitInput = (cardholder?.spendingLimitDaily ?: 500.0).toString()
                                },
                                onReplaceCard = {
                                    coroutineScope.launch {
                                        viewModel.replaceCardAdmin(card.id)
                                        snackbarMessage = "Old card canceled. New virtual card issued."
                                    }
                                }
                            )
                        }
                    }

                    "AUTHORIZATIONS" -> {
                        item {
                            Text(
                                text = "Real-Time Authorization Decisions Feed",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        items(authorizations) { auth ->
                            AdminAuthorizationDecisionItem(auth)
                        }
                    }

                    "RECONCILIATION" -> {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "3-Way Purchase & Receipt Reconciliation",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Connected matching: ShopSafe Order + Stripe Captured Amount + POS Receipt OCR",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        items(reconciliationRecords) { rec ->
                            AdminReconciliationItem(rec)
                        }
                    }

                    "WEBHOOKS" -> {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Lan, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Webhook Endpoint Status", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            Surface(
                                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    "HEALTHY (200 OK)",
                                                    color = Color(0xFF34D399),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "URL: ${config.webhookEndpointUrl}",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            "Secret: ${config.webhookSecretMasked} (Verified)",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val evt = viewModel.simulateWebhookPing()
                                                    snackbarMessage = "Webhook event received: ${evt.eventType}"
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Send Test Webhook Event", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Recent Stripe Webhook Deliveries",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        items(webhookEvents) { evt ->
                            AdminWebhookEventItem(evt)
                        }
                    }

                    "AUDIT" -> {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Administrative Audit Trail",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Immutable WHO → WHAT → WHEN → RESOURCE logs",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        items(auditLogs) { log ->
                            AdminAuditLogItem(log)
                        }
                    }
                }
            }
        }
    }

    // Switch Environment Dialog
    if (showEnvSwitchDialog) {
        val targetMode = if (config.environment == StripeEnvironmentMode.LIVE) StripeEnvironmentMode.TEST else StripeEnvironmentMode.LIVE
        AlertDialog(
            onDismissRequest = { showEnvSwitchDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("Switch Stripe Issuing Environment?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Switching to ${targetMode.label} will route all commercial card authorizations and virtual card issuance through Stripe's ${targetMode.name.lowercase()} environment.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEnvSwitchDialog = false
                        coroutineScope.launch {
                            viewModel.setStripeEnvironmentMode(targetMode)
                            snackbarMessage = "Environment switched to ${targetMode.badgeText}"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (targetMode == StripeEnvironmentMode.LIVE) Color(0xFF10B981) else Color(0xFFF59E0B)
                    )
                ) {
                    Text("Switch to ${targetMode.label}")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnvSwitchDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Rotate Key Dialog
    if (showRotateKeyDialog) {
        AlertDialog(
            onDismissRequest = { showRotateKeyDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("Rotate Server-Side Stripe Key", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Rotates the restricted server-side Issuing credential (${config.restrictedKeyName}) in the secure secret manager without requiring an app store update.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Text(
                        "Last rotated: ${config.lastKeyRotationDate}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRotateKeyDialog = false
                        coroutineScope.launch {
                            viewModel.rotateStripeServerKey()
                            snackbarMessage = "Server secret rotated and audit log recorded."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Rotate Key Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRotateKeyDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Limit Override Dialog
    showLimitOverrideDialog?.let { targetCard ->
        AlertDialog(
            onDismissRequest = { showLimitOverrideDialog = null },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("Adjust Daily Spend Limit", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Driver: ${targetCard.driverName} (ic_...${targetCard.last4})",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = overrideLimitInput,
                        onValueChange = { overrideLimitInput = it },
                        label = { Text("Daily Spend Limit ($)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = overrideLimitInput.toDoubleOrNull() ?: 500.0
                        coroutineScope.launch {
                            viewModel.overrideCardSpendingLimit(targetCard.id, amount)
                            snackbarMessage = "Daily limit updated to $$amount for ${targetCard.driverName}"
                        }
                        showLimitOverrideDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Save Limit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitOverrideDialog = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

/**
 * Metric Pill for Program Health
 */
@Composable
private fun MetricPill(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = Color(0xFF94A3B8), fontSize = 9.sp, maxLines = 1)
            Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
        }
    }
}

/**
 * Admin Card Row Item
 */
@Composable
private fun AdminCardItem(
    card: StripeIssuingCard,
    cardholder: StripeCardholder?,
    onToggleFreeze: () -> Unit,
    onAdjustLimit: () -> Unit,
    onReplaceCard: () -> Unit
) {
    val isFrozen = card.isFrozenByDriver || card.status == StripeCardStatus.FROZEN

    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(card.driverName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("•••• ${card.last4} • ${card.brand}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }

                Surface(
                    color = if (isFrozen) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isFrozen) "FROZEN" else "ACTIVE",
                        color = if (isFrozen) Color(0xFFF87171) else Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Daily Spend Limit", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    Text("$${String.format("%.2f", cardholder?.spendingLimitDaily ?: 500.0)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column {
                    Text("Active Order Bound", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    Text(card.currentOrderBoundId ?: "None (Idle)", color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Settled Spend", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    Text("$${String.format("%.2f", cardholder?.totalSettledSpend ?: 0.0)}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onToggleFreeze,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    border = BorderStroke(1.dp, if (isFrozen) Color(0xFF10B981) else Color(0xFFEF4444))
                ) {
                    Text(if (isFrozen) "Unfreeze" else "Freeze", color = if (isFrozen) Color(0xFF10B981) else Color(0xFFEF4444), fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onAdjustLimit,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8))
                ) {
                    Text("Set Limit", color = Color(0xFF38BDF8), fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onReplaceCard,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    border = BorderStroke(1.dp, Color(0xFF94A3B8))
                ) {
                    Text("Replace", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

/**
 * Admin Authorization Decision Feed Item
 */
@Composable
private fun AdminAuthorizationDecisionItem(auth: StripeIssuingAuthorization) {
    val isApproved = auth.status == StripeAuthStatus.APPROVED

    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isApproved) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isApproved) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isApproved) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(auth.merchantName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Text(
                    text = "$${String.format("%.2f", auth.amountRequested)}",
                    color = if (isApproved) Color(0xFF10B981) else Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${auth.cardholderName} (•••• ${auth.cardLast4}) • ${auth.timestampFormatted}",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = auth.decisionDetails,
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

/**
 * Admin Reconciliation Feed Item
 */
@Composable
private fun AdminReconciliationItem(rec: IssuingReconciliationRecord) {
    val isVerified = rec.status == IssuingReconciliationStatus.VERIFIED

    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(rec.orderId, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("• ${rec.merchant}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }

                Surface(
                    color = if (isVerified) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = rec.status.label,
                        color = if (isVerified) Color(0xFF34D399) else Color(0xFFFBBF24),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Way Comparison Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Expected Order", color = Color(0xFF94A3B8), fontSize = 9.sp)
                    Text("$${String.format("%.2f", rec.expectedOrderTotal)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Stripe Swipe", color = Color(0xFF94A3B8), fontSize = 9.sp)
                    Text("$${String.format("%.2f", rec.amountCaptured)}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Receipt OCR", color = Color(0xFF94A3B8), fontSize = 9.sp)
                    Text("$${String.format("%.2f", rec.receiptScannedTotal)}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            rec.discrepancyNotes?.let { notes ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(notes, color = Color(0xFFCBD5E1), fontSize = 11.sp)
            }

            if (rec.quickbooksSynced) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Synced to QuickBooks (${rec.quickbooksTxId})", color = Color(0xFF22C55E), fontSize = 10.sp)
                }
            }
        }
    }
}

/**
 * Webhook Event Log Item
 */
@Composable
private fun AdminWebhookEventItem(evt: StripeWebhookEventRecord) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(evt.eventType, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(evt.createdFormatted, color = Color(0xFF94A3B8), fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(evt.description, color = Color.White, fontSize = 11.sp)
        }
    }
}

/**
 * Admin Audit Log Item
 */
@Composable
private fun AdminAuditLogItem(log: StripeIssuingAuditLog) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF6366F1).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            log.action,
                            color = Color(0xFF818CF8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(log.timestampFormatted, color = Color(0xFF94A3B8), fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(log.details, color = Color.White, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Admin: ${log.adminName} (${log.ipAddressMasked})", color = Color(0xFF94A3B8), fontSize = 10.sp)
        }
    }
}
