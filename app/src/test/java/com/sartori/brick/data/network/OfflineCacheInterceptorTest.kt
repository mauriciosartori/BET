package com.sartori.brick.data.network

import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class OfflineCacheInterceptorTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `returns a stale cached response when the network request fails`() {
        val server = MockWebServer()
        server.start()
        server.enqueue(
            MockResponse()
                .setHeader("Cache-Control", "public, max-age=0")
                .setBody(RESPONSE_BODY)
        )

        val client = OkHttpClient.Builder()
            .cache(Cache(temporaryFolder.newFolder("http_cache"), CACHE_SIZE_BYTES))
            .addInterceptor(OfflineCacheInterceptor())
            .build()
        val request = Request.Builder()
            .url(server.url("/earthquakes"))
            .build()

        client.newCall(request).execute().use { response ->
            assertEquals(RESPONSE_BODY, response.body.string())
        }
        server.shutdown()

        client.newCall(request).execute().use { response ->
            assertEquals(RESPONSE_BODY, response.body.string())
            assertNull(response.networkResponse)
        }
    }

    private companion object {
        const val RESPONSE_BODY = "earthquake data"
        const val CACHE_SIZE_BYTES = 1024L * 1024L
    }
}
