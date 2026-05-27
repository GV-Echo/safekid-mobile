package com.example.safekidsmobile.data.repository

import com.example.safekidsmobile.data.api.DeviceApiService
import com.example.safekidsmobile.data.api.LocationApiService
import com.example.safekidsmobile.data.model.Device
import com.example.safekidsmobile.data.model.LinkDeviceRequest
import com.example.safekidsmobile.data.model.LinkDeviceResponse
import com.example.safekidsmobile.data.model.LocationData
import com.example.safekidsmobile.data.model.UpdateDeviceSettingsRequest
import javax.inject.Inject

sealed class DeviceResult<out T> {
    data class Success<T>(val data: T) : DeviceResult<T>()
    data class Error(val exception: Exception) : DeviceResult<Nothing>()
    object Loading : DeviceResult<Nothing>()
}

class DeviceRepository @Inject constructor(
    private val deviceApiService: DeviceApiService,
    private val locationApiService: LocationApiService
) {
    suspend fun listDevices(): DeviceResult<List<Device>> = try {
        val response = deviceApiService.listDevices()
        if (response.isSuccessful) {
            DeviceResult.Success(response.body()?.devices ?: emptyList())
        } else {
            DeviceResult.Error(Exception("Failed to fetch devices: ${response.code()}"))
        }
    } catch (e: Exception) {
        DeviceResult.Error(e)
    }

    suspend fun linkDevice(pin: String, name: String): DeviceResult<LinkDeviceResponse> {
        if (pin.length < 4) return DeviceResult.Error(Exception("PIN must be at least 4 characters"))
        if (name.isBlank()) return DeviceResult.Error(Exception("Device name is required"))
        return try {
            val response = deviceApiService.linkDevice(LinkDeviceRequest(pin, name))
            if (response.isSuccessful) {
                response.body()?.let { DeviceResult.Success(it) }
                    ?: DeviceResult.Error(Exception("Empty response body"))
            } else {
                DeviceResult.Error(Exception("Pairing failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            DeviceResult.Error(e)
        }
    }

    suspend fun getDevice(deviceId: String): DeviceResult<Device> = try {
        val response = deviceApiService.getDevice(deviceId)
        if (response.isSuccessful) {
            response.body()?.let { DeviceResult.Success(it) }
                ?: DeviceResult.Error(Exception("Empty response body"))
        } else {
            DeviceResult.Error(Exception("Failed to fetch device: ${response.code()}"))
        }
    } catch (e: Exception) {
        DeviceResult.Error(e)
    }

    suspend fun updateDeviceSettings(
        deviceId: String,
        locationIntervalMin: Int
    ): DeviceResult<Boolean> {
        if (locationIntervalMin < 5 || locationIntervalMin > 60) {
            return DeviceResult.Error(Exception("Location interval must be between 5 and 60 minutes"))
        }
        return try {
            val response = deviceApiService.updateDeviceSettings(
                deviceId,
                UpdateDeviceSettingsRequest(locationIntervalMin = locationIntervalMin)
            )
            if (response.isSuccessful) DeviceResult.Success(true)
            else DeviceResult.Error(Exception("Failed to update settings: ${response.code()}"))
        } catch (e: Exception) {
            DeviceResult.Error(e)
        }
    }

    suspend fun unpairDevice(deviceId: String): DeviceResult<Boolean> = try {
        val response = deviceApiService.unpairDevice(deviceId)
        if (response.isSuccessful) DeviceResult.Success(true)
        else DeviceResult.Error(Exception("Failed to unpair device: ${response.code()}"))
    } catch (e: Exception) {
        DeviceResult.Error(e)
    }

    suspend fun getLocationHistory(
        deviceId: String,
        from: String,
        to: String
    ): DeviceResult<List<LocationData>> = try {
        val response = locationApiService.getLocationHistory(deviceId, from, to)
        if (response.isSuccessful) {
            DeviceResult.Success(response.body()?.locations ?: emptyList())
        } else {
            DeviceResult.Error(Exception("Failed to fetch location history: ${response.code()}"))
        }
    } catch (e: Exception) {
        DeviceResult.Error(e)
    }

    suspend fun getCurrentLocation(deviceId: String): DeviceResult<LocationData> = try {
        val response = locationApiService.getCurrentLocation(deviceId)
        if (response.isSuccessful) {
            response.body()?.let { DeviceResult.Success(it) }
                ?: DeviceResult.Error(Exception("No location available"))
        } else {
            DeviceResult.Error(Exception("Failed to fetch location: ${response.code()}"))
        }
    } catch (e: Exception) {
        DeviceResult.Error(e)
    }

    suspend fun requestLocation(deviceId: String): DeviceResult<Boolean> = try {
        val response = locationApiService.requestLocation(deviceId)
        if (response.isSuccessful) DeviceResult.Success(true)
        else DeviceResult.Error(Exception("Request failed: ${response.code()}"))
    } catch (e: Exception) {
        DeviceResult.Error(e)
    }
}
