package com.sartori.brick.feature.earthquakelist

import com.sartori.brick.data.earthquake.Earthquake
import com.sartori.brick.data.earthquake.EarthquakeFeed
import com.sartori.brick.data.earthquake.EarthquakeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class EarthquakeListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial load exposes earthquake data in one state`() = runTest {
        val repository = FakeEarthquakeRepository(Result.success(TEST_FEED))
        val viewModel = EarthquakeListViewModel(repository)

        advanceUntilIdle()

        assertEquals(TEST_EARTHQUAKES, viewModel.uiState.value.earthquakes)
        assertFalse(viewModel.uiState.value.isInitialLoading)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(null, viewModel.uiState.value.error)
        assertEquals(listOf(true), repository.forceRefreshValues)
    }

    @Test
    fun `failed refresh keeps existing data and exposes refresh error`() = runTest {
        val repository = FakeEarthquakeRepository(Result.success(TEST_FEED))
        val viewModel = EarthquakeListViewModel(repository)
        advanceUntilIdle()
        repository.result = Result.failure(IllegalStateException("Network failed"))

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(TEST_EARTHQUAKES, viewModel.uiState.value.earthquakes)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(EarthquakeListError.REFRESH_FAILED, viewModel.uiState.value.error)
    }

    private class FakeEarthquakeRepository(
        var result: Result<EarthquakeFeed>
    ) : EarthquakeRepository {
        val forceRefreshValues = mutableListOf<Boolean>()

        override suspend fun getEarthquakes(forceRefresh: Boolean): EarthquakeFeed {
            forceRefreshValues += forceRefresh
            return result.getOrThrow()
        }
    }

    private companion object {
        val TEST_EARTHQUAKES = listOf(
            Earthquake(
                id = "test",
                magnitude = 4.2,
                place = "Test location",
                timeMillis = 1_725_000_000_000,
                longitude = -118.24,
                latitude = 34.05,
                depthKilometers = 8.4,
                detailsUrl = null
            )
        )
        val TEST_FEED = EarthquakeFeed(
            earthquakes = TEST_EARTHQUAKES,
            isFromOfflineCache = false
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
