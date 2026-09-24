package com.sartori.brick.data.earthquake

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EarthquakeCoordinatesTest {
    private val event = Earthquake(
        id = "coordinates", magnitude = null, place = null, timeMillis = null,
        longitude = 0.0, latitude = 0.0, depthKilometers = null, detailsUrl = null
    )

    @Test
    fun `accepts zero and geographic boundaries`() {
        assertTrue(event.hasMapCoordinates())
        assertTrue(event.copy(latitude = -90.0, longitude = -180.0).hasMapCoordinates())
        assertTrue(event.copy(latitude = 90.0, longitude = 180.0).hasMapCoordinates())
    }

    @Test
    fun `rejects missing nonfinite and out of range coordinates`() {
        listOf(null, Double.NaN, Double.POSITIVE_INFINITY, -90.1, 90.1).forEach {
            assertFalse(event.copy(latitude = it).hasMapCoordinates())
        }
        listOf(null, Double.NaN, Double.NEGATIVE_INFINITY, -180.1, 180.1).forEach {
            assertFalse(event.copy(longitude = it).hasMapCoordinates())
        }
    }
}
