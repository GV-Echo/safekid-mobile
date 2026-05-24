package com.example.safekidsmobile.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safekidsmobile.data.model.Device
import com.example.safekidsmobile.ui.theme.AccentGreen
import com.example.safekidsmobile.ui.theme.PrimaryBlue
import com.example.safekidsmobile.ui.viewmodel.DeviceViewModel
import androidx.compose.foundation.layout.width

@Composable
fun DevicesScreen(
    viewModel: DeviceViewModel,
    onDeviceSelected: (Device) -> Unit,
    onPairDevice: () -> Unit,
    onBack: () -> Unit
) {
    val deviceState by viewModel.deviceState.collectAsState()
    var showPairDialog by remember { mutableStateOf(false) }

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
            Text(
                text = "Devices",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }

        // Content
        if (deviceState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (deviceState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Error",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = deviceState.error ?: "Unknown error",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadDevices() }) {
                        Text("Retry")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(deviceState.devices) { device ->
                    DeviceCard(
                        device = device,
                        onClick = { 
                            viewModel.selectDevice(device)
                            onDeviceSelected(device)
                        },
                        onUnpair = { viewModel.unpairDevice(device.id) }
                    )
                }
            }
        }

        // Pair Button
        Button(
            onClick = { showPairDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Pair New Device", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showPairDialog) {
        PairDeviceDialog(
            onDismiss = { showPairDialog = false },
            onPair = { 
                showPairDialog = false
                onPairDevice()
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun DeviceCard(
    device: Device,
    onClick: () -> Unit,
    onUnpair: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = device.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Text(
                        text = device.model,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                // Status Badge
                val statusColor = if (device.is_online) AccentGreen else Color.Gray
                val statusText = if (device.is_online) "Online" else "Offline"
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Battery
                Column {
                    Text(
                        text = "Battery",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${device.battery}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }

                // Last Update
                Column {
                    Text(
                        text = "Last Update",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = device.last_update.split("T")[0],
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }

                // Unpair Button
                OutlinedButton(
                    onClick = onUnpair,
                    modifier = Modifier
                        .height(36.dp)
                ) {
                    Text("Unpair", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PairDeviceDialog(
    onDismiss: () -> Unit,
    onPair: () -> Unit,
    viewModel: DeviceViewModel
) {
    val pairingState by viewModel.pairingState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pair New Device",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Enter the PIN from your device",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PIN Input Field
                PinInputField(
                    value = pairingState.pinInput,
                    onValueChange = { viewModel.updatePinInput(it) }
                )

                if (pairingState.error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = pairingState.error ?: "Error",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            viewModel.pairDevice(pairingState.pinInput)
                            onPair()
                        },
                        enabled = pairingState.pinInput.length >= 4 && !pairingState.isPairing,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        if (pairingState.isPairing) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(end = 8.dp)
                            )
                        }
                        Text("Pair")
                    }
                }
            }
        }
    }
}

@Composable
fun PinInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(6) { index ->
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFE8E8E8), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (index < value.length) value[index].toString() else "•",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (index < value.length) PrimaryBlue else Color.Gray
                )
            }
        }
    }
}

// Size extension for Modifier
fun Modifier.size(size: androidx.compose.ui.unit.Dp) = this
    .width(size)
    .height(size)


