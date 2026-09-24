package com.sartori.brick.ui.earthquake

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.sartori.brick.ui.theme.MagnitudeLow
import com.sartori.brick.ui.theme.MagnitudeModerate
import com.sartori.brick.ui.theme.MagnitudeSevere
import com.sartori.brick.ui.theme.MagnitudeStrong
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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
