package com.trading.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TradingDarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = DarkBackground,
    primaryContainer = PrimaryBlueDark,
    secondary = ProfitGreen,
    tertiary = ChartYellow,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = LossRed,
    onError = TextPrimary,
)

@Composable
fun TradingAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TradingDarkColorScheme,
        content = content
    )
}
