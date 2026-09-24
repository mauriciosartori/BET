package com.sartori.brick.data.earthquake

data class Earthquake(
    val id: String,
    val magnitude: Double?,
    val place: String?,
    val timeMillis: Long?,
    val longitude: Double?,
    val latitude: Double?,
    val depthKilometers: Double?,
    val detailsUrl: String?,
    val alert: String? = null,
    val hasTsunamiRisk: Boolean = false
)

data class EarthquakeFeed(
    val earthquakes: List<Earthquake>,
    val isFromOfflineCache: Boolean
)

internal fun Earthquake.hasMapCoordinates(): Boolean =
    latitude != null && longitude != null && latitude.isFinite() && longitude.isFinite() &&
        latitude in -90.0..90.0 && longitude in -180.0..180.0
