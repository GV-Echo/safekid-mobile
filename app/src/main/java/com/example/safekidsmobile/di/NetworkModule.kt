package com.example.safekidsmobile.di

import android.content.Context
import com.example.safekidsmobile.data.api.AuthApiService
import com.example.safekidsmobile.data.api.AuthInterceptor
import com.example.safekidsmobile.data.api.DeviceApiService
import com.example.safekidsmobile.data.api.LocationApiService
import com.example.safekidsmobile.data.api.MockAuthApiService
import com.example.safekidsmobile.data.api.MockDeviceApiService
import com.example.safekidsmobile.data.api.MockLocationApiService
import com.example.safekidsmobile.data.api.TokenAuthenticator
import com.example.safekidsmobile.data.manager.TokenManager
import com.example.safekidsmobile.data.websocket.WebSocketLocationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://localhost:8000/api/"

    @Provides
    @Singleton
    fun provideTokenManager(
        @ApplicationContext context: Context
    ): TokenManager = TokenManager(context)

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenManager: TokenManager,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return MockAuthApiService()
    }

    @Provides
    @Singleton
    fun provideDeviceApiService(retrofit: Retrofit): DeviceApiService {
        return MockDeviceApiService()
    }

    @Provides
    @Singleton
    fun provideLocationApiService(retrofit: Retrofit): LocationApiService {
        return MockLocationApiService()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenManager: TokenManager,
        // Просим Hilt передать Provider вместо готового AuthApiService
        authApiServiceProvider: javax.inject.Provider<AuthApiService>
    ): TokenAuthenticator {
        return TokenAuthenticator(tokenManager, authApiServiceProvider)
    }
}