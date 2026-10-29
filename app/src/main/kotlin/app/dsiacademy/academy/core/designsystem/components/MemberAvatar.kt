package app.dsiacademy.academy.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dsiacademy.academy.core.designsystem.theme.ManropeFamily
import coil.compose.AsyncImage

private val RoyalStart = Color(0xFF0A1B54)
private val RoyalEnd = Color(0xFF1D4ED8)

/**
 * Avatar membre — photo communauté (URL courte durée) ou initiales.
 * Aligné sur MemberAvatar.jsx du site.
 */
@Composable
fun MemberAvatar(
    imageUrl: String?,
    initials: String,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(RoyalStart, RoyalEnd))),
        contentAlignment = Alignment.Center,
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.size(size).clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                initials.ifBlank { "?" },
                fontFamily = ManropeFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.36f).sp,
                color = Color.White,
            )
        }
    }
}
