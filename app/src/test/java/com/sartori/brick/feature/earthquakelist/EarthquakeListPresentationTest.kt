package com.sartori.brick.feature.earthquakelist

import com.sartori.brick.data.earthquake.Earthquake
import org.junit.Assert.assertEquals
import org.junit.Test

class EarthquakeListPresentationTest {
    @Test
    fun `splits the region from a USGS place using the last comma`() {
        val location = splitLocation("9 km WNW of Azusa, California")

        assertEquals("California", location.region)
        assertEquals("9 km WNW of Azusa", location.description)
    }

    @Test
    fun `keeps a place without a region as one line`() {
        val location = splitLocation("South Sandwich Islands region")

        assertEquals(null, location.region)
        assertEquals("South Sandwich Islands region", location.description)
    }

    @Test
    fun `assigns magnitude colors at each boundary`() {
        assertEquals(MagnitudeLevel.UNKNOWN, magnitudeLevel(null))
        assertEquals(MagnitudeLevel.GREEN, magnitudeLevel(2.4))
        assertEquals(MagnitudeLevel.YELLOW, magnitudeLevel(2.5))
        assertEquals(MagnitudeLevel.ORANGE, magnitudeLevel(4.5))
        assertEquals(MagnitudeLevel.RED, magnitudeLevel(6.0))
    }

    @Test
    fun `sorts earthquakes by latest or strongest`() {
        val newest = earthquake(id = "newest", magnitude = 2.0, timeMillis = 300L)
        val strongest = earthquake(id = "strongest", magnitude = 6.0, timeMillis = 100L)
        val middle = earthquake(id = "middle", magnitude = 4.0, timeMillis = 200L)
        val earthquakes = listOf(strongest, newest, middle)

        assertEquals(
            listOf("newest", "middle", "strongest"),
            earthquakes.sortedFor(EarthquakeSortOption.LATEST).map(Earthquake::id)
        )
        assertEquals(
            listOf("strongest", "middle", "newest"),
            earthquakes.sortedFor(EarthquakeSortOption.STRONGEST).map(Earthquake::id)
        )
    }

    private fun earthquake(
        id: String,
        magnitude: Double,
        timeMillis: Long
    ) = Earthquake(
        id = id,
        magnitude = magnitude,
        place = null,
        timeMillis = timeMillis,
        longitude = null,
        latitude = null,
        depthKilometers = null,
        detailsUrl = null
    )
}
