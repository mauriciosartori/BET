package com.sartori.brick.feature.earthquakedetail

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EarthquakeDetailPresentationTest {
    @Test
    fun `shakes only for severe magnitudes`() {
        assertFalse(shouldShakeMagnitude(null))
        assertFalse(shouldShakeMagnitude(4.9))
        assertTrue(shouldShakeMagnitude(5.0))
        assertTrue(shouldShakeMagnitude(8.2))
    }
}
