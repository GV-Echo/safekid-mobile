package com.example.safekidsmobile.data.api

import com.example.safekidsmobile.data.manager.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val accessToken = tokenManager.getAccessToken()

        val newRequest = if (accessToken != null) {
            request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .build()
        } else {
            request.newBuilder()
                .header("Content-Type", "application/json")
                .build()
        }

        return chain.proceed(newRequest)
    }
}
