package app.dsiacademy.academy.feature.browser

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    startUrl: String?,
    onWebViewCreated: (WebView) -> Unit,
    onPageStarted: (String) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onTitleReceived: (String) -> Unit,
    onPageFinished: (url: String, canGoBack: Boolean) -> Unit,
    onMainFrameError: () -> Unit,
    onNavigationRequest: (String) -> Boolean,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val webViewStateSaver = remember {
        Saver<Bundle?, Bundle>(
            save = { it ?: Bundle() },
            restore = { it },
        )
    }
    var savedState by rememberSaveable(stateSaver = webViewStateSaver) {
        mutableStateOf<Bundle?>(null)
    }
    var hasLoadedInitial by rememberSaveable { mutableStateOf(false) }

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = false
                allowContentAccess = false
                allowFileAccessFromFileURLs = false
                allowUniversalAccessFromFileURLs = false
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                setSupportMultipleWindows(false)
                javaScriptCanOpenWindowsAutomatically = false
            }
            // No addJavascriptInterface
            WebView.setWebContentsDebuggingEnabled(false)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest,
                ): Boolean {
                    if (!request.isForMainFrame) return false
                    val url = request.url?.toString() ?: return true
                    return onNavigationRequest(url)
                }

                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    return onNavigationRequest(url)
                }

                override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                    if (url != null) onPageStarted(url)
                }

                override fun onPageFinished(view: WebView, url: String?) {
                    if (url != null) onPageFinished(url, view.canGoBack())
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError,
                ) {
                    if (request.isForMainFrame) onMainFrameError()
                }

                override fun onReceivedHttpError(
                    view: WebView,
                    request: WebResourceRequest,
                    errorResponse: WebResourceResponse,
                ) {
                    if (request.isForMainFrame && errorResponse.statusCode >= 500) {
                        onMainFrameError()
                    }
                }

                override fun onReceivedSslError(
                    view: WebView,
                    handler: SslErrorHandler,
                    error: android.net.http.SslError,
                ) {
                    handler.cancel()
                    onMainFrameError()
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    onProgressChanged(newProgress)
                }

                override fun onReceivedTitle(view: WebView, title: String?) {
                    onTitleReceived(title.orEmpty())
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> webView.onResume()
                Lifecycle.Event.ON_PAUSE -> webView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            webView.stopLoading()
            (webView.parent as? ViewGroup)?.removeView(webView)
            val bundle = Bundle()
            webView.saveState(bundle)
            savedState = bundle
            webView.destroy()
        }
    }

    AndroidView(
        factory = {
            onWebViewCreated(webView)
            val restored = savedState
            if (restored != null && !restored.isEmpty) {
                webView.restoreState(restored)
            } else if (!hasLoadedInitial && !startUrl.isNullOrBlank()) {
                hasLoadedInitial = true
                webView.loadUrl(startUrl)
            }
            webView
        },
        modifier = Modifier.fillMaxSize(),
    )
}

private fun openExternal(
    context: android.content.Context,
    url: String,
    viewModel: BrowserViewModel,
) {
    try {
        if (url.startsWith("intent:", ignoreCase = true)) {
            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME).apply {
                component = null
                selector = null
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                val fallback = intent.getStringExtra("browser_fallback_url")
                if (!fallback.isNullOrBlank()) {
                    when (val decision = viewModel.onNavigationRequest(fallback)) {
                        is LinkDecision.Allow -> {
                            // Cannot load in this helper; screen handles via decision return.
                            // Fallback already evaluated — open as VIEW if Allow-like.
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(fallback))
                                )
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.browser_no_app_found),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                        is LinkDecision.OpenExternal -> {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(fallback))
                                )
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.browser_no_app_found),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                        else -> {
                            Toast.makeText(
                                context,
                                context.getString(R.string.browser_no_app_found),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.browser_no_app_found),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        } else {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            context.getString(R.string.browser_no_app_found),
            Toast.LENGTH_SHORT,
