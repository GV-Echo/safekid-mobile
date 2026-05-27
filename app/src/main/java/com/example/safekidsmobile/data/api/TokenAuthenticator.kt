package com.example.safekidsmobile.data.api

import com.example.safekidsmobile.data.manager.TokenManager
import com.example.safekidsmobile.data.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiServiceProvider: Provider<AuthApiService>
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        return runBlocking {
            try {
                val result = authApiServiceProvider.get()
                    .refreshToken(RefreshTokenRequest(refreshToken))

                if (result.isSuccessful && result.body() != null) {
                    val body = result.body()!!
                    tokenManager.saveTokens(body.accessToken, body.refreshToken)
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${body.accessToken}")
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
