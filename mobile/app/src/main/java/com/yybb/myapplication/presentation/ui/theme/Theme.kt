package com.yybb.myapplication.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme()

private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    secondary = Color.Black
)

private val ColorBlindColorScheme = lightColorScheme(
    primary = CB_ButtonPrimary,
    onPrimary = Color.White,
    primaryContainer = CB_NodeSelected,
    onPrimaryContainer = Color.White,
    secondary = CB_ButtonSecondary,
    onSecondary = Color.White,
    tertiary = CB_NodeDefault,
    onTertiary = Color.White,
    background = CB_Background,
    onBackground = CB_PrimaryText,
    surface = CB_CardBackground,
    onSurface = CB_PrimaryText,
    surfaceVariant = CB_Header,
    onSurfaceVariant = CB_SecondaryText,
    error = CB_Error,
    onError = Color.White,
    outline = CB_InputBorder,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    colorBlindMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        colorBlindMode -> ColorBlindColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}