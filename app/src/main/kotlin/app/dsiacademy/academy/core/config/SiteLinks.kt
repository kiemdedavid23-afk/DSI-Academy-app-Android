package app.dsiacademy.academy.core.config

import app.dsiacademy.academy.BuildConfig
import java.net.URI

/**
 * Liens du site ouverts dans le navigateur intégré.
 * Aucune adresse en dur : baseUrl vient de BuildConfig.SITE_URL.
 */
enum class SiteLink(val id: String, val path: String) {
    INSCRIPTION("inscription", "/inscription"),
}

object SiteLinks {
    val baseUrl: String = BuildConfig.SITE_URL

    val isConfigured: Boolean
        get() = baseUrl.isNotBlank()

    /**
     * Hôtes extraits des href non nuls de [SocialLinks].
     */
    val externalHosts: Set<String> by lazy {
        SocialLinks.all
            .mapNotNull { it.href }
            .mapNotNull { href ->
                try {
                    URI(href).host?.lowercase()?.takeIf { it.isNotBlank() }
                } catch (_: Exception) {
                    null
                }
            }
            .toSet()
    }

    fun find(id: String): SiteLink? =
        SiteLink.entries.find { it.id == id }
}