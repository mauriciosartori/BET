package com.sartori.brick.feature.earthquakelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sartori.brick.data.earthquake.EarthquakeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EarthquakeListViewModel @Inject constructor(
    private val repository: EarthquakeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EarthquakeListUiState())
    val uiState: StateFlow<EarthquakeListUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        loadEarthquakes(forceRefresh = true)
    }

    fun refresh() {
        loadEarthquakes(forceRefresh = true)
    }

    fun selectSortOption(sortOption: EarthquakeSortOption) {
        _uiState.update { currentState ->
            currentState.copy(sortOption = sortOption)
        }
    }

    private fun loadEarthquakes(forceRefresh: Boolean) {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            _uiState.update { currentState ->
                if (currentState.earthquakes.isEmpty()) {
                    currentState.copy(
                        isInitialLoading = true,
                        isRefreshing = false,
                        error = null
                    )
                } else {
                    currentState.copy(
                        isInitialLoading = false,
                        isRefreshing = true,
                        error = null
                    )
                }
            }

            try {
                val feed = repository.getEarthquakes(forceRefresh)
                _uiState.update { currentState ->
                    currentState.copy(
                        earthquakes = feed.earthquakes,
                        isInitialLoading = false,
                        isRefreshing = false,
                        isFromOfflineCache = feed.isFromOfflineCache,
                        error = null
                    )
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { currentState ->
                    currentState.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        error = if (currentState.earthquakes.isEmpty()) {
                            EarthquakeListError.INITIAL_LOAD_FAILED
                        } else {
                            EarthquakeListError.REFRESH_FAILED
                        }
                    )
                }
            }
        }
    }
}
