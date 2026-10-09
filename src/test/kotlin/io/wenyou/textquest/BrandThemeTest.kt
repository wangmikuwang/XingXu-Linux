package io.wenyou.textquest

import androidx.compose.ui.graphics.luminance
import io.wenyou.textquest.ui.theme.LightColors
import io.wenyou.textquest.ui.theme.DarkColors
import io.wenyou.textquest.ui.theme.readableAccent
import org.junit.Assert.assertTrue
import org.junit.Test

class BrandThemeTest {
    @Test fun lightAndDarkPalettesKeepTextReadable() {
        for (c in listOf(LightColors, DarkColors)) {
            val pairs = listOf(c.onPrimary to c.primary, c.onSecondary to c.secondary,
                c.onTertiary to c.tertiary, c.onPrimaryContainer to c.primaryContainer,
                c.onSecondaryContainer to c.secondaryContainer, c.onTertiaryContainer to c.tertiaryContainer,
                c.onBackground to c.background) + listOf(c.surface, c.surfaceContainerLowest,
                c.surfaceContainerLow, c.surfaceContainer, c.surfaceContainerHigh, c.surfaceContainerHighest).flatMap {
                listOf(c.onSurface to it, c.onSurfaceVariant to it, c.readableAccent() to it)
            }
            for ((foreground, background) in pairs) {
                val a = foreground.luminance(); val b = background.luminance()
                assertTrue("Unreadable brand text on $background",
                    (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f) >= 4.5f)
            }
        }
    }
}
