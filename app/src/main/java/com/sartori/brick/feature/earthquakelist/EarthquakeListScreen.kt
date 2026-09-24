package com.sartori.brick.feature.earthquakelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sartori.brick.R
import com.sartori.brick.data.earthquake.Earthquake
import com.sartori.brick.ui.theme.BrickTheme
import com.sartori.brick.ui.theme.MagnitudeLow
import com.sartori.brick.ui.theme.MagnitudeModerate
import com.sartori.brick.ui.theme.MagnitudeSevere
import com.sartori.brick.ui.theme.MagnitudeStrong
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun EarthquakeListScreen(
    viewModel: EarthquakeListViewModel = viewModel(),
    onEarthquakeClick: (String) -> Unit = {},
    onMapClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    EarthquakeListContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        onEarthquakeClick = onEarthquakeClick,
        onMapClick = onMapClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EarthquakeListContent(
    uiState: EarthquakeListUiState,
    onRefresh: () -> Unit,
    onEarthquakeClick: (String) -> Unit = {},
    onMapClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.earthquake_list_title)) },
                actions = {
                    IconButton(
                        onClick = onMapClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_map),
                            contentDescription = stringResource(R.string.earthquake_map)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isInitialLoading -> LoadingContent(
                modifier = Modifier.padding(innerPadding)
            )

            uiState.error == EarthquakeListError.INITIAL_LOAD_FAILED -> MessageContent(
                message = stringResource(R.string.earthquake_error_message),
                onRetry = onRefresh,
                modifier = Modifier.padding(innerPadding)
            )

            uiState.earthquakes.isEmpty() -> MessageContent(
                message = stringResource(R.string.earthquake_empty_message),
                onRetry = onRefresh,
                modifier = Modifier.padding(innerPadding)
            )

            else -> EarthquakeList(
                uiState = uiState,
                onRefresh = onRefresh,
                onEarthquakeClick = onEarthquakeClick,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MessageContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun EarthquakeList(
    uiState: EarthquakeListUiState,
    onRefresh: () -> Unit,
    onEarthquakeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isFromOfflineCache) {
                item {
                    StatusMessage(text = stringResource(R.string.offline_data_message))
                }
            }

            if (uiState.error == EarthquakeListError.REFRESH_FAILED) {
                item {
                    StatusMessage(text = stringResource(R.string.refresh_failed_message))
                }
            }

            item {
                Text(
                    text = stringResource(R.string.earthquake_count, uiState.earthquakes.size),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            items(
                items = uiState.earthquakes,
                key = Earthquake::id
            ) { earthquake ->
                EarthquakeListItem(earthquake, onClick = { onEarthquakeClick(earthquake.id) })
            }
        }
    }
}

@Composable
private fun StatusMessage(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EarthquakeListItem(earthquake: Earthquake, onClick: () -> Unit) {
    val location = splitLocation(earthquake.place)
    val cardColors = if (earthquake.hasTsunamiRisk) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    } else {
        CardDefaults.cardColors()
    }
    val supportingTextColor = if (earthquake.hasTsunamiRisk) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = cardColors
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = magnitudeColor(earthquake.magnitude),
                            shape = CircleShape
                        )
                )
                Text(
                    text = earthquake.magnitude?.let { "%.1f".format(it) } ?: "—",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                location.region?.let { region ->
                    Text(
                        text = region,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = location.description.ifEmpty {
                        stringResource(R.string.unknown_location)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = supportingTextColor
                )
                earthquake.timeMillis?.let { timeMillis ->
                    Text(
                        text = formatEarthquakeTime(timeMillis),
                        style = MaterialTheme.typography.bodyMedium,
                        color = supportingTextColor
                    )
                }
            }
        }
    }
}

@Composable
internal fun magnitudeColor(magnitude: Double?) = when (magnitudeLevel(magnitude)) {
    MagnitudeLevel.UNKNOWN -> MaterialTheme.colorScheme.outline
    MagnitudeLevel.GREEN -> MagnitudeLow
    MagnitudeLevel.YELLOW -> MagnitudeModerate
    MagnitudeLevel.ORANGE -> MagnitudeStrong
    MagnitudeLevel.RED -> MagnitudeSevere
}

internal fun magnitudeLevel(magnitude: Double?): MagnitudeLevel = when {
    magnitude == null -> MagnitudeLevel.UNKNOWN
    magnitude < 2.5 -> MagnitudeLevel.GREEN
    magnitude < 4.5 -> MagnitudeLevel.YELLOW
    magnitude < 6.0 -> MagnitudeLevel.ORANGE
    else -> MagnitudeLevel.RED
}

internal fun splitLocation(place: String?): EarthquakeLocation {
    val normalizedPlace = place?.trim().orEmpty()
    val separatorIndex = normalizedPlace.lastIndexOf(',')
    if (separatorIndex <= 0 || separatorIndex == normalizedPlace.lastIndex) {
        return EarthquakeLocation(region = null, description = normalizedPlace)
    }

    return EarthquakeLocation(
        region = normalizedPlace.substring(separatorIndex + 1).trim(),
        description = normalizedPlace.substring(0, separatorIndex).trim()
    )
}

internal data class EarthquakeLocation(
    val region: String?,
    val description: String
)

internal enum class MagnitudeLevel {
    UNKNOWN,
    GREEN,
    YELLOW,
    ORANGE,
    RED
}

internal fun formatEarthquakeTime(timeMillis: Long): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .format(Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault()))

@Preview(showBackground = true)
@Composable
private fun EarthquakeListPreview() {
    BrickTheme {
        EarthquakeListContent(
            uiState = EarthquakeListUiState(
                earthquakes = listOf(
                    Earthquake(
                        id = "preview",
                        magnitude = 4.2,
                        place = "12 km NW of Los Angeles, California",
                        timeMillis = 1_725_000_000_000,
                        longitude = -118.24,
                        latitude = 34.05,
                        depthKilometers = 8.4,
                        detailsUrl = null,
                        alert = null,
                        hasTsunamiRisk = false
                    )
                ),
                isInitialLoading = false,
                isFromOfflineCache = false
            ),
            onRefresh = {}
        )
    }
}
