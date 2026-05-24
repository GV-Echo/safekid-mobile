package com.example.safekidsmobile.data.api

import com.example.safekidsmobile.data.model.Device
import com.example.safekidsmobile.data.model.DeviceListResponse
import com.example.safekidsmobile.data.model.DeviceSettings
import com.example.safekidsmobile.data.model.LocationData
import com.example.safekidsmobile.data.model.LocationHistoryResponse
import com.example.safekidsmobile.data.model.PairDeviceRequest
import com.example.safekidsmobile.data.model.PairDeviceResponse
import com.example.safekidsmobile.data.model.UpdateDeviceSettingsRequest
import retrofit2.Response

class MockDeviceApiService : DeviceApiService {
    private val mockDevices = listOf(
        Device(
            id = "device_001",
            name = "Max's Watch",
            type = "smartwatch",
            model = "Huami Amazfit",
            is_online = true,
            battery = 82,
            last_update = "2026-05-20T10:30:00Z",
            last_location = LocationData(
                id = "loc_001",
                device_id = "device_001",
                lat = 55.7558,
                lon = 37.6173,
                accuracy = 15,
                timestamp = "2026-05-20T10:30:00Z"
            )
        ),
        Device(
            id = "device_002",
            name = "Sophia's Phone",
            type = "phone",
            model = "Samsung A14",
            is_online = false,
            battery = 45,
            last_update = "2026-05-20T08:15:00Z",
            last_location = LocationData(
                id = "loc_002",
                device_id = "device_002",
                lat = 55.7505,
                lon = 37.6250,
                accuracy = 20,
                timestamp = "2026-05-20T08:15:00Z"
            )
        )
    )

    override suspend fun listDevices(): Response<DeviceListResponse> {
        return Response.success(DeviceListResponse(devices = mockDevices))
    }

    override suspend fun pairDevice(request: PairDeviceRequest): Response<PairDeviceResponse> {
        val response = PairDeviceResponse(
            id = "device_${System.currentTimeMillis()}",
            name = "New Device",
            paired_at = "2026-05-20T10:30:00Z"
        )
        return Response.success(response)
    }

    override suspend fun getDevice(deviceId: String): Response<Device> {
        val device = mockDevices.find { it.id == deviceId } 
            ?: mockDevices.first()
        return Response.success(device)
    }

    override suspend fun updateDeviceSettings(
        deviceId: String,
        request: UpdateDeviceSettingsRequest
    ): Response<Map<String, Boolean>> {
        return Response.success(mapOf("success" to true))
    }

    override suspend fun unpairDevice(deviceId: String): Response<Map<String, Boolean>> {
        return Response.success(mapOf("success" to true))
    }
}

class MockLocationApiService : LocationApiService {
    override suspend fun getLocationHistory(
        deviceId: String,
        startDate: String,
        endDate: String
    ): Response<LocationHistoryResponse> {
        val locations = listOf(
            LocationData(
                id = "loc_001",
                device_id = deviceId,
                lat = 55.7558,
                lon = 37.6173,
                accuracy = 15,
                timestamp = "2026-05-20T10:00:00Z"
            ),
            LocationData(
                id = "loc_002",
                device_id = deviceId,
                lat = 55.7560,
                lon = 37.6180,
                accuracy = 18,
                timestamp = "2026-05-20T10:15:00Z"
            ),
            LocationData(
                id = "loc_003",
                device_id = deviceId,
                lat = 55.7565,
                lon = 37.6190,
                accuracy = 12,
                timestamp = "2026-05-20T10:30:00Z"
            )
        )
        return Response.success(LocationHistoryResponse(locations = locations))
    }

    override suspend fun getCurrentLocation(deviceId: String): Response<LocationData> {
        val location = LocationData(
            id = "loc_current",
            device_id = deviceId,
            lat = 55.7565,
            lon = 37.6190,
            accuracy = 12,
            timestamp = "2026-05-20T10:30:00Z"
        )
        return Response.success(location)
    }
}
