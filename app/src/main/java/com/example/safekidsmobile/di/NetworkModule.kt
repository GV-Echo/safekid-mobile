package com.example.safekidsmobile.di

import android.content.Context
import com.example.safekidsmobile.data.api.AuthApiService
import com.example.safekidsmobile.data.api.AuthInterceptor
import com.example.safekidsmobile.data.api.DeviceApiService
import com.example.safekidsmobile.data.api.LocationApiService
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

    private const val BASE_URL = "http://10.0.31.15:8080/"

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager =
        TokenManager(context)

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(httpLoggingInterceptor)
        .authenticator(tokenAuthenticator)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideDeviceApiService(retrofit: Retrofit): DeviceApiService =
        retrofit.create(DeviceApiService::class.java)

    @Provides
    @Singleton
    fun provideLocationApiService(retrofit: Retrofit): LocationApiService =
        retrofit.create(LocationApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor =
        AuthInterceptor(tokenManager)

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenManager: TokenManager,
        authApiServiceProvider: javax.inject.Provider<AuthApiService>
    ): TokenAuthenticator = TokenAuthenticator(tokenManager, authApiServiceProvider)
}
