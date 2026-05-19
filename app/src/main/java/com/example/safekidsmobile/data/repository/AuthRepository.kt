package com.example.safekidsmobile.data.repository

import com.example.safekidsmobile.data.api.AuthApiService
import com.example.safekidsmobile.data.manager.TokenManager
import com.example.safekidsmobile.data.model.AuthResponse
import com.example.safekidsmobile.data.model.LoginRequest
import com.example.safekidsmobile.data.model.RegisterRequest
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val response: AuthResponse) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val request = LoginRequest(email, password)
            val response = authApiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                tokenManager.saveUserInfo(authResponse.id, authResponse.email, authResponse.fullName)
                AuthResult.Success(authResponse)
            } else {
                AuthResult.Error("Login failed: ${response.code()}")
            }
        } catch (e: Exception) {
            AuthResult.Error("Login error: ${e.message}")
        }
    }

    suspend fun register(email: String, password: String, fullName: String): AuthResult {
        return try {
            val request = RegisterRequest(email, password, fullName)
            val response = authApiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                tokenManager.saveUserInfo(authResponse.id, authResponse.email, authResponse.fullName)
                AuthResult.Success(authResponse)
            } else {
                AuthResult.Error("Registration failed: ${response.code()}")
            }
        } catch (e: Exception) {
            AuthResult.Error("Registration error: ${e.message}")
        }
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    fun logout() {
        tokenManager.clearTokens()
    }
}
