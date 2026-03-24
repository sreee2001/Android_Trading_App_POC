package com.trading.app.di

import com.trading.app.data.remote.FinnhubApi
import com.trading.app.data.remote.FinnhubWebSocket
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Finnhub API key. In production, this would come from BuildConfig or encrypted storage.
     * Free tier: 60 API calls/minute, WebSocket streaming included.
     *
     * Get your free key at: https://finnhub.io/register
     */
    private const val FINNHUB_API_KEY = "demo" // Replace with real key
    private const val FINNHUB_BASE_URL = "https://finnhub.io/api/v1/"

    @Provides
    @Singleton
    @Named("finnhubApiKey")
    fun provideFinnhubApiKey(): String = FINNHUB_API_KEY

    @Provides
    @Singleton
    fun provideAuthInterceptor(@Named("finnhubApiKey") apiKey: String): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val url = original.url.newBuilder()
                .addQueryParameter("token", apiKey)
                .build()
            chain.proceed(original.newBuilder().url(url).build())
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .pingInterval(30, TimeUnit.SECONDS) // Keep WebSocket alive
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(FINNHUB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideFinnhubApi(retrofit: Retrofit): FinnhubApi {
        return retrofit.create(FinnhubApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFinnhubWebSocket(
        okHttpClient: OkHttpClient,
        @Named("finnhubApiKey") apiKey: String
    ): FinnhubWebSocket {
        return FinnhubWebSocket(okHttpClient, apiKey)
    }
}
