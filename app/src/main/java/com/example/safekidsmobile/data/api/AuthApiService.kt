package com.example.safekidsmobile.data.api

import com.example.safekidsmobile.data.model.AuthResponse
import com.example.safekidsmobile.data.model.LoginRequest
import com.example.safekidsmobile.data.model.RefreshTokenRequest
import com.example.safekidsmobile.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>
}
