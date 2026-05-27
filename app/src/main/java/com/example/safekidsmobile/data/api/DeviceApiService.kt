package com.example.safekidsmobile.data.api

import com.example.safekidsmobile.data.model.Device
import com.example.safekidsmobile.data.model.DeviceListResponse
import com.example.safekidsmobile.data.model.LinkDeviceRequest
import com.example.safekidsmobile.data.model.LinkDeviceResponse
import com.example.safekidsmobile.data.model.LocationData
import com.example.safekidsmobile.data.model.LocationListResponse
import com.example.safekidsmobile.data.model.UpdateDeviceSettingsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DeviceApiService {
    @GET("devices")
    suspend fun listDevices(): Response<DeviceListResponse>

    @POST("devices/link")
    suspend fun linkDevice(@Body request: LinkDeviceRequest): Response<LinkDeviceResponse>

    @GET("devices/{device_id}")
    suspend fun getDevice(@Path("device_id") deviceId: String): Response<Device>

    @PATCH("devices/{device_id}")
    suspend fun updateDeviceSettings(
        @Path("device_id") deviceId: String,
        @Body request: UpdateDeviceSettingsRequest
    ): Response<Device>

    @DELETE("devices/{device_id}")
    suspend fun unpairDevice(@Path("device_id") deviceId: String): Response<Unit>
}

interface LocationApiService {
    @GET("location/{device_id}/history")
    suspend fun getLocationHistory(
        @Path("device_id") deviceId: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("limit") limit: Int = 200
    ): Response<LocationListResponse>

    @GET("location/{device_id}/current")
    suspend fun getCurrentLocation(@Path("device_id") deviceId: String): Response<LocationData>

    @POST("location/{device_id}/request")
    suspend fun requestLocation(@Path("device_id") deviceId: String): Response<Unit>
}
