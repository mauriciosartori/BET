package com.sartori.brick.data.earthquake

data class Earthquake(
    val id: String,
    val magnitude: Double?,
    val place: String?,
    val timeMillis: Long?,
    val longitude: Double?,
    val latitude: Double?,
    val depthKilometers: Double?,
    val detailsUrl: String?
)

data class EarthquakeFeed(
    val earthquakes: List<Earthquake>,
    val isFromOfflineCache: Boolean
)
