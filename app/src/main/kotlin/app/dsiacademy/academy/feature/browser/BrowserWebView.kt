package app.dsiacademy.academy.feature.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * WebView du navigateur intégré.
 *
 * La sauvegarde d'état passe par un [Saver] appelé quand Android sauvegarde
 * l'état de l'écran (avant rotation), et non dans onDispose.
 */
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

    // Référence vivante au WebView, lue par le Saver au moment de la sauvegarde Android.
    val webViewHolder = remember { arrayOfNulls<WebView>(1) }

    val webViewStateSaver = remember {
        Saver<Bundle, Bundle>(
            save = {
                Bundle().also { bundle ->
                    webViewHolder[0]?.saveState(bundle)
                }
            },
            restore = { it },
        )
    }

    var restoredBundle by rememberSaveable(stateSaver = webViewStateSaver) {
        mutableStateOf(Bundle())
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
        webViewHolder[0] = webView
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
            webViewHolder[0] = null
            webView.destroy()
        }
    }

    AndroidView(
        factory = {
            onWebViewCreated(webView)
            val bundle = restoredBundle
            if (!bundle.isEmpty) {
                webView.restoreState(bundle)
            } else if (!hasLoadedInitial && !startUrl.isNullOrBlank()) {
                hasLoadedInitial = true
                webView.loadUrl(startUrl)
            }
            webView
        },
        modifier = Modifier.fillMaxSize(),
    )
}
