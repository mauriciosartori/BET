package com.sartori.brick.data.earthquake.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface EarthquakeApi {
    @GET("earthquakes/feed/v1.0/summary/all_day.geojson")
    suspend fun getEarthquakes(
        @Header("Cache-Control") cacheControl: String? = null
    ): Response<EarthquakeResponseDto>
}
