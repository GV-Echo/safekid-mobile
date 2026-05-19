package com.example.safekidsmobile.data.api

import com.example.safekidsmobile.data.model.AuthResponse
import com.example.safekidsmobile.data.model.LoginRequest
import com.example.safekidsmobile.data.model.RefreshTokenRequest
import com.example.safekidsmobile.data.model.RegisterRequest
import retrofit2.Response

class MockAuthApiService : AuthApiService {
    override suspend fun login(request: LoginRequest): Response<AuthResponse> {
        // Simulate successful login
        val response = AuthResponse(
            id = "user_${System.currentTimeMillis()}",
            email = request.email,
            fullName = "Demo User",
            accessToken = "mock_access_token_${System.currentTimeMillis()}",
            refreshToken = "mock_refresh_token_${System.currentTimeMillis()}",
            expiresIn = 3600
        )
        return Response.success(response)
    }

    override suspend fun register(request: RegisterRequest): Response<AuthResponse> {
        // Simulate successful registration
        val response = AuthResponse(
            id = "user_${System.currentTimeMillis()}",
            email = request.email,
            fullName = request.fullName,
            accessToken = "mock_access_token_${System.currentTimeMillis()}",
            refreshToken = "mock_refresh_token_${System.currentTimeMillis()}",
            expiresIn = 3600
        )
        return Response.success(response)
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): Response<AuthResponse> {
        // Simulate token refresh
        val response = AuthResponse(
            id = "user_${System.currentTimeMillis()}",
            email = "user@example.com",
            fullName = "Demo User",
            accessToken = "mock_access_token_${System.currentTimeMillis()}",
            refreshToken = "mock_refresh_token_${System.currentTimeMillis()}",
            expiresIn = 3600
        )
        return Response.success(response)
    }
}
