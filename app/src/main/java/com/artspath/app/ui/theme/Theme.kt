package com.artspath.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.isSystemInDarkColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type pairing: Noto Serif (platform serif) for display/headings — textbook chapter
 * character, feels like a printed book; platform sans for body/UI — clarity at small
 * sizes. Tabular numerals for the clock and counters so digits don't jitter.
 */
val LocalPalette = staticCompositionLocalOf { LightPalette }

fun Typography.displaySerif(
    size: Int,
    weight: FontWeight = FontWeight.Bold,
    tracking: Double = -0.5
): TextStyle = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = weight,
    fontSize = size.sp,
    letterSpacing = tracking.sp
)

val TabularNumbers = TextStyle(fontFeatureSettings = "tnum")

@Composable
fun ArtsTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkColor()
    val palette = if (dark) DarkPalette else LightPalette
    CompositionLocalProvider(LocalPalette provides palette) {
        MaterialTheme(
            colorScheme = if (dark) darkScheme(palette) else lightScheme(palette),
            typography = Typography(),
            content = content
        )
    }
}

private fun lightScheme(p: Palette) = androidx.compose.material3.lightColorScheme(
    primary = p.accentDeep,
    onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    secondary = p.inkSoft,
    onSecondary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    tertiary = p.ok,
    background = p.paper,
    onBackground = p.ink,
    surface = p.paper,
    onSurface = p.ink,
    surfaceVariant = p.surfaceAlt,
    onSurfaceVariant = p.inkSoft,
    outline = p.inkFaint,
    error = p.danger
)

private fun darkScheme(p: Palette) = androidx.compose.material3.darkColorScheme(
    primary = p.accent,
    onPrimary = androidx.compose.ui.graphics.Color(0xFF241C12),
    secondary = p.inkSoft,
    onSecondary = androidx.compose.ui.graphics.Color(0xFF241C12),
    tertiary = p.ok,
    background = p.paper,
    onBackground = p.ink,
    surface = p.paper,
    onSurface = p.ink,
    surfaceVariant = p.surfaceAlt,
    onSurfaceVariant = p.inkSoft,
    outline = p.inkFaint,
    error = p.danger
)
