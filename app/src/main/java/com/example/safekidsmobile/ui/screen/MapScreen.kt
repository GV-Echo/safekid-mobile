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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.io.File
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.safekidsmobile.data.model.Device
import com.example.safekidsmobile.data.model.LocationData
import com.example.safekidsmobile.ui.theme.AccentGreen
import com.example.safekidsmobile.ui.theme.PrimaryBlue
import com.example.safekidsmobile.ui.viewmodel.DeviceViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem

@Composable
fun MapScreen(
    deviceId: String,
    viewModel: DeviceViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onBack: () -> Unit
) {
    val deviceState by viewModel.deviceState.collectAsState()
    val locationHistoryState by viewModel.locationHistoryState.collectAsState()
    val selectedDevice = deviceState.selectedDevice
    val lastLocation = locationHistoryState.locations.lastOrNull()

    LaunchedEffect(deviceId) {
        viewModel.loadCurrentLocation(deviceId)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9F8F5))
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(PrimaryBlue).padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedDevice?.name ?: "Device Map",
                    fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White
                )
                OutlinedButton(onClick = onBack, modifier = Modifier.height(36.dp)) {
                    Text("Back", color = Color.White)
                }
            }
        }

        if (selectedDevice != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                MapPlaceholder(location = lastLocation, deviceName = selectedDevice.name)

                Spacer(modifier = Modifier.height(16.dp))

                DeviceInfoCard(device = selectedDevice)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var refreshing by remember { mutableStateOf(false) }
                    LaunchedEffect(refreshing) {
                        if (refreshing) {
                            repeat(3) {
                                delay(4000)
                                viewModel.loadCurrentLocation(deviceId)
                            }
                            viewModel.loadDevice(deviceId)
                            refreshing = false
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.requestLocation(deviceId)
                            refreshing = true
                        },
                        enabled = !refreshing,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text(if (refreshing) "..." else "Refresh") }

                    Button(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("History") }

                    Button(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Settings") }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }
    }
}

@Composable
fun MapPlaceholder(location: LocationData?, deviceName: String) {
    if (location != null) {
        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    Configuration.getInstance().apply {
                        userAgentValue = context.packageName
                        osmdroidTileCache = File(context.cacheDir, "osmdroid")
                    }
                    MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        controller.setCenter(GeoPoint(location.lat, location.lng))
                    }
                },
                update = { mapView ->
                    val point = GeoPoint(location.lat, location.lng)
                    mapView.controller.setCenter(point)
                    mapView.overlays.clear()
                    mapView.overlays.add(
                        ItemizedIconOverlay(
                            arrayListOf(OverlayItem(
                                deviceName,
                                "%.4f, %.4f".format(location.lat, location.lng),
                                point
                            )),
                            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                                override fun onItemSingleTapUp(index: Int, item: OverlayItem?) = true
                                override fun onItemLongPress(index: Int, item: OverlayItem?) = false
                            },
                            mapView.context
                        )
                    )
                    mapView.invalidate()
                }
            )
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8E8E8)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📍 Map View", fontSize = 24.sp,
                        fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No location data available", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun DeviceInfoCard(device: Device) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Status", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = if (device.online) "Online" else "Offline",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = if (device.online) AccentGreen else Color.Gray
                )
            }
            Column {
                Text("Battery", fontSize = 12.sp, color = Color.Gray)
                Text("${device.battery}%", fontSize = 16.sp,
                    fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
            Column {
                Text("Heart Rate", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = if (device.heartRate != null) "${device.heartRate} bpm" else "— bpm",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = if (device.heartRate != null) Color(0xFFE53935) else Color.Gray
                )
            }
            Column {
                Text("Last Seen", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = device.lastSeenAt?.substring(11, 16) ?: "—",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue
                )
            }
        }
    }
}
