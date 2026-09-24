package com.sartori.brick.feature.earthquakelist

import com.sartori.brick.data.earthquake.Earthquake

data class EarthquakeListUiState(
    val earthquakes: List<Earthquake> = emptyList(),
    val sortOption: EarthquakeSortOption = EarthquakeSortOption.LATEST,
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isFromOfflineCache: Boolean = false,
    val error: EarthquakeListError? = null
)

enum class EarthquakeSortOption {
    LATEST,
    STRONGEST
}

enum class EarthquakeListError {
    INITIAL_LOAD_FAILED,
    REFRESH_FAILED
}
