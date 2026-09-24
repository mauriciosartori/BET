package com.sartori.brick.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sartori.brick.ui.theme.BrandOchre

@Composable
internal fun StatusMessage(
    text: String,
    tone: StatusMessageTone = StatusMessageTone.INFO,
    modifier: Modifier = Modifier
) {
    val containerColor = when (tone) {
        StatusMessageTone.INFO -> MaterialTheme.colorScheme.secondaryContainer
        StatusMessageTone.OFFLINE -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val borderColor = when (tone) {
        StatusMessageTone.INFO -> MaterialTheme.colorScheme.secondary
        StatusMessageTone.OFFLINE -> BrandOchre
    }
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.55f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


internal enum class StatusMessageTone {
    INFO,
    OFFLINE
}
