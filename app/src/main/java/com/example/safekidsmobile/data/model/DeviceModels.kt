package com.example.safekidsmobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: String,
    val name: String,
    @SerialName("parent_id") val parentId: String = "",
    val battery: Int = 0,
    @SerialName("heart_rate") val heartRate: Int? = null,
    val online: Boolean = false,
    @SerialName("last_seen_at") val lastSeenAt: String? = null,
    @SerialName("location_interval_min") val locationIntervalMin: Int = 30,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class LocationData(
    val id: Long = 0,
    val lat: Double,
    val lng: Double,
    val accuracy: Float? = null,
    val speed: Float? = null,
    val bearing: Float? = null,
    val source: String = "",
    @SerialName("recorded_at") val recordedAt: String = ""
)

@Serializable
data class LinkDeviceRequest(
    val pin: String,
    val name: String
)

@Serializable
data class LinkDeviceResponse(
    @SerialName("device_id") val deviceId: String,
    @SerialName("device_token") val deviceToken: String
)

@Serializable
data class DeviceListResponse(
    val devices: List<Device>,
    val total: Int = 0
)

@Serializable
data class LocationListResponse(
    val locations: List<LocationData>
)

@Serializable
data class UpdateDeviceSettingsRequest(
    @SerialName("location_interval_min") val locationIntervalMin: Int? = null,
    val name: String? = null
)

@Serializable
data class WebSocketMessage(
    val type: String,
    val data: LocationData? = null
)
