package com.sartori.brick.data.earthquake

import com.sartori.brick.data.earthquake.network.EarthquakeApi
import com.sartori.brick.data.earthquake.network.EarthquakeFeatureDto
import java.io.IOException
import javax.inject.Inject

class DefaultEarthquakeRepository @Inject constructor(
    private val api: EarthquakeApi
) : EarthquakeRepository {
    override suspend fun getEarthquakes(forceRefresh: Boolean): EarthquakeFeed {
        val cacheControl = if (forceRefresh) FORCE_REFRESH_CACHE_CONTROL else null
        val response = api.getEarthquakes(cacheControl)

        if (!response.isSuccessful) {
            throw IOException("USGS request failed with HTTP ${response.code()}")
        }

        val responseBody = response.body()
            ?: throw IOException("USGS returned an empty response")
        val usedOfflineCache = response.raw().request.cacheControl.onlyIfCached

        return EarthquakeFeed(
            earthquakes = responseBody.features.map { it.toEarthquake() },
            isFromOfflineCache = usedOfflineCache
        )
    }

    private fun EarthquakeFeatureDto.toEarthquake(): Earthquake = Earthquake(
        id = id,
        magnitude = properties.mag,
        place = properties.place,
        timeMillis = properties.time,
        longitude = geometry.coordinates.getOrNull(LONGITUDE_INDEX),
        latitude = geometry.coordinates.getOrNull(LATITUDE_INDEX),
        depthKilometers = geometry.coordinates.getOrNull(DEPTH_INDEX),
        detailsUrl = properties.detail ?: properties.url
    )

    private companion object {
        const val FORCE_REFRESH_CACHE_CONTROL = "no-cache"
        const val LONGITUDE_INDEX = 0
        const val LATITUDE_INDEX = 1
        const val DEPTH_INDEX = 2
    }
}
