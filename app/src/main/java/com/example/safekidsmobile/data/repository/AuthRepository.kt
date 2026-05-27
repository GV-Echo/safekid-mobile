package com.example.safekidsmobile.data.repository

import com.example.safekidsmobile.data.api.AuthApiService
import com.example.safekidsmobile.data.manager.TokenManager
import com.example.safekidsmobile.data.model.LoginRequest
import com.example.safekidsmobile.data.model.RegisterRequest
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response = authApiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveTokens(body.accessToken, body.refreshToken)
                AuthResult.Success
            } else {
                AuthResult.Error("Login failed: ${response.code()}")
            }
        } catch (e: Exception) {
            AuthResult.Error("Login error: ${e.message}")
        }
    }

    suspend fun register(email: String, password: String, name: String): AuthResult {
        return try {
            val response = authApiService.register(RegisterRequest(email, password, name))
            if (response.isSuccessful) {
                login(email, password)
            } else {
                AuthResult.Error("Registration failed: ${response.code()}")
            }
        } catch (e: Exception) {
            AuthResult.Error("Registration error: ${e.message}")
        }
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    fun logout() = tokenManager.clearTokens()
}
