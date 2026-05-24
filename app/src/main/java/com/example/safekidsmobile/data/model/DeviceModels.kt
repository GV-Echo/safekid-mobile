package com.example.safekidsmobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: String,
    val name: String,
    val type: String,  // "smartwatch", "phone", etc.
    val model: String,
    val is_online: Boolean,
    val battery: Int,  // 0-100
    val last_update: String,  // ISO 8601
    val last_location: LocationData?
)

@Serializable
data class LocationData(
    val id: String,
    val device_id: String,
    val lat: Double,
    val lon: Double,
    val accuracy: Int,  // meters
    val timestamp: String  // ISO 8601
)

@Serializable
data class DeviceSettings(
    @SerialName("location_interval_min")
    val locationIntervalMin: Int,
    val notifications_enabled: Boolean
)

@Serializable
data class PairDeviceRequest(
    val pin: String
)

@Serializable
data class PairDeviceResponse(
    val id: String,
    val name: String,
    val paired_at: String
)

@Serializable
data class LocationHistoryRequest(
    val device_id: String,
    val start_date: String,
    val end_date: String
)

@Serializable
data class LocationHistoryResponse(
    val locations: List<LocationData>
)

@Serializable
data class UpdateDeviceSettingsRequest(
    @SerialName("location_interval_min")
    val locationIntervalMin: Int
)

@Serializable
data class DeviceListResponse(
    val devices: List<Device>
)

// WebSocket message types
@Serializable
data class WebSocketMessage(
    val type: String,
    val device_id: String? = null,
    val data: LocationData? = null
)
