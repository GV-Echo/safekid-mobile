package com.example.safekidsmobile.data.api

import com.example.safekidsmobile.data.manager.TokenManager
import com.example.safekidsmobile.data.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider // Добавили импорт провайдера

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    // Оборачиваем в Provider, чтобы разорвать циклическую зависимость Hilt
    private val authApiServiceProvider: Provider<AuthApiService>
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        return runBlocking {
            try {
                val refreshRequest = RefreshTokenRequest(refreshToken)

                // Получаем экземпляр сервиса прямо перед использованием
                val authApiService = authApiServiceProvider.get()
                val result = authApiService.refreshToken(refreshRequest)

                if (result.isSuccessful && result.body() != null) {
                    val authResponse = result.body()!!
                    tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)

                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${authResponse.accessToken}")
                        .build()
                } else {
                    tokenManager.clearTokens()
                    null
                }
            } catch (e: Exception) {
                tokenManager.clearTokens()
                null
            }
        }
    }
}