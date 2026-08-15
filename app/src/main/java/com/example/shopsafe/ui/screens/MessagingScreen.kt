package com.example.shopsafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopsafe.data.models.ChatMessage
import com.example.shopsafe.data.models.ChatThread
import com.example.shopsafe.ui.ShopSafeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingScreen(
    viewModel: ShopSafeViewModel
) {
    val threads by viewModel.chatThreads.collectAsState()
    val selectedThreadId by viewModel.selectedThreadId.collectAsState()
    val messages by viewModel.currentThreadMessages.collectAsState()
    val inputText by viewModel.messageInputText.collectAsState()

    val currentThread = threads.find { it.threadId == selectedThreadId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (currentThread != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = currentThread.partnerName.take(1),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(currentThread.partnerName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(currentThread.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        Text("ShopSafe Instant Messages", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    if (currentThread != null) {
                        // Instant Phone Call Action
                        IconButton(
                            onClick = {
                                viewModel.startCall(currentThread.partnerName, currentThread.partnerPhone)
                            }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF16A34A),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call Contact", tint = Color.White, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Chat List Pane (Side list)
            Column(
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "Conversations",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyColumn {
                    items(threads) { thread ->
                        val isSelected = thread.threadId == selectedThreadId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectedThreadId.value = thread.threadId },
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = thread.partnerName,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                                Text(
                                    text = thread.lastMessage,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }

            Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color.LightGray.copy(alpha = 0.5f))

            // Active Chat Conversation Pane
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val listState = rememberLazyListState()
                val scope = rememberCoroutineScope()

                // Messages list
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        ChatMessageBubble(msg = msg)
                    }
                }

                // Quick Prompt Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickPrompts = listOf("Is this available?", "On my way!", "Pick up location?")
                    quickPrompts.forEach { q ->
                        SuggestionChip(
                            onClick = {
                                viewModel.messageInputText.value = q
                                viewModel.sendMessage()
                            },
                            label = { Text(q, fontSize = 11.sp) }
                        )
                    }
                }

                // Action Buttons Row (Image & Location)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            viewModel.sendMessage(
                                imageUrl = "https://images.unsplash.com/photo-1526947425960-945c6e72858f?q=80&w=200&auto=format&fit=crop"
                            )
                            scope.launch {
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Send Photo", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Photo", fontSize = 12.sp)
                    }
                    FilledTonalButton(
                        onClick = {
                            viewModel.sendMessage(
                                lat = 37.7749,
                                lng = -122.4194,
                                locationAddress = "Market St & 5th St"
                            )
                            scope.launch {
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Send Location", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Location", fontSize = 12.sp)
                    }
                }

                // Text Input Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { viewModel.messageInputText.value = it },
                            placeholder = { Text("Type message...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                viewModel.sendMessage()
                                scope.launch {
                                    if (messages.isNotEmpty()) {
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF0284C7), CircleShape)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(msg: ChatMessage) {
    val isUser = msg.isFromUser
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            color = if (isUser) Color(0xFF0284C7) else Color(0xFFE2E8F0),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 16.dp
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = msg.senderName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) Color.White.copy(alpha = 0.8f) else Color.DarkGray
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (msg.imageUrl != null) {
                    coil.compose.AsyncImage(
                        model = msg.imageUrl,
                        contentDescription = "Shared Image",
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                if (msg.lat != null && msg.lng != null) {
                    Surface(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Location Shared", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            if (msg.locationAddress != null) {
                                Text(text = msg.locationAddress, fontSize = 10.sp, color = Color.DarkGray)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                if (msg.text.isNotBlank()) {
                    Text(
                        text = msg.text,
                        fontSize = 14.sp,
                        color = if (isUser) Color.White else Color.Black
                    )
                }
            }
        }
    }
}
