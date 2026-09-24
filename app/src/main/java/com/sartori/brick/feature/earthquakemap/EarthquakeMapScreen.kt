package com.sartori.brick.feature.earthquakemap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
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
                        if (uiState.isFromOfflineCache) MapNotice(stringResource(R.string.offline_data_message))
                        if (events.size != uiState.earthquakes.size) MapNotice(stringResource(R.string.map_missing_coordinates))
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(MapsComposeExperimentalApi::class)
private fun EarthquakeMarkers(events: List<Earthquake>, onEarthquakeClick: (String) -> Unit) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(context.hasLocationPermission())
    }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.any { it }
    }
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(39.8, -98.6), 3f)
    }
    val items = remember(events) { events.map { EarthquakeClusterItem(it) } }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!hasLocationPermission && !permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = camera,
        properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
        uiSettings = MapUiSettings(
            mapToolbarEnabled = false,
            myLocationButtonEnabled = hasLocationPermission
        )
    ) {
        Clustering(
            items = items,
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

@Composable
private fun MapNotice(message: String) {
    Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) {
        Text(message, Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

internal fun Earthquake.hasMapCoordinates(): Boolean =
    latitude != null && longitude != null && latitude.isFinite() && longitude.isFinite() &&
        latitude in -90.0..90.0 && longitude in -180.0..180.0

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
