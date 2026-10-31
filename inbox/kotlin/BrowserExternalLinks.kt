package app.dsiacademy.academy.feature.browser

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import app.dsiacademy.academy.R
import app.dsiacademy.academy.core.common.LinkDecision

/**
 * Ouvre une URL hors de l'app (navigateur, tel, intent:…).
 * Aucune modification du manifeste (pas de resolveActivity).
 */
fun openExternal(
    context: Context,
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
                    when (viewModel.onNavigationRequest(fallback)) {
                        is LinkDecision.Allow,
                        is LinkDecision.OpenExternal,
                        -> {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(fallback)),
                                )
                            } catch (_: ActivityNotFoundException) {
                                showNoAppToast(context)
                            }
                        }
                        else -> showNoAppToast(context)
                    }
                } else {
                    showNoAppToast(context)
                }
            }
        } else {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    } catch (_: ActivityNotFoundException) {
        showNoAppToast(context)
    } catch (_: Exception) {
        showNoAppToast(context)
    }
}

private fun showNoAppToast(context: Context) {
    Toast.makeText(
        context,
        context.getString(R.string.browser_no_app_found),
        Toast.LENGTH_SHORT,
    ).show()
}
