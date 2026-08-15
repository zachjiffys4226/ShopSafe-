package com.example.shopsafe.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DriverShortcutItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val badge: String? = null,
    val color: Color = Color(0xFF38BDF8)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverExpandableMenuSheet(
    onDismiss: () -> Unit,
    onSelectShortcut: (String) -> Unit
) {
    val shortcuts = listOf(
        DriverShortcutItem("MAP", "Live Map", Icons.Default.Map, null, Color(0xFF10B981)),
        DriverShortcutItem("SHOPSAFE_CARD", "ShopSafe Card", Icons.Default.CreditCard, "Stripe", Color(0xFF6366F1)),
        DriverShortcutItem("ORDERS", "Available Orders", Icons.Default.ListAlt, "8 New", Color(0xFF0284C7)),
        DriverShortcutItem("ACTIVE", "Active Trip", Icons.Default.Navigation, null, Color(0xFFF59E0B)),
        DriverShortcutItem("RECEIPTS", "Receipt Scanner", Icons.Default.DocumentScanner, "OCR", Color(0xFF10B981)),
        DriverShortcutItem("STRIPE_ADMIN", "Issuing Admin", Icons.Default.AdminPanelSettings, "Live/Test", Color(0xFFF59E0B)),
        DriverShortcutItem("TAX_CENTER", "Earnings & Taxes", Icons.Default.AccountBalance, "1099", Color(0xFF38BDF8)),
        DriverShortcutItem("MEDIA_VAULT", "Delivery Vault", Icons.Default.Shield, "AES", Color(0xFF8B5CF6)),
        DriverShortcutItem("BIZ_ACCOUNTING", "QuickBooks Sync", Icons.Default.Sync, "QB", Color(0xFF22C55E)),
        DriverShortcutItem("EARNINGS", "Earnings & Payout", Icons.Default.AccountBalanceWallet, "$142.50", Color(0xFF34D399)),
        DriverShortcutItem("MILEAGE", "Mileage Log", Icons.Default.DirectionsCar, "Tax $", Color(0xFF38BDF8)),
        DriverShortcutItem("HISTORY", "Activity History", Icons.Default.History, null, Color(0xFF94A3B8)),
        DriverShortcutItem("MESSAGES", "Driver Messages", Icons.Default.Chat, "2", Color(0xFF60A5FA)),
        DriverShortcutItem("HOTSPOTS", "Demand Zones", Icons.Default.LocalFireDepartment, "🔥 Surge", Color(0xFFEF4444)),
        DriverShortcutItem("SHOP", "Customer App", Icons.Default.Storefront, "Shop", Color(0xFF0284C7)),
        DriverShortcutItem("PERFORMANCE", "Performance & Tier", Icons.Default.Star, "4.98 ★", Color(0xFFFBBF24)),
        DriverShortcutItem("VEHICLES", "My Vehicles", Icons.Default.CarRental, "2 Cars", Color(0xFFA78BFA)),
        DriverShortcutItem("DOCUMENTS", "Compliance Docs", Icons.Default.VerifiedUser, "Checkr ✓", Color(0xFF10B981)),
        DriverShortcutItem("BANKING", "Instant Cashout", Icons.Default.CreditCard, "Stripe", Color(0xFF22C55E)),
        DriverShortcutItem("REFERRALS", "Refer a Driver", Icons.Default.CardGiftcard, "$50", Color(0xFFEC4899)),
        DriverShortcutItem("SUPPORT", "24/7 Support", Icons.Default.SupportAgent, null, Color(0xFF6366F1)),
        DriverShortcutItem("SETTINGS", "Preferences", Icons.Default.Settings, null, Color(0xFF94A3B8))
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF10B981),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ShopSafe Driver Toolkit",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Instant access to all courier operations",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close Menu", tint = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(shortcuts) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .clickable {
                                onSelectShortcut(item.id)
                                onDismiss()
                            }
                            .testTag("driver_shortcut_${item.id.lowercase()}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    color = item.color.copy(alpha = 0.15f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            item.icon,
                                            contentDescription = item.title,
                                            tint = item.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                if (item.badge != null) {
                                    Text(
                                        text = item.badge,
                                        color = item.color,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
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
