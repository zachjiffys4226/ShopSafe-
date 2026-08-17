package com.example.shopsafe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopsafe.data.models.DemandHeatmapEngine
import com.example.shopsafe.data.models.DemandHotspot
import com.example.shopsafe.ui.ShopSafeViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDemandHeatmapModal(
    viewModel: ShopSafeViewModel,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val orders by viewModel.orders.collectAsState()
    val firestoreOrders by viewModel.firestoreOrders.collectAsState()
    val hotspots = remember(orders, firestoreOrders) { 
        DemandHeatmapEngine.generateHotspotsFromOrders(firestoreOrders + orders) 
    }
    var selectedHotspot by remember { mutableStateOf<DemandHotspot?>(hotspots.firstOrNull()) }

    val defaultCenter = LatLng(37.7749, -122.4194) // San Francisco Center
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 12.5f)
    }

    LaunchedEffect(selectedHotspot) {
        selectedHotspot?.let { hotspot ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(hotspot.latLng, 14.2f),
                durationMs = 800
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Advanced Custom Google Maps with Integrated Real-Time Surge Overlays & Toggles
            com.example.shopsafe.ui.components.GoogleMapView(
                modifier = Modifier.fillMaxSize(),
                showDriverMarker = false,
                showTurnByTurnHud = false,
                showRouteOptions = false,
                showSurgeOverlay = true,
                initialSelectedSurgeZoneId = selectedHotspot?.id ?: "hotspot_1",
                onSurgeZoneSelected = { hotspot -> selectedHotspot = hotspot }
            )

            // Top Header Bar
            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.92f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFDC2626),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = "High Demand Indicator Icon", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Live Order Demand Heatmap", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            Text("Position in high-surge zones for priority dispatch", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .background(Color(0xFF334155), CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            // Bottom Hotspot Carousel Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF0F172A).copy(alpha = 0.95f), Color(0xFF0F172A))
                        )
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "🔥 Top Demand Hotspots & Surge Multipliers",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(hotspots) { hotspot ->
                        val isSelected = selectedHotspot?.id == hotspot.id
                        Surface(
                            onClick = { selectedHotspot = hotspot },
                            color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.width(220.dp),
                            shadowElevation = if (isSelected) 6.dp else 2.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (isSelected) Color.White.copy(alpha = 0.2f) else Color(0xFFDC2626).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${hotspot.surgeMultiplier}x SURGE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) Color.White else Color(0xFFF87171),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "~${hotspot.orderVolumePerHour} orders/hr",
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF94A3B8)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = hotspot.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = hotspot.dominantCategory,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF64748B),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
