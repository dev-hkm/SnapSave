package com.snapsave.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val LightScheme = lightColorScheme(
    primary = Color(0xFF4654E0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDFE1FF),
    onPrimaryContainer = Color(0xFF00125C),
    secondary = Color(0xFF5A5D72),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDFE1F9),
    onSecondaryContainer = Color(0xFF171B2C),
    tertiary = Color(0xFF77536D),
    tertiaryContainer = Color(0xFFFFD7F1),
    surface = Color(0xFFFBF8FF),
    background = Color(0xFFFBF8FF)
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFBBC3FF),
    onPrimary = Color(0xFF12226B),
    primaryContainer = Color(0xFF2E3CC8),
    onPrimaryContainer = Color(0xFFDFE1FF),
    secondary = Color(0xFFC3C5DD),
    secondaryContainer = Color(0xFF434659),
    onSecondaryContainer = Color(0xFFDFE1F9),
    tertiary = Color(0xFFE6BAD8),
    tertiaryContainer = Color(0xFF5D3C55),
    surface = Color(0xFF131318),
    background = Color(0xFF131318)
)

private val baseTypography = Typography()

val AppTypography = Typography(
    headlineLarge = baseTypography.headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineMedium = baseTypography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = baseTypography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = baseTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
private fun animateThemeColor(target: Color): Color =
    animateColorAsState(targetValue = target, animationSpec = tween(durationMillis = 220), label = "themeColor").value

@Composable
private fun ColorScheme.animated(): ColorScheme = copy(
    primary = animateThemeColor(primary),
    onPrimary = animateThemeColor(onPrimary),
    primaryContainer = animateThemeColor(primaryContainer),
    onPrimaryContainer = animateThemeColor(onPrimaryContainer),
    secondary = animateThemeColor(secondary),
    onSecondary = animateThemeColor(onSecondary),
    secondaryContainer = animateThemeColor(secondaryContainer),
    onSecondaryContainer = animateThemeColor(onSecondaryContainer),
    tertiary = animateThemeColor(tertiary),
    onTertiary = animateThemeColor(onTertiary),
    tertiaryContainer = animateThemeColor(tertiaryContainer),
    onTertiaryContainer = animateThemeColor(onTertiaryContainer),
    background = animateThemeColor(background),
    onBackground = animateThemeColor(onBackground),
    surface = animateThemeColor(surface),
    onSurface = animateThemeColor(onSurface),
    surfaceVariant = animateThemeColor(surfaceVariant),
    onSurfaceVariant = animateThemeColor(onSurfaceVariant),
    surfaceTint = animateThemeColor(surfaceTint),
    inverseSurface = animateThemeColor(inverseSurface),
    inverseOnSurface = animateThemeColor(inverseOnSurface),
    error = animateThemeColor(error),
    onError = animateThemeColor(onError),
    errorContainer = animateThemeColor(errorContainer),
    onErrorContainer = animateThemeColor(onErrorContainer),
    outline = animateThemeColor(outline),
    outlineVariant = animateThemeColor(outlineVariant),
    scrim = animateThemeColor(scrim),
    surfaceBright = animateThemeColor(surfaceBright),
    surfaceDim = animateThemeColor(surfaceDim),
    surfaceContainer = animateThemeColor(surfaceContainer),
    surfaceContainerHigh = animateThemeColor(surfaceContainerHigh),
    surfaceContainerHighest = animateThemeColor(surfaceContainerHighest),
    surfaceContainerLow = animateThemeColor(surfaceContainerLow),
    surfaceContainerLowest = animateThemeColor(surfaceContainerLowest)
)

/**
 * themeMode: 0 = hệ thống, 1 = sáng, 2 = tối.
 * dynamicColor: Material You (API 31+); máy cũ dùng bảng màu brand phía trên.
 */
@Composable
fun AppTheme(
    themeMode: Int = 0,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    val context = LocalContext.current
    val rawScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkScheme
        else -> LightScheme
    }
    val scheme = rawScheme.animated()

    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(dark) {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !dark
                controller.isAppearanceLightNavigationBars = !dark
            }
            onDispose {}
        }
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
