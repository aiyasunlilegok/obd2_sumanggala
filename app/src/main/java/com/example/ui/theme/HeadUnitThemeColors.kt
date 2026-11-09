package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import java.util.Calendar

data class CockpitColorScheme(
    val isDark: Boolean,
    val screenBackground: Color,
    val surfaceBackground: Color,
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val borderStroke: Color,
    val dividerColor: Color
)

object HeadUnitThemeColors {
    val DarkScheme = CockpitColorScheme(
        isDark = true,
        screenBackground = Color(0xFF070B12),
        surfaceBackground = Color(0xFF090E17),
        cardBackground = Color(0xFF131D2D),
        textPrimary = Color.White,
        textSecondary = Color(0xFFE2E8F0),
        textMuted = Color(0xFF94A3B8),
        borderStroke = Color(0xFF1E293B),
        dividerColor = Color(0xFF334155)
    )

    // High-Contrast Day Mode for Car Head Units (Sunlight-optimized, Anti-glare)
    val LightScheme = CockpitColorScheme(
        isDark = false,
        screenBackground = Color(0xFFE2E8F0),
        surfaceBackground = Color(0xFFF1F5F9),
        cardBackground = Color(0xFFFFFFFF),
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF1E293B),
        textMuted = Color(0xFF475569),
        borderStroke = Color(0xFFCBD5E1),
        dividerColor = Color(0xFF94A3B8)
    )

    /**
     * Determines if night mode should be active based on user choice, system theme, or local time.
     */
    fun isNightTime(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour >= 18 || hour < 6
    }
}
