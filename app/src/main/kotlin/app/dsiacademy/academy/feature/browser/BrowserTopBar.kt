package app.dsiacademy.academy.feature.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.designsystem.theme.DsiTheme

@Composable
fun BrowserTopBar(
    title: String,
    host: String,
    isSecure: Boolean,
    onClose: () -> Unit,
    onReload: () -> Unit,
) {
    val colors = DsiTheme.colors
    val spacing = DsiTheme.spacing
    val typography = DsiTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgSurface)
            .padding(horizontal = spacing.xs, vertical = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.browser_close),
                tint = colors.textPrimary,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title.ifBlank { host },
                style = typography.bodyMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (host.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSecure) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = stringResource(R.string.browser_secure_connection),
                            tint = colors.textMuted,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = host,
                        style = typography.labelSmall,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        IconButton(onClick = onReload) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = stringResource(R.string.browser_reload),
                tint = colors.textPrimary,
            )
        }
    }
}

@Preview(name = "TopBar — clair", showBackground = true)
@Composable
private fun BrowserTopBarPreviewLight() {
    DsiTheme(darkTheme = false) {
        BrowserTopBar(
            title = "Inscription DSI Academy",
            host = "exemple.test",
            isSecure = true,
            onClose = {},
            onReload = {},
        )
    }
}

@Preview(name = "TopBar — sombre", showBackground = true)
@Composable
private fun BrowserTopBarPreviewDark() {
    DsiTheme(darkTheme = true) {
        BrowserTopBar(
            title = "Inscription DSI Academy",
            host = "exemple.test",
            isSecure = true,
            onClose = {},
            onReload = {},
        )
    }
}
