package com.example.shopsafe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopsafe.ui.ActiveCallState

@Composable
fun InAppCallScreen(
    callState: ActiveCallState,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onEndCall: () -> Unit
) {
    AnimatedVisibility(
        visible = callState.isCalling,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White) // Clean white safety calling canvas
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp, bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Safe Phone Call",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier
                                .padding(12.dp)
                                .size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ShopSafe Encrypted Call",
                        color = Color(0xFF475569),
                        fontSize = 14.sp
                    )
                }

                // Call Partner Profile
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        color = Color(0xFF0284C7),
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = callState.callerName.take(1).ifBlank { "S" },
                                color = Color.White,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = callState.callerName.ifBlank { "ShopSafe Contact" },
                        color = Color(0xFF0F172A),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = callState.callerPhone.ifBlank { "(555) 234-5678" },
                        color = Color(0xFF334155),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val mins = callState.durationSeconds / 60
                    val secs = callState.durationSeconds % 60
                    val durationStr = String.format("%02d:%02d", mins, secs)
                    Text(
                        text = "Connected • $durationStr (HD Voice)",
                        color = Color(0xFF16A34A),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Controls
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Mute
                        IconButton(
                            onClick = onMuteToggle,
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    if (callState.isMuted) Color(0xFFDC2626) else Color(0xFF334155),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Speaker
                        IconButton(
                            onClick = onSpeakerToggle,
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    if (callState.isSpeaker) Color(0xFF0284C7) else Color(0xFF334155),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (callState.isSpeaker) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = "Speaker",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Keypad
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFF334155), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dialpad,
                                contentDescription = "Keypad",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // End Call
                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFFEF4444), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}
