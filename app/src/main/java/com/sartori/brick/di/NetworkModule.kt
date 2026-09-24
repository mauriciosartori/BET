package com.sartori.brick.di

import android.content.Context
import com.sartori.brick.data.earthquake.network.EarthquakeApi
import com.sartori.brick.data.network.OfflineCacheInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideHttpCache(@ApplicationContext context: Context): Cache = Cache(
        directory = File(context.cacheDir, HTTP_CACHE_DIRECTORY),
        maxSize = HTTP_CACHE_SIZE_BYTES
    )

    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache,
        offlineCacheInterceptor: OfflineCacheInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(offlineCacheInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE.toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideEarthquakeApi(retrofit: Retrofit): EarthquakeApi =
        retrofit.create(EarthquakeApi::class.java)

    private const val BASE_URL = "https://earthquake.usgs.gov/"
    private const val JSON_MEDIA_TYPE = "application/json"
    private const val HTTP_CACHE_DIRECTORY = "http_cache"
    private const val HTTP_CACHE_SIZE_BYTES = 10L * 1024L * 1024L // 10 MB
}
