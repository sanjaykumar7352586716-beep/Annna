package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CalculatorColorScheme = lightColorScheme(
    background = ThemeBackground,
    surface = ThemeBackground,
    onBackground = ThemeTextPrimary,
    onSurface = ThemeTextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CalculatorColorScheme,
        typography = Typography,
        content = content
    )
}
