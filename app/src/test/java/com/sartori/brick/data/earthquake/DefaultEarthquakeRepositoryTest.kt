package com.sartori.brick.data.earthquake

import com.sartori.brick.data.earthquake.network.EarthquakeApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class DefaultEarthquakeRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: DefaultEarthquakeRepository
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(EarthquakeApi::class.java)
        repository = DefaultEarthquakeRepository(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `maps a USGS response to earthquakes`() = runTest {
        server.enqueue(MockResponse().setBody(USGS_RESPONSE))

        val feed = repository.getEarthquakes()

        assertFalse(feed.isFromOfflineCache)
        assertEquals(1, feed.earthquakes.size)
        with(feed.earthquakes.single()) {
            assertEquals("test-earthquake", id)
            assertEquals(4.2, magnitude)
            assertEquals("12 km NW of Los Angeles, California", place)
            assertEquals(-118.24, longitude)
            assertEquals(34.05, latitude)
            assertEquals(8.4, depthKilometers)
            assertEquals("orange", alert)
            assertEquals(true, hasTsunamiRisk)
        }
    }

    private companion object {
        val USGS_RESPONSE = """
            {
              "type": "FeatureCollection",
              "unknownField": "ignored",
              "features": [
                {
                  "id": "test-earthquake",
                  "properties": {
                    "mag": 4.2,
                    "place": "12 km NW of Los Angeles, California",
                    "time": 1725000000000,
                    "url": "https://example.com/earthquake",
                    "detail": "https://example.com/earthquake.geojson",
                    "alert": "orange",
                    "tsunami": 1
                  },
                  "geometry": {
                    "type": "Point",
                    "coordinates": [-118.24, 34.05, 8.4]
                  }
                }
              ]
            }
        """.trimIndent()
    }
}
