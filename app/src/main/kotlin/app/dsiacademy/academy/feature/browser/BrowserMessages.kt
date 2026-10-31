package app.dsiacademy.academy.feature.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.designsystem.theme.DsiTheme

@Composable
fun BrowserMessageView(
    title: String,
    body: String,
    onClose: () -> Unit,
) {
    val colors = DsiTheme.colors
    val spacing = DsiTheme.spacing
    val typography = DsiTheme.typography

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgApp)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(spacing.xxl),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = typography.titleMedium,
                color = colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(spacing.md))
            Text(
                text = body,
                style = typography.bodyMedium,
                color = colors.textSecondary,
            )
            Spacer(modifier = Modifier.height(spacing.xxl))
            TextButton(onClick = onClose) {
                Text(
                    text = stringResource(R.string.browser_close),
                    color = colors.goldPrestige,
                )
            }
        }
    }
}

@Composable
fun BrowserErrorView(
    onRetry: () -> Unit,
) {
    val colors = DsiTheme.colors
    val spacing = DsiTheme.spacing
    val typography = DsiTheme.typography

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgApp.copy(alpha = 0.96f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(spacing.xxl),
        ) {
            Text(
                text = stringResource(R.string.error_network),
                style = typography.bodyMedium,
                color = colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(spacing.lg))
            TextButton(onClick = onRetry) {
                Text(
                    text = stringResource(R.string.retry),
                    color = colors.goldPrestige,
                )
            }
        }
    }
}

@Preview(name = "Message — clair", showBackground = true)
@Composable
private fun BrowserMessageViewPreviewLight() {
    DsiTheme(darkTheme = false) {
        BrowserMessageView(
            title = "Page indisponible pour le moment",
            body = "Cette fonctionnalité sera bientôt disponible.",
            onClose = {},
        )
    }
}

@Preview(name = "Message — sombre", showBackground = true)
@Composable
private fun BrowserMessageViewPreviewDark() {
    DsiTheme(darkTheme = true) {
        BrowserMessageView(
            title = "Page indisponible pour le moment",
            body = "Cette fonctionnalité sera bientôt disponible.",
            onClose = {},
        )
    }
}

@Preview(name = "Erreur — clair", showBackground = true)
@Composable
private fun BrowserErrorViewPreviewLight() {
    DsiTheme(darkTheme = false) {
        BrowserErrorView(onRetry = {})
    }
}

@Preview(name = "Erreur — sombre", showBackground = true)
@Composable
private fun BrowserErrorViewPreviewDark() {
    DsiTheme(darkTheme = true) {
        BrowserErrorView(onRetry = {})
    }
}
