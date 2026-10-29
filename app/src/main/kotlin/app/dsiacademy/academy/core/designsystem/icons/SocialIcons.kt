package app.dsiacademy.academy.core.designsystem.icons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object SocialIcons {
    val Facebook: ImageVector by lazy {
        ImageVector.Builder("Facebook", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.White), pathFillType = PathFillType.NonZero) {
                moveTo(14f, 13.5f)
                horizontalLineToRelative(2.5f)
                lineToRelative(0.5f, -3f)
                horizontalLineTo(14f)
                verticalLineTo(8.5f)
                curveToRelative(0f, -0.9f, 0.1f, -1.5f, 1.5f, -1.5f)
                horizontalLineTo(17f)
                verticalLineTo(4.1f)
                curveToRelative(-0.3f, 0f, -1.2f, -0.1f, -2.3f, -0.1f)
                curveTo(12.1f, 4f, 10.5f, 5.4f, 10.5f, 8.1f)
                verticalLineTo(10.5f)
                horizontalLineTo(8f)
                verticalLineToRelative(3f)
                horizontalLineToRelative(2.5f)
                verticalLineTo(20f)
                horizontalLineToRelative(3.5f)
                verticalLineTo(13.5f)
                close()
            }
        }.build()
    }

    val WhatsApp: ImageVector by lazy {
        ImageVector.Builder("WhatsApp", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12.04f, 2f)
                curveTo(6.58f, 2f, 2.13f, 6.45f, 2.13f, 11.91f)
                curveToRelative(0f, 1.89f, 0.49f, 3.72f, 1.43f, 5.34f)
                lineTo(2f, 22f)
                lineToRelative(4.89f, -1.52f)
                curveToRelative(1.56f, 0.85f, 3.32f, 1.3f, 5.15f, 1.3f)
                horizontalLineToRelative(0f)
                curveToRelative(5.46f, 0f, 9.91f, -4.45f, 9.91f, -9.91f)
                curveTo(21.95f, 6.45f, 17.5f, 2f, 12.04f, 2f)
                close()
                moveTo(12.04f, 20.15f)
                curveToRelative(-1.56f, 0f, -3.09f, -0.42f, -4.42f, -1.21f)
                lineToRelative(-0.32f, -0.19f)
                lineToRelative(-2.9f, 0.9f)
                lineToRelative(0.92f, -2.83f)
                lineToRelative(-0.2f, -0.33f)
                curveToRelative(-0.87f, -1.39f, -1.33f, -2.99f, -1.33f, -4.64f)
                curveToRelative(0f, -4.54f, 3.7f, -8.24f, 8.25f, -8.24f)
                reflectiveCurveToRelative(8.24f, 3.7f, 8.24f, 8.24f)
                reflectiveCurveToRelative(-3.69f, 8.3f, -8.24f, 8.3f)
                close()
            }
        }.build()
    }

    val TikTok: ImageVector by lazy {
        ImageVector.Builder("TikTok", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(19.59f, 6.69f)
                curveToRelative(-0.83f, -0.34f, -1.5f, -0.95f, -1.91f, -1.74f)
                curveToRelative(-0.23f, -0.45f, -0.36f, -0.95f, -0.36f, -1.47f)
                horizontalLineTo(14.3f)
                verticalLineToRelative(11.4f)
                curveToRelative(0f, 1.45f, -1.18f, 2.63f, -2.63f, 2.63f)
                reflectiveCurveToRelative(-2.63f, -1.18f, -2.63f, -2.63f)
                reflectiveCurveToRelative(1.18f, -2.63f, 2.63f, -2.63f)
                curveToRelative(0.28f, 0f, 0.55f, 0.04f, 0.81f, 0.13f)
                verticalLineTo(9.95f)
                curveToRelative(-0.27f, -0.04f, -0.54f, -0.06f, -0.81f, -0.06f)
                curveToRelative(-3.03f, 0f, -5.5f, 2.46f, -5.5f, 5.5f)
                reflectiveCurveToRelative(2.46f, 5.5f, 5.5f, 5.5f)
                reflectiveCurveToRelative(5.5f, -2.46f, 5.5f, -5.5f)
                verticalLineTo(8.72f)
                curveToRelative(1.05f, 0.76f, 2.35f, 1.21f, 3.75f, 1.21f)
                verticalLineTo(6.91f)
                curveToRelative(-0.45f, 0f, -0.88f, -0.08f, -1.28f, -0.22f)
                close()
            }
        }.build()
    }

    val Telegram: ImageVector by lazy {
        ImageVector.Builder("Telegram", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(9.78f, 14.65f)
                lineToRelative(-0.38f, 5.27f)
                curveToRelative(0.54f, 0f, 0.78f, -0.23f, 1.06f, -0.5f)
                lineToRelative(2.55f, -2.42f)
                lineToRelative(5.29f, 3.87f)
                curveToRelative(0.97f, 0.53f, 1.66f, 0.25f, 1.92f, -0.9f)
                lineToRelative(3.48f, -16.26f)
                lineToRelative(0.01f, -0.01f)
                curveToRelative(0.31f, -1.44f, -0.52f, -2.01f, -1.47f, -1.66f)
                lineTo(1.3f, 9.77f)
                curveToRelative(-1.4f, 0.54f, -1.38f, 1.31f, -0.24f, 1.66f)
                lineToRelative(5.45f, 1.7f)
                lineToRelative(12.65f, -7.96f)
                curveToRelative(0.6f, -0.36f, 1.14f, -0.16f, 0.69f, 0.2f)
                close()
            }
        }.build()
    }
}

@Composable
fun SocialIcon(id: String, enabled: Boolean, modifier: Modifier = Modifier) {
    val icon = when (id) {
        "facebook" -> SocialIcons.Facebook
        "whatsapp" -> SocialIcons.WhatsApp
        "tiktok" -> SocialIcons.TikTok
        "telegram" -> SocialIcons.Telegram
        else -> null
    }
    val tint = if (enabled) Color(0xFF475569) else Color(0xFF94A3B8).copy(alpha = 0.45f)
    if (icon != null) {
        Icon(icon, contentDescription = id, tint = tint, modifier = modifier.size(18.dp))
    }
}
