package com.example.aibudgetapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*



private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightCard,
    secondary = AccentGreen,
    onSecondary = LightCard,
    background = LightCanvas,
    onBackground = LightText,
    surface = LightCard,
    onSurface = LightText,
    surfaceVariant = LightCanvas,
    onSurfaceVariant = LightText2,
    outline = LightOutline,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkCard,
    secondary = AccentGreen,
    onSecondary = DarkCard,
    background = DarkCanvas,
    onBackground = DarkText,
    surface = DarkCard,
    onSurface = DarkText,
    surfaceVariant = DarkCanvas,
    onSurfaceVariant = DarkText2,
    outline = DarkOutline,
)

enum class ThemeMode { FollowSystem, Light, Dark }

@Stable
class ThemeController {
    var mode by mutableStateOf(ThemeMode.FollowSystem)
}

val LocalThemeController = staticCompositionLocalOf { ThemeController() }

@Composable
fun AIBudgetAppTheme(
    mode: ThemeMode = LocalThemeController.current.mode,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (mode) {
        ThemeMode.FollowSystem -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}