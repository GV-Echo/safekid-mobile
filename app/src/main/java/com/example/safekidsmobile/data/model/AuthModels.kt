package com.example.safekidsmobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

@Serializable
data class LoginResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Int = 3600
)

@Serializable
data class RegisterResponse(
    val id: String,
    val email: String,
    val name: String
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String? = null,
    val code: Int? = null
)
