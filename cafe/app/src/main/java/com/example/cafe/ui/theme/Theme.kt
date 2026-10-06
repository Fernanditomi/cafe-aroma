package com.example.cafe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryCoffee,
    secondary = AccentCinnamon,
    tertiary = MutedTaupe,
    background = BgMain,
    surface = CardBg,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = PrimaryCoffee,
    onSurface = PrimaryCoffee
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentCinnamon,
    secondary = AccentCinnamon,
    tertiary = DarkMuted,
    background = DarkBg,
    surface = DarkCard,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = DarkText,
    onSurface = DarkText
)

@Composable
fun CafeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
