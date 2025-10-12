package com.yybb.myapplication.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme()

private val LightColorScheme = lightColorScheme()

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
    dynamicColor: Boolean = true,
    colorBlindMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        colorBlindMode -> ColorBlindColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicLightColorScheme(context)
        }
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}