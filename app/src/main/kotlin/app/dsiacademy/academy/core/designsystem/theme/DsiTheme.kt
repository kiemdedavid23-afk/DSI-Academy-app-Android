package app.dsiacademy.academy.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LocalDsiColors = staticCompositionLocalOf { lightDsiColorScheme() }
private val LocalDsiTypography = staticCompositionLocalOf { DefaultDsiTypography }
private val LocalDsiShapes = staticCompositionLocalOf { DefaultDsiShapes }
private val LocalDsiSpacing = staticCompositionLocalOf { DefaultDsiSpacing }

object DsiTheme {
    val colors: DsiColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalDsiColors.current

    val typography: DsiTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalDsiTypography.current

    val shapes: DsiShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalDsiShapes.current

    val spacing: DsiSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalDsiSpacing.current
}

@Composable
fun DsiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) darkDsiColorScheme() else lightDsiColorScheme()

    CompositionLocalProvider(
        LocalDsiColors provides colors,
        LocalDsiTypography provides DefaultDsiTypography,
        LocalDsiShapes provides DefaultDsiShapes,
        LocalDsiSpacing provides DefaultDsiSpacing,
    ) {
        content()
    }
}
