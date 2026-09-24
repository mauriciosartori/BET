package com.sartori.brick.feature.earthquakedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.sartori.brick.R
import com.sartori.brick.data.earthquake.Earthquake
import com.sartori.brick.feature.earthquakelist.formatEarthquakeTime
import com.sartori.brick.feature.earthquakelist.magnitudeColor
import com.sartori.brick.feature.earthquakemap.EarthquakeMarkerBadge
import com.sartori.brick.feature.earthquakemap.rememberEarthquakeMapStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeDetailScreen(
    earthquake: Earthquake?,
    isLoading: Boolean,
    isFromOfflineCache: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.earthquake_details)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        )
    }) { padding ->
        if (earthquake == null) {
            Column(
                Modifier.padding(padding).fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) CircularProgressIndicator() else {
                    Text(stringResource(R.string.detail_unavailable))
                    Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                }
            }
        } else {
            Column(
                Modifier.padding(padding).fillMaxSize()
                    .verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (isFromOfflineCache) Text(stringResource(R.string.offline_data_message))

                Text(
                    earthquake.place ?: stringResource(R.string.unknown_location),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(Modifier.size(24.dp).background(magnitudeColor(earthquake.magnitude), CircleShape))
                    Column {
                        Text(stringResource(R.string.magnitude), style = MaterialTheme.typography.labelLarge)
                        Text(
                            earthquake.magnitude?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "—",
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                }

                DetailField(
                    stringResource(R.string.event_time),
                    earthquake.timeMillis?.let(::formatEarthquakeTime) ?: "—"
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                EarthquakeMap(earthquake)
                EventDataSection(earthquake)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun EventDataSection(earthquake: Earthquake) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = stringResource(R.string.event_data),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailField(
                stringResource(R.string.latitude),
                earthquake.latitude?.let { String.format(Locale.getDefault(), "%.4f", it) } ?: "—",
                Modifier.weight(1f)
            )
            DetailField(
                stringResource(R.string.longitude),
                earthquake.longitude?.let { String.format(Locale.getDefault(), "%.4f", it) } ?: "—",
                Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth()) {
            DetailField(
                stringResource(R.string.depth),
                earthquake.depthKilometers?.let { String.format(Locale.getDefault(), "%.1f km", it) } ?: "—",
                Modifier.weight(1f)
            )
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun DetailField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun EarthquakeMap(earthquake: Earthquake) {
    val latitude = earthquake.latitude
    val longitude = earthquake.longitude
    val validCoordinates = latitude != null && longitude != null &&
        latitude.isFinite() && longitude.isFinite() && latitude in -90.0..90.0 && longitude in -180.0..180.0
    if (!validCoordinates || stringResource(R.string.maps_api_key).isBlank()) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(220.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) { Text(stringResource(R.string.map_unavailable)) }
        }
        return
    }
    val position = LatLng(requireNotNull(latitude), requireNotNull(longitude))
    val camera = rememberCameraPositionState {
        this.position = CameraPosition.fromLatLngZoom(position, 5f)
    }
    val mapStyle = rememberEarthquakeMapStyle()
    GoogleMap(
        modifier = Modifier.fillMaxWidth().height(220.dp).clip(MaterialTheme.shapes.medium),
        cameraPositionState = camera,
        properties = MapProperties(mapStyleOptions = mapStyle),
        uiSettings = MapUiSettings(scrollGesturesEnabled = false, mapToolbarEnabled = false)
    ) {
        MarkerComposable(
            earthquake.id,
            earthquake.magnitude ?: Double.NaN,
            state = rememberUpdatedMarkerState(position = position),
            anchor = Offset(0.5f, 0.5f),
            title = earthquake.place
        ) {
            EarthquakeMarkerBadge(earthquake.magnitude)
        }
    }
}
