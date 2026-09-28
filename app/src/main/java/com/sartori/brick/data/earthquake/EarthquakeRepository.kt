package com.sartori.brick.data.earthquake

interface EarthquakeRepository {
    suspend fun getEarthquakes(): EarthquakeFeed
}
