package app.dsiacademy.academy.feature.browser

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.webkit.WebView
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.common.LinkDecision
import app.dsiacademy.academy.core.common.PaymentReturnStatus
import app.dsiacademy.academy.core.designsystem.theme.DsiTheme

@Composable
fun BrowserScreen(
    onClose: () -> Unit,
    onPaymentReturn: (PaymentReturnStatus, String?) -> Unit,
    viewModel: BrowserViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BrowserEvent.Close -> onClose()
                is BrowserEvent.PaymentReturned ->
                    onPaymentReturn(event.status, event.trackingToken)
            }
        }
    }

    when (state.phase) {
        BrowserPhase.NotConfigured -> {
            BrowserMessageView(
                title = stringResource(R.string.browser_not_configured_title),
                body = stringResource(R.string.browser_not_configured_body),
                onClose = { viewModel.onCloseRequested() },
            )
        }
        BrowserPhase.UnknownLink -> {
            BrowserMessageView(
                title = stringResource(R.string.browser_not_configured_title),
                body = stringResource(R.string.browser_unknown_link_body),
                onClose = { viewModel.onCloseRequested() },
            )
        }
        BrowserPhase.Ready -> {
            BrowserReadyContent(
                state = state,
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun BrowserReadyContent(
    state: BrowserUiState,
    viewModel: BrowserViewModel,
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    BackHandler {
        val wv = webViewRef
        if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else {
            viewModel.onCloseRequested()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DsiTheme.colors.bgApp)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        BrowserTopBar(
            title = state.title,
            host = state.host,
            isSecure = state.isSecure,
            onClose = { viewModel.onCloseRequested() },
            onReload = { webViewRef?.reload() },
        )

        if (state.loading) {
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.fillMaxWidth(),
                color = DsiTheme.colors.goldPrestige,
                trackColor = DsiTheme.colors.borderLight,
            )
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding(),
        ) {
            BrowserWebView(
                startUrl = state.startUrl,
                onWebViewCreated = { webViewRef = it },
                onPageStarted = { viewModel.onPageStarted(it) },
                onProgressChanged = { viewModel.onProgressChanged(it) },
                onTitleReceived = { viewModel.onTitleReceived(it) },
                onPageFinished = { url, canGoBack ->
                    viewModel.onPageFinished(url, canGoBack)
                },
                onMainFrameError = { viewModel.onMainFrameError() },
                onNavigationRequest = { url ->
                    val decision = viewModel.onNavigationRequest(url)
                    when (decision) {
                        is LinkDecision.Allow -> false
                        is LinkDecision.OpenExternal -> {
                            openExternal(context, url, viewModel)
                            true
                        }
                        is LinkDecision.Block -> true
                        is LinkDecision.PaymentReturn -> true
                    }
                },
            )

            if (state.hasError) {
                BrowserErrorView(
                    onRetry = {
                        viewModel.onRetry()
                        webViewRef?.reload()
                    },
                )
            }
        }
    }
}
