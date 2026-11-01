package app.dsiacademy.academy.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val SkelLight = Color(0xFFE2E8F0)
private val SkelDark = Color(0xFF334155)

@Composable
fun SkeletonBox(
    width: Dp? = null,
    height: Dp = 12.dp,
    circle: Boolean = false,
    onDark: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val color = if (onDark) SkelDark else SkelLight
    val shape = if (circle) CircleShape else RoundedCornerShape(6.dp)
    Box(
        modifier
            .then(
                when {
                    circle -> Modifier.size(width ?: height)
                    width != null -> Modifier.width(width).height(height)
                    else -> Modifier.height(height)
                },
            )
            .clip(shape)
            .background(color),
    )
}
