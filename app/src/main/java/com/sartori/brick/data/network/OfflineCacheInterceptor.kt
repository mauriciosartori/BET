package com.sartori.brick.data.network

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OfflineCacheInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (networkError: IOException) {
            val cachedRequest = chain.request()
                .newBuilder()
                .cacheControl(
                    CacheControl.Builder()
                        .onlyIfCached()
                        .maxStale(CACHE_MAX_STALE_DAYS, TimeUnit.DAYS)
                        .build()
                )
                .build()
            
            chain.proceed(cachedRequest)
        }
    }

    private companion object {
        const val CACHE_MAX_STALE_DAYS = 7
    }
}
