package com.example.safekidsmobile.data.repository

import com.example.safekidsmobile.data.api.DeviceApiService
import com.example.safekidsmobile.data.api.LocationApiService
import com.example.safekidsmobile.data.model.Device
import com.example.safekidsmobile.data.model.LocationData
import com.example.safekidsmobile.data.model.PairDeviceRequest
import com.example.safekidsmobile.data.model.PairDeviceResponse
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
            val devices = response.body()?.devices ?: emptyList()
            DeviceResult.Success(devices)
        } else {
            DeviceResult.Error(Exception("Failed to fetch devices: ${response.code()}"))
        }
    } catch (e: Exception) {
        DeviceResult.Error(e)
    }

    suspend fun pairDevice(pin: String): DeviceResult<PairDeviceResponse> {
        return try {
            if (pin.length < 4) {
                return DeviceResult.Error(Exception("PIN must be at least 4 characters"))
            }
            
            val response = deviceApiService.pairDevice(PairDeviceRequest(pin))
            if (response.isSuccessful) {
                response.body()?.let {
                    DeviceResult.Success(it)
                } ?: DeviceResult.Error(Exception("Empty response body"))
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
            response.body()?.let {
                DeviceResult.Success(it)
            } ?: DeviceResult.Error(Exception("Empty response body"))
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
        return try {
            if (locationIntervalMin < 1 || locationIntervalMin > 1440) {
                return DeviceResult.Error(Exception("Location interval must be between 1 and 1440 minutes"))
            }
            
            val response = deviceApiService.updateDeviceSettings(
                deviceId,
                UpdateDeviceSettingsRequest(locationIntervalMin)
            )
            if (response.isSuccessful) {
                DeviceResult.Success(true)
            } else {
                DeviceResult.Error(Exception("Failed to update settings: ${response.code()}"))
            }
        } catch (e: Exception) {
            DeviceResult.Error(e)
        }
    }

    suspend fun unpairDevice(deviceId: String): DeviceResult<Boolean> = try {
        val response = deviceApiService.unpairDevice(deviceId)
        if (response.isSuccessful) {
            DeviceResult.Success(true)
        } else {
            DeviceResult.Error(Exception("Failed to unpair device: ${response.code()}"))
        }
    } catch (e: Exception) {
        DeviceResult.Error(e)
    }

    suspend fun getLocationHistory(
        deviceId: String,
        startDate: String,
        endDate: String
    ): DeviceResult<List<LocationData>> = try {
        val response = locationApiService.getLocationHistory(deviceId, startDate, endDate)
        if (response.isSuccessful) {
            val locations = response.body()?.locations ?: emptyList()
            DeviceResult.Success(locations)
        } else {
            DeviceResult.Error(Exception("Failed to fetch location history: ${response.code()}"))
        }
    } catch (e: Exception) {
        DeviceResult.Error(e)
    }

    suspend fun getCurrentLocation(deviceId: String): DeviceResult<LocationData> = try {
        val response = locationApiService.getCurrentLocation(deviceId)
        if (response.isSuccessful) {
            response.body()?.let {
                DeviceResult.Success(it)
            } ?: DeviceResult.Error(Exception("Empty response body"))
        } else {
            DeviceResult.Error(Exception("Failed to fetch location: ${response.code()}"))
        }
    } catch (e: Exception) {
        DeviceResult.Error(e)
    }
}
