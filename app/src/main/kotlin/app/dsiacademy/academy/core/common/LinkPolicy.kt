package app.dsiacademy.academy.core.common

import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Politique de navigation pour le navigateur intégré (WebView).
 * Kotlin pur : aucun import Android.
 */
sealed interface LinkDecision {
    data object Allow : LinkDecision
    data class OpenExternal(val url: String) : LinkDecision
    data object Block : LinkDecision
    data class PaymentReturn(
        val status: PaymentReturnStatus,
        val trackingToken: String?,
    ) : LinkDecision
}

enum class PaymentReturnStatus {
    Success,
    Failed,
    Unknown,
}

class LinkPolicy(
    private val siteUrl: String,
    private val externalHosts: Set<String>,
) {
    val isConfigured: Boolean
        get() = siteUrl.isNotBlank()

    /**
     * Construit l'URL https complète du site pour le chemin donné.
     * @return null si le site n'est pas configuré.
     */
    fun startUrl(path: String, query: Map<String, String> = emptyMap()): String? {
        if (!isConfigured) return null
        val base = siteUrl.trimEnd('/')
        val cleanPath = when {
            path.isEmpty() -> ""
            path.startsWith("/") -> path
            else -> "/$path"
        }
        if (query.isEmpty()) {
            return base + cleanPath
        }
        val queryString = query.entries.joinToString("&") { (key, value) ->
            val encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.name())
            val encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8.name())
            "$encodedKey=$encodedValue"
        }
        return "$base$cleanPath?$queryString"
    }

    fun decide(url: String): LinkDecision {
        // a. URL vide ou illisible
        if (url.isBlank()) return LinkDecision.Block
        val uri = try {
            URI(url.trim())
        } catch (_: Exception) {
            return LinkDecision.Block
        }
        val scheme = (uri.scheme ?: "").lowercase()

        // b. Schémas interdits
        val blockedSchemes = setOf(
            "javascript", "file", "data", "content", "blob", "about", "http"
        )
        if (scheme in blockedSchemes) return LinkDecision.Block

        // c. Schéma autre que https → OpenExternal
        if (scheme != "https") {
            return LinkDecision.OpenExternal(url.trim())
        }

        // d. Site non configuré
        if (!isConfigured) return LinkDecision.Block

        val host = normalizeHost(uri.host ?: "")
        if (host.isEmpty()) return LinkDecision.Block

        val siteHost = normalizeHost(URI(siteUrl).host ?: "")
        val path = normalizePath(uri.path ?: "")

        // e. Retour de paiement
        if (host == siteHost && path == "/paiement-retour") {
            val statusRaw = (
                queryParam(uri, "status") ?: queryParam(uri, "etat") ?: ""
            ).lowercase()
            val status = when (statusRaw) {
                "success", "ok", "paid" -> PaymentReturnStatus.Success
                "failed", "error", "cancel" -> PaymentReturnStatus.Failed
                else -> PaymentReturnStatus.Unknown
            }
            val token = queryParam(uri, "token")
                ?: queryParam(uri, "paymentTrackingToken")
            val trackingToken = token?.takeIf { it.isNotBlank() }
            return LinkDecision.PaymentReturn(status, trackingToken)
        }

        // f. Hôte externe (égal ou sous-domaine)
        if (isExternalHost(host)) {
            return LinkDecision.OpenExternal(url.trim())
        }

        // g. Tout autre https → Allow
        return LinkDecision.Allow
    }

    private fun normalizeHost(host: String): String {
        val lower = host.lowercase()
        return if (lower.startsWith("www.")) lower.removePrefix("www.") else lower
    }

    private fun normalizePath(path: String): String {
        if (path.isEmpty() || path == "/") return path
        return path.trimEnd('/')
    }

    private fun isExternalHost(host: String): Boolean {
        val normalizedExternal = externalHosts.map { normalizeHost(it) }.toSet()
        return normalizedExternal.any { external ->
            host == external || host.endsWith(".$external")
        }
    }

    private fun queryParam(uri: URI, name: String): String? {
        val raw = uri.rawQuery ?: return null
        return raw.split("&")
            .mapNotNull { pair ->
                val parts = pair.split("=", limit = 2)
                if (parts.size == 2 && parts[0].equals(name, ignoreCase = true)) {
                    // Décodage minimal (les espaces + et %xx)
                    try {
                        java.net.URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name())
                    } catch (_: Exception) {
                        parts[1]
                    }
                } else {
                    null
                }
            }
            .firstOrNull()
    }
}