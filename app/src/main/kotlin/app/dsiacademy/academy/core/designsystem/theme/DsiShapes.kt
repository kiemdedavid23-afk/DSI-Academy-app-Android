package app.dsiacademy.academy.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

/**
 * Rayons extraits de global.css :
 * --radius-sm: 6px
 * --radius-md: 10px
 * --radius-lg: 16px
 * --radius-full: 9999px
 */
@Immutable
data class DsiShapes(
    val small: RoundedCornerShape = RoundedCornerShape(6.dp),
    val medium: RoundedCornerShape = RoundedCornerShape(10.dp),
    val large: RoundedCornerShape = RoundedCornerShape(16.dp),
    val full: RoundedCornerShape = RoundedCornerShape(9999.dp),
)

val DefaultDsiShapes = DsiShapes()
