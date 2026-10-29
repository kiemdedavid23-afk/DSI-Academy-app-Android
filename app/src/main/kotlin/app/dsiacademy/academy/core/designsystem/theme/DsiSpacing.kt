package app.dsiacademy.academy.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Échelle d'espacement alignée sur les valeurs rem du site (1 rem = 16 dp).
 * Les marges latérales d'écran du site (1.25 rem) correspondent à xl = 20 dp.
 */
@Immutable
data class DsiSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
)

val DefaultDsiSpacing = DsiSpacing()
