package com.example.shopsafe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopsafe.data.models.Order
import com.example.shopsafe.data.repository.AiOrderDispatchAnalysis
import com.example.shopsafe.ui.ShopSafeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiOrderDispatchSheet(
    viewModel: ShopSafeViewModel,
    order: Order?,
    analysis: AiOrderDispatchAnalysis?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onAcceptAndStart: (Order, AiOrderDispatchAnalysis) -> Unit
) {
    if (order == null) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 44.dp, height = 4.dp),
                shape = CircleShape,
                color = Color(0xFF475569)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: AI Intelligence Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "Gemini AI",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "GEMINI AI ORDER DISPATCH",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Real Customer Order #${order.id.takeLast(6).uppercase()}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "REAL ORDER",
                        color = Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real Order Summary Card
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MERCHANT / STORE",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = order.storeOrSellerName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ORDER VALUE",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$${String.format(java.util.Locale.US, "%.2f", order.total)}",
                                color = Color(0xFF38BDF8),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Items
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Items to deliver:",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = order.itemsSummary,
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Addresses
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Drop-off Destination:",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = order.dropoffAddress.ifBlank { "123 Main St, San Francisco, CA" },
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Analysis Section
            if (isLoading) {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Gemini AI Analyzing Real Order Route & Payout...",
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Evaluating transit safety, driver match score, and handling parameters",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            } else if (analysis != null) {
                // AI Result Card
                Surface(
                    color = Color(0xFF0C1938),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎯", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI Match Score",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${analysis.matchScore}% Match",
                                    color = Color(0xFF34D399),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Suggested Payout & Estimated Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "SUGGESTED PAYOUT",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$${String.format(java.util.Locale.US, "%.2f", analysis.suggestedPayout)}",
                                    color = Color(0xFF10B981),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "ESTIMATED TRANSIT",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${analysis.estimatedTransitMins} mins",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Risk & Special Handling
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Handling Tier: ${analysis.riskLevel}",
                                color = Color(0xFFF59E0B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "💡 Instructions: ${analysis.handlingInstructions}",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "🤖 Gemini Dispatch Rationale: ${analysis.aiReasoningSummary}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8))
                ) {
                    Text("Dismiss", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        if (analysis != null) {
                            onAcceptAndStart(order, analysis)
                        } else {
                            viewModel.dispatchRealOrderWithAi(order)
                        }
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(50.dp)
                        .testTag("accept_ai_dispatched_order_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Accept Real Order", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
