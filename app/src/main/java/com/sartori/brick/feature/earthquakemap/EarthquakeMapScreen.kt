package com.sartori.brick.feature.earthquakemap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.sartori.brick.data.earthquake.hasMapCoordinates
import com.sartori.brick.ui.components.StatusMessage
import com.sartori.brick.ui.components.StatusMessageTone
import com.sartori.brick.R
import com.sartori.brick.data.earthquake.Earthquake
import com.sartori.brick.feature.earthquakelist.EarthquakeListError
import com.sartori.brick.feature.earthquakelist.EarthquakeListUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeMapScreen(
    uiState: EarthquakeListUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onEarthquakeClick: (String) -> Unit
) {
    val events = remember(uiState.earthquakes) { uiState.earthquakes.filter { it.hasMapCoordinates() } }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.earthquake_map)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.back))
                }
            }
        )
    }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                uiState.isInitialLoading -> CircularProgressIndicator()
                uiState.error == EarthquakeListError.INITIAL_LOAD_FAILED -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(stringResource(R.string.earthquake_error_message))
                        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                    }
                }
                stringResource(R.string.maps_api_key).isBlank() -> {
                    Text(stringResource(R.string.map_screen_unavailable), Modifier.padding(24.dp))
                }
                events.isEmpty() -> Text(stringResource(R.string.map_no_coordinates), Modifier.padding(24.dp))
                else -> {
                    EarthquakeMarkers(events, onEarthquakeClick)
                    Column(
                        Modifier.align(Alignment.TopCenter).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.isFromOfflineCache) {
                            StatusMessage(
                                text = stringResource(R.string.offline_data_message),
                                tone = StatusMessageTone.OFFLINE,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (events.size != uiState.earthquakes.size) StatusMessage(
                            text = stringResource(R.string.map_missing_coordinates),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(MapsComposeExperimentalApi::class)
private fun EarthquakeMarkers(events: List<Earthquake>, onEarthquakeClick: (String) -> Unit) {
    val hasLocationPermission = rememberLocationPermission()
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(39.8, -98.6), 3f)
    }
    val items = remember(events) { events.map { EarthquakeClusterItem(it) } }
    val scope = rememberCoroutineScope()
    val mapStyle = rememberEarthquakeMapStyle()

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = camera,
        properties = MapProperties(
            isMyLocationEnabled = hasLocationPermission,
            mapStyleOptions = mapStyle
        ),
        uiSettings = MapUiSettings(
            mapToolbarEnabled = false,
            myLocationButtonEnabled = hasLocationPermission
        )
    ) {
        Clustering(
            items = items,
            clusterContentAnchor = Offset(0.5f, 0.5f),
            clusterItemContentAnchor = Offset(0.5f, 0.5f),
            clusterContent = { cluster ->
                val strongestMagnitude = cluster.items.maxOfOrNull {
                    it.earthquake.magnitude ?: Double.NEGATIVE_INFINITY
                }?.takeUnless { it == Double.NEGATIVE_INFINITY }
                val clusterSize = when {
                    cluster.size >= 100 -> 54.dp
                    cluster.size >= 10 -> 48.dp
                    else -> 42.dp
                }
                EarthquakeMarkerBadge(
                    magnitude = strongestMagnitude,
                    label = cluster.size.toString(),
                    size = clusterSize
                )
            },
            clusterItemContent = { item ->
                EarthquakeMarkerBadge(item.earthquake.magnitude)
            },
            onClusterClick = { cluster ->
                scope.launch {
                    camera.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            cluster.position,
                            (camera.position.zoom + 2f).coerceAtMost(21f)
                        )
                    )
                }
                true
            },
            onClusterItemClick = { item ->
                onEarthquakeClick(item.earthquake.id)
                true
            }
        )
    }
}
