package app.dsiacademy.academy.feature.member.pendingpayment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.designsystem.theme.DsiTheme

@Composable
fun PendingPaymentScreen(
    onPaid: () -> Unit,
    onBack: () -> Unit,
    viewModel: PendingPaymentViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PendingPaymentEvent.OpenPaymentUrl -> {
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(event.url)),
                        )
                    } catch (_: ActivityNotFoundException) {
                        // no-op : pas d'app pour ouvrir l'URL
                    }
                }
                is PendingPaymentEvent.Paid -> onPaid()
            }
        }
    }

    when (state.phase) {
        PendingPaymentPhase.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DsiTheme.colors.bgApp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = DsiTheme.colors.goldPrestige)
            }
        }
        PendingPaymentPhase.NoPending -> {
            LaunchedEffect(Unit) { onBack() }
        }
        PendingPaymentPhase.Ready -> {
            PendingPaymentContent(
                checking = state.checking,
                message = state.message,
                onOpenPayment = { viewModel.onOpenPaymentClicked() },
                onVerify = { viewModel.verifierPaiement() },
                onBack = onBack,
            )
        }
    }
}

@Composable
fun PendingPaymentContent(
    checking: Boolean,
    message: PendingPaymentMessage,
    onOpenPayment: () -> Unit,
    onVerify: () -> Unit,
    onBack: () -> Unit,
) {
    val colors = DsiTheme.colors
    val spacing = DsiTheme.spacing
    val typography = DsiTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgApp)
            .padding(spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(spacing.xxxl))
        Text(
            text = stringResource(R.string.register_payment_title),
            style = typography.titleMedium,
            color = colors.textPrimary,
        )
        Spacer(modifier = Modifier.height(spacing.md))
        Text(
            text = stringResource(R.string.register_payment_hint),
            style = typography.bodyMedium,
            color = colors.textSecondary,
        )
        Spacer(modifier = Modifier.height(spacing.xxl))

        Button(
            onClick = onOpenPayment,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.goldPrestige,
                contentColor = colors.textOnDark,
            ),
        ) {
            Text(stringResource(R.string.register_payment_open))
        }

        Spacer(modifier = Modifier.height(spacing.md))

        OutlinedButton(
            onClick = onVerify,
            enabled = !checking,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (checking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = colors.goldPrestige,
                )
            } else {
                Text(stringResource(R.string.register_payment_check))
            }
        }

        when (message) {
            PendingPaymentMessage.None -> Unit
            PendingPaymentMessage.Waiting -> {
                Spacer(modifier = Modifier.height(spacing.lg))
                Text(
                    text = stringResource(R.string.register_payment_pending),
                    style = typography.bodySmall,
                    color = colors.warning,
                )
            }
            PendingPaymentMessage.Refused -> {
                Spacer(modifier = Modifier.height(spacing.lg))
                Text(
                    text = stringResource(R.string.register_payment_failed),
                    style = typography.bodySmall,
                    color = colors.error,
                )
            }
            PendingPaymentMessage.Unavailable -> {
                Spacer(modifier = Modifier.height(spacing.lg))
                Text(
                    text = stringResource(R.string.payment_check_unavailable),
                    style = typography.bodySmall,
                    color = colors.error,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onBack) {
            Text(
                text = stringResource(R.string.browser_close),
                color = colors.textMuted,
            )
        }
    }
}

@Preview(name = "Pending — clair", showBackground = true)
@Composable
private fun PendingPaymentContentPreviewLight() {
    DsiTheme(darkTheme = false) {
        PendingPaymentContent(
            checking = false,
            message = PendingPaymentMessage.Waiting,
            onOpenPayment = {},
            onVerify = {},
            onBack = {},
        )
    }
}

@Preview(name = "Pending — sombre", showBackground = true)
@Composable
private fun PendingPaymentContentPreviewDark() {
    DsiTheme(darkTheme = true) {
        PendingPaymentContent(
            checking = true,
            message = PendingPaymentMessage.Refused,
            onOpenPayment = {},
            onVerify = {},
            onBack = {},
        )
    }
}
