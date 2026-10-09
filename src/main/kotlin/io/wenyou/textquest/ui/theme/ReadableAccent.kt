package io.wenyou.textquest.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Foreground accents must remain readable across all neutral containers. */
internal fun ColorScheme.readableAccent(accent: Color = primary): Color {
    val foreground = accent.luminance()
    return if (listOf(background, surface, surfaceContainerLowest, surfaceContainerLow,
        surfaceContainer, surfaceContainerHigh, surfaceContainerHighest).all {
            val background = it.luminance()
            (maxOf(foreground, background) + .05f) / (minOf(foreground, background) + .05f) >= 4.5f
        }) accent else onSurface
}
