package com.example.safekidsmobile.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.safekidsmobile.ui.theme.AccentGreen
import com.example.safekidsmobile.ui.theme.PrimaryBlue
import com.example.safekidsmobile.ui.viewmodel.DeviceViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.util.GeoPoint

@Composable
fun MapScreen(
    deviceId: String,
    viewModel: DeviceViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onBack: () -> Unit
) {
    val deviceState by viewModel.deviceState.collectAsState()
    val selectedDevice = deviceState.selectedDevice

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F8F5))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryBlue)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedDevice?.name ?: "Device Map",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Back", color = Color.White)
                }
            }
        }

        if (selectedDevice != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                // Map Container with Osmdroid
                MapPlaceholder(device = selectedDevice)

                Spacer(modifier = Modifier.height(16.dp))

                // Device Info Card
                DeviceInfoCard(device = selectedDevice)

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToHistory,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("History")
                    }

                    Button(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Settings")
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }
    }
}

@Composable
fun MapPlaceholder(device: com.example.safekidsmobile.data.model.Device) {
    val location = device.last_location

    if (location != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    // Initialize Osmdroid configuration
                    Configuration.getInstance().let {
                        it.userAgentValue = "SafeKid/1.0"
                    }

                    MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)

                        // Set map center to device location
                        controller.setCenter(GeoPoint(location.lat, location.lon))
                        controller.setZoom(15.0)

                        // Add device marker
                        val items = ArrayList<OverlayItem>().apply {
                            add(OverlayItem(
                                "Device Location",
                                "${device.name}\nLat: %.4f, Lon: %.4f".format(location.lat, location.lon),
                                GeoPoint(location.lat, location.lon)
                            ))
                        }

                        // Correctly typed ItemizedIconOverlay with basic gestures implemented
                        val overlay = ItemizedIconOverlay<OverlayItem>(
                            items,
                            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                                    return true
                                }
                                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                                    return true
                                }
                            },
                            context
                        )

                        overlays.add(overlay)
                    }
                },
                update = { mapView ->
                    // Update map when device location changes
                    mapView.controller.setCenter(GeoPoint(location.lat, location.lon))
                }
            )
        }
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8E8E8)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📍 Map View",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "No location data available",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceInfoCard(device: com.example.safekidsmobile.data.model.Device) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Device Status",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = if (device.is_online) "Online" else "Offline",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (device.is_online) AccentGreen else Color.Gray
                    )
                }

                Column {
                    Text(
                        text = "Battery",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${device.battery}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }

                Column {
                    Text(
                        text = "Last Update",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = device.last_update.substring(11, 16),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }
        }
    }
}