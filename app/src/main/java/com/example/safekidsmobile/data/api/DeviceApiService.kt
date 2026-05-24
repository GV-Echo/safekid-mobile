package com.example.safekidsmobile.data.api

import com.example.safekidsmobile.data.model.DeviceListResponse
import com.example.safekidsmobile.data.model.LocationHistoryResponse
import com.example.safekidsmobile.data.model.PairDeviceRequest
import com.example.safekidsmobile.data.model.PairDeviceResponse
import com.example.safekidsmobile.data.model.UpdateDeviceSettingsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DeviceApiService {
    @GET("devices")
    suspend fun listDevices(): Response<DeviceListResponse>

    @POST("devices/pair")
    suspend fun pairDevice(@Body request: PairDeviceRequest): Response<PairDeviceResponse>

    @GET("devices/{device_id}")
    suspend fun getDevice(@Path("device_id") deviceId: String): Response<com.example.safekidsmobile.data.model.Device>

    @PUT("devices/{device_id}/settings")
    suspend fun updateDeviceSettings(
        @Path("device_id") deviceId: String,
        @Body request: UpdateDeviceSettingsRequest
    ): Response<Map<String, Boolean>>

    @DELETE("devices/{device_id}")
    suspend fun unpairDevice(@Path("device_id") deviceId: String): Response<Map<String, Boolean>>
}

interface LocationApiService {
    @GET("locations")
    suspend fun getLocationHistory(
        @Query("device_id") deviceId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Response<LocationHistoryResponse>

    @GET("devices/{device_id}/location")
    suspend fun getCurrentLocation(@Path("device_id") deviceId: String): Response<com.example.safekidsmobile.data.model.LocationData>
}
