package com.artspath.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * "Paper planner" palette: warm handmade-paper background, near-black ink,
 * one marigold accent (turmeric/marigold margin-line tradition), muted subject inks.
 * Identity comes from paper + ink + the marigold margin line — not gradients or glass.
 */

data class Palette(
    val paper: Color,
    val surfaceAlt: Color,
    val surfaceRaised: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkFaint: Color,
    val hairline: Color,
    val accent: Color,
    val accentDeep: Color,
    val accentSoft: Color,
    val ok: Color,
    val danger: Color,
    val dangerSoft: Color
)

val LightPalette = Palette(
    paper = Color(0xFFFAF5EB),
    surfaceAlt = Color(0xFFF3EBDC),
    surfaceRaised = Color(0xFFFFFCF5),
    ink = Color(0xFF241C12),
    inkSoft = Color(0xFF6B5F4E),
    inkFaint = Color(0xFFA99C87),
    hairline = Color(0x1F241C12),
    accent = Color(0xFFE8A13D),
    accentDeep = Color(0xFF9C6410),
    accentSoft = Color(0x26E8A13D),
    ok = Color(0xFF5F7A3D),
    danger = Color(0xFFC2502A),
    dangerSoft = Color(0x24C2502A)
)

val DarkPalette = Palette(
    paper = Color(0xFF171310),
    surfaceAlt = Color(0xFF211B15),
    surfaceRaised = Color(0xFF2A221A),
    ink = Color(0xFFF2E9DA),
    inkSoft = Color(0xFFB4A896),
    inkFaint = Color(0xFF7D7263),
    hairline = Color(0x24F2E9DA),
    accent = Color(0xFFF0B45C),
    accentDeep = Color(0xFFFFCB7A),
    accentSoft = Color(0x33F0B45C),
    ok = Color(0xFF9DBB77),
    danger = Color(0xFFE07248),
    dangerSoft = Color(0x33E07248)
)

/** Fixed subject-ink assignment so a subject is always the same colour. */
private val subjectInksLight = mapOf(
    "terracotta" to Color(0xFFC4572E),
    "indigo" to Color(0xFF44578F),
    "moss" to Color(0xFF5F7A3D),
    "plum" to Color(0xFF8A4A62),
    "teal" to Color(0xFF2F7E77),
    "ochre" to Color(0xFFB07F22),
    "copper" to Color(0xFF9A5B33),
    "sand" to Color(0xFF97742F),
    "wine" to Color(0xFF7E3B47),
    "olive" to Color(0xFF6E7329),
    "forest" to Color(0xFF3E6B4A),
    "slate" to Color(0xFF55636F),
    "berry" to Color(0xFF7C4394),
    "rose" to Color(0xFFB25B6B),
    "steel" to Color(0xFF4A6B8A)
)

private val subjectInksDark = mapOf(
    "terracotta" to Color(0xFFE8845C),
    "indigo" to Color(0xFF8C9EE0),
    "moss" to Color(0xFF9DBB77),
    "plum" to Color(0xFFC98AA6),
    "teal" to Color(0xFF6FC2BA),
    "ochre" to Color(0xFFE0B45C),
    "copper" to Color(0xFFD09066),
    "sand" to Color(0xFFD0A96B),
    "wine" to Color(0xFFC97987),
    "olive" to Color(0xFFB7BB6E),
    "forest" to Color(0xFF7FB08D),
    "slate" to Color(0xFF93A3B2),
    "berry" to Color(0xFFB585CE),
    "rose" to Color(0xFFDE8FA0),
    "steel" to Color(0xFF82A6C8)
)

private val fallbackInk = listOf(
    "terracotta", "indigo", "moss", "plum", "teal", "ochre",
    "wine", "olive", "slate", "berry", "rose", "steel"
)

fun subjectColor(colorKey: String, dark: Boolean): Color {
    val map = if (dark) subjectInksDark else subjectInksLight
    return map[colorKey] ?: map[fallbackInk[(colorKey.hashCode().let { if (it < 0) -it else it }) % fallbackInk.size]]!!
}
