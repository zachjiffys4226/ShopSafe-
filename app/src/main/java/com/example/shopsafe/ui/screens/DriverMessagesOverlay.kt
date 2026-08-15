package com.example.shopsafe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopsafe.ui.ShopSafeViewModel

data class DriverMessageThread(
    val id: String,
    val senderName: String,
    val role: String, // Customer, Merchant, Dispatcher
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val isSafetySupport: Boolean = false
)

@Composable
fun DriverMessagesOverlay(
    viewModel: ShopSafeViewModel,
    onBackToMap: () -> Unit
) {
    var selectedThread by remember { mutableStateOf<DriverMessageThread?>(null) }
    var chatMessageText by remember { mutableStateOf("") }
    var chatMessages by remember {
        mutableStateOf(
            listOf(
                "Hello Alex, please leave the groceries on the porch bench next to the front door.",
                "Got it! I am arriving in 2 minutes.",
                "Thank you so much! Extra tip added in app."
            )
        )
    }

    val threads = remember {
        listOf(
            DriverMessageThread(
                id = "thread_1",
                senderName = "Sarah Jenkins (Customer)",
                role = "Active Delivery • Walmart Order",
                lastMessage = "Thank you so much! Extra tip added in app.",
                time = "Just now",
                unreadCount = 1
            ),
            DriverMessageThread(
                id = "thread_2",
                senderName = "ShopSafe 24/7 Safety Dispatch",
                role = "Support Agent (Officer Kevin)",
                lastMessage = "All systems operational in SOMA surge corridor.",
                time = "14m ago",
                isSafetySupport = true
            ),
            DriverMessageThread(
                id = "thread_3",
                senderName = "Artisan Bakery & Coffee",
                role = "Merchant Partner",
                lastMessage = "Order #409 is packaged and waiting at pickup counter.",
                time = "1h ago"
            )
        )
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            Surface(
                color = Color(0xFF1E293B),
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
                        Surface(
                            color = Color(0xFF38BDF8),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (selectedThread != null) selectedThread!!.senderName else "Driver Messages & Chat",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = if (selectedThread != null) selectedThread!!.role else "Encrypted in-app courier communication",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Persistent "BACK TO MAP" Button
                    Button(
                        onClick = onBackToMap,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("messages_back_to_map_button")
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BACK TO MAP", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        if (selectedThread == null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(threads) { thread ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedThread = thread },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (thread.isSafetySupport) Color(0xFF2563EB) else Color(0xFF0284C7),
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (thread.isSafetySupport) Icons.Default.SupportAgent else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(thread.senderName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(thread.time, color = Color(0xFF64748B), fontSize = 10.sp)
                                }
                                Text(thread.role, color = Color(0xFF38BDF8), fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(thread.lastMessage, color = Color(0xFF94A3B8), fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        } else {
            // Live Chat View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(chatMessages) { msg ->
                        val isDriver = msg.contains("Alex", ignoreCase = true) || msg.contains("arriving", ignoreCase = true)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isDriver) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                color = if (isDriver) Color(0xFF0284C7) else Color(0xFF1E293B),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = msg,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                // Input Bar
                Surface(
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatMessageText,
                            onValueChange = { chatMessageText = it },
                            placeholder = { Text("Type message to customer...", color = Color(0xFF64748B), fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (chatMessageText.isNotBlank()) {
                                    chatMessages = chatMessages + chatMessageText
                                    chatMessageText = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
