package app.dsiacademy.academy.core.designsystem.icons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Icônes filaires alignées sur NavIcons.jsx + RoomIcons.jsx du site
 * (viewBox 0 0 24 24, stroke 1.8, round caps).
 */
object DsiIcons {

    val Community: ImageVector by lazy {
        ImageVector.Builder(24, 24, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                // circle cx=9 cy=8 r=3.5
                moveTo(12.5f, 8f)
                arcToRelative(3.5f, 3.5f, 0f, true, true, -7f, 0f)
                arcToRelative(3.5f, 3.5f, 0f, true, true, 7f, 0f)
                // path M2.5 20c0-3.6 2.9-6 6.5-6s6.5 2.4 6.5 6
                moveTo(2.5f, 20f)
                curveToRelative(0f, -3.6f, 2.9f, -6f, 6.5f, -6f)
                reflectiveCurveToRelative(6.5f, 2.4f, 6.5f, 6f)
                // circle cx=17.2 cy=9 r=2.6
                moveTo(19.8f, 9f)
                arcToRelative(2.6f, 2.6f, 0f, true, true, -5.2f, 0f)
                arcToRelative(2.6f, 2.6f, 0f, true, true, 5.2f, 0f)
                // path M17.2 14.2c2.6.2 4.3 2 4.3 5.3
                moveTo(17.2f, 14.2f)
                curveToRelative(2.6f, 0.2f, 4.3f, 2f, 4.3f, 5.3f)
            }
        }.build()
    }

    val Academy: ImageVector by lazy {
        ImageVector.Builder(24, 24, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(12f, 3.5f)
                lineTo(3f, 8.5f)
                horizontalLineToRelative(18f)
                lineTo(12f, 3.5f)
                close()
                moveTo(6f, 11.5f)
                verticalLineToRelative(6f)
                moveTo(10f, 11.5f)
                verticalLineToRelative(6f)
                moveTo(14f, 11.5f)
                verticalLineToRelative(6f)
                moveTo(18f, 11.5f)
                verticalLineToRelative(6f)
                moveTo(3.5f, 20.5f)
                horizontalLineToRelative(17f)
            }
        }.build()
    }

    val Shop: ImageVector by lazy {
        ImageVector.Builder(24, 24, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(5f, 8f)
                horizontalLineToRelative(14f)
                lineToRelative(-1f, 12f)
                horizontalLineTo(6f)
                lineTo(5f, 8f)
                close()
                moveTo(9f, 8f)
                verticalLineTo(6.5f)
                arcToRelative(3f, 3f, 0f, false, true, 6f, 0f)
                verticalLineTo(8f)
            }
        }.build()
    }

    val Marketplace: ImageVector by lazy {
        ImageVector.Builder(24, 24, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(21f, 12f)
                arcToRelative(9f, 9f, 0f, true, true, -18f, 0f)
                arcToRelative(9f, 9f, 0f, true, true, 18f, 0f)
                moveTo(3f, 12f)
                horizontalLineToRelative(18f)
                moveTo(12f, 3f)
                curveToRelative(2.6f, 2.4f, 4f, 5.6f, 4f, 9f)
                reflectiveCurveToRelative(-1.4f, 6.6f, -4f, 9f)
                curveToRelative(-2.6f, -2.4f, -4f, -5.6f, -4f, -9f)
                reflectiveCurveToRelative(1.4f, -6.6f, 4f, -9f)
                close()
            }
        }.build()
    }

    val Home: ImageVector by lazy {
        ImageVector.Builder(24, 24, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                // rect 3.5 3.5 17 17 rx 3
                moveTo(6.5f, 3.5f)
                horizontalLineToRelative(11f)
                arcToRelative(3f, 3f, 0f, false, true, 3f, 3f)
                verticalLineToRelative(11f)
                arcToRelative(3f, 3f, 0f, false, true, -3f, 3f)
                horizontalLineToRelative(-11f)
                arcToRelative(3f, 3f, 0f, false, true, -3f, -3f)
                verticalLineToRelative(-11f)
                arcToRelative(3f, 3f, 0f, false, true, 3f, -3f)
                close()
                moveTo(8f, 16f)
                verticalLineToRelative(-4f)
                moveTo(12f, 16f)
                verticalLineTo(8f)
                moveTo(16f, 16f)
                verticalLineToRelative(-6f)
            }
        }.build()
    }

    val Subscription: ImageVector by lazy {
        ImageVector.Builder(24, 24, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(5.5f, 5.5f)
                horizontalLineToRelative(13f)
                arcToRelative(2.5f, 2.5f, 0f, false, true, 2.5f, 2.5f)
                verticalLineToRelative(8f)
                arcToRelative(2.5f, 2.5f, 0f, false, true, -2.5f, 2.5f)
                horizontalLineToRelative(-13f)
                arcToRelative(2.5f, 2.5f, 0f, false, true, -2.5f, -2.5f)
                verticalLineToRelative(-8f)
                arcToRelative(2.5f, 2.5f, 0f, false, true, 2.5f, -2.5f)
                close()
                moveTo(3f, 10f)
                horizontalLineToRelative(18f)
                moveTo(7f, 15f)
                horizontalLineToRelative(3f)
            }
        }.build()
    }

    val DsiHoldings: ImageVector by lazy {
        ImageVector.Builder(24, 24, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(20.5f, 12f)
                arcToRelative(8.5f, 8.5f, 0f, true, true, -17f, 0f)
                arcToRelative(8.5f, 8.5f, 0f, true, true, 17f, 0f)
                moveTo(17f, 12f)
                arcToRelative(5f, 5f, 0f, true, true, -10f, 0f)
                arcToRelative(5f, 5f, 0f, true, true, 10f, 0f)
                moveTo(12f, 9.5f)
                verticalLineToRelative(5f)
            }
        }.build()
    }

    val Withdrawals: ImageVector by lazy {
        ImageVector.Builder(24, 24, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(12f, 15f)
                verticalLineTo(4f)
                moveTo(8f, 8f)
                lineToRelative(4f, -4f)
                lineToRelative(4f, 4f)
                moveTo(4.5f, 15f)
                verticalLineToRelative(3f)
                arcToRelative(2f, 2f, 0f, false, false, 2f, 2f)
                horizontalLineToRelative(11f)
                arcToRelative(2f, 2f, 0f, false, false, 2f, -2f)
                verticalLineToRelative(-3f)
            }
        }.build()
    }

    val Payments: ImageVector by lazy {
        ImageVector.Builder(24, 24, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(6f, 3.5f)
                horizontalLineToRelative(12f)
                verticalLineToRelative(17f)
                lineToRelative(-2.5f, -1.7f)
                lineToRelative(-2f, 1.7f)
                lineToRelative(-1.5f, -1.7f)
                lineToRelative(-1.5f, 1.7f)
                lineToRelative(-2f, -1.7f)
                lineTo(6f, 20.5f)
                verticalLineTo(3.5f)
                close()
                moveTo(9f, 8.5f)
                horizontalLineToRelative(6f)
                moveTo(9f, 12f)
                horizontalLineToRelative(6f)
            }
        }.build()
    }

    val Referral: ImageVector by lazy {
        ImageVector.Builder(24, 24, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(13f, 8f)
                arcToRelative(3.5f, 3.5f, 0f, true, true, -7f, 0f)
                arcToRelative(3.5f, 3.5f, 0f, true, true, 7f, 0f)
                moveTo(3f, 20f)
                curveToRelative(0f, -3.4f, 2.9f, -5.5f, 6.5f, -5.5f)
                curveToRelative(1.3f, 0f, 2.5f, 0.3f, 3.5f, 0.9f)
                moveTo(18f, 14f)
                verticalLineToRelative(6f)
                moveTo(15f, 17f)
                horizontalLineToRelative(6f)
            }
        }.build()
    }

    val Notifications: ImageVector by lazy {
        ImageVector.Builder(24, 24, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(6f, 16.5f)
                verticalLineTo(11f)
                arcToRelative(6f, 6f, 0f, true, true, 12f, 0f)
                verticalLineToRelative(5.5f)
                lineToRelative(1.5f, 2f)
                horizontalLineToRelative(-15f)
                lineToRelative(1.5f, -2f)
                close()
                moveTo(10f, 20.5f)
                arcToRelative(2.2f, 2.2f, 0f, false, false, 4f, 0f)
            }
        }.build()
    }

    val Profile: ImageVector by lazy {
        ImageVector.Builder(24, 24, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(15.8f, 8f)
                arcToRelative(3.8f, 3.8f, 0f, true, true, -7.6f, 0f)
                arcToRelative(3.8f, 3.8f, 0f, true, true, 7.6f, 0f)
                moveTo(4.5f, 20.5f)
                curveToRelative(0f, -4f, 3.4f, -6.5f, 7.5f, -6.5f)
                reflectiveCurveToRelative(7.5f, 2.5f, 7.5f, 6.5f)
            }
        }.build()
    }

    val Settings: ImageVector by lazy {
        ImageVector.Builder(24, 24, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(4f, 7f)
                horizontalLineToRelative(9f)
                moveTo(17f, 7f)
                horizontalLineToRelative(3f)
                moveTo(4f, 17f)
                horizontalLineToRelative(3f)
                moveTo(11f, 17f)
                horizontalLineToRelative(9f)
                moveTo(17f, 7f)
                arcToRelative(2f, 2f, 0f, true, true, -4f, 0f)
                arcToRelative(2f, 2f, 0f, true, true, 4f, 0f)
                moveTo(11f, 17f)
                arcToRelative(2f, 2f, 0f, true, true, -4f, 0f)
                arcToRelative(2f, 2f, 0f, true, true, 4f, 0f)
            }
        }.build()
    }

    val Logout: ImageVector by lazy {
        ImageVector.Builder(24, 24, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(10f, 4.5f)
                horizontalLineTo(6.5f)
                arcToRelative(2f, 2f, 0f, false, false, -2f, 2f)
                verticalLineToRelative(11f)
                arcToRelative(2f, 2f, 0f, false, false, 2f, 2f)
                horizontalLineTo(10f)
                moveTo(15f, 8f)
                lineToRelative(4f, 4f)
                lineToRelative(-4f, 4f)
                moveTo(19f, 12f)
                horizontalLineTo(9.5f)
            }
        }.build()
    }

    fun navIcon(id: String): ImageVector? = when (id) {
        "community" -> Community
        "academy" -> Academy
        "shop" -> Shop
        "marketplace" -> Marketplace
        else -> null
    }

    fun roomIcon(id: String): ImageVector? = when (id) {
        "home" -> Home
        "subscription" -> Subscription
        "dsi-holdings" -> DsiHoldings
        "withdrawals" -> Withdrawals
        "payments" -> Payments
        "referral" -> Referral
        "notifications" -> Notifications
        "profile" -> Profile
        "settings" -> Settings
        else -> null
    }
}

@Composable
fun DsiIcon(
    imageVector: ImageVector,
    contentDescription: String? = null,
    tint: Color,
    modifier: Modifier = Modifier.size(22.dp),
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier,
    )
}
