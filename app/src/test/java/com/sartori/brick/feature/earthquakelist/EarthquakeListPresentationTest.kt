package com.sartori.brick.feature.earthquakelist

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
}
