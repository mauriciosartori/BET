package com.sartori.brick.feature.earthquakemap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.MapStyleOptions
import com.sartori.brick.R
import com.sartori.brick.ui.earthquake.MagnitudeLevel
import com.sartori.brick.ui.earthquake.magnitudeColor
import com.sartori.brick.ui.earthquake.magnitudeLevel
import com.sartori.brick.ui.theme.Charcoal
import java.util.Locale

@Composable
internal fun rememberEarthquakeMapStyle(): MapStyleOptions {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    return remember(context, isDarkTheme) {
        MapStyleOptions.loadRawResourceStyle(
            context,
            if (isDarkTheme) R.raw.map_style_dark else R.raw.map_style_light
        )
    }
}

@Composable
internal fun EarthquakeMarkerBadge(
    magnitude: Double?,
    label: String = magnitude?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "?",
    size: Dp = 34.dp
) {
    val contentColor = when (magnitudeLevel(magnitude)) {
        MagnitudeLevel.GREEN, MagnitudeLevel.RED -> Color.White
        MagnitudeLevel.YELLOW, MagnitudeLevel.ORANGE -> Charcoal
        MagnitudeLevel.UNKNOWN -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = Modifier
            .size(size)
            .background(magnitudeColor(magnitude), CircleShape)
            .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
