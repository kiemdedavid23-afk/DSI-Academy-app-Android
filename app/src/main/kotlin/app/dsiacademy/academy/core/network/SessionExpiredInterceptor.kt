package app.dsiacademy.academy.core.network

import app.dsiacademy.academy.core.session.SessionStore
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Intercepteur global de fin de session.
 *
 * Déclenche la purge + événement SessionEvent.Expired lorsque :
 * - HTTP 401 sur une route authentifiée, OU
 * - corps JSON contenant un code d'expiration de session
 *   (SESSION_EXPIRED, TOKEN_EXPIRED, UNAUTHORIZED… — voir CONTRATS-API §1.5 / §1.6)
 *
 * Aucun écran ni Repository ne teste un 401 (ARCHITECTURE.md §5).
 */
@Singleton
class SessionExpiredInterceptor @Inject constructor(
    private val sessionStore: SessionStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (!sessionStore.hasSession()) {
            return response
        }

        val expired = when {
            response.code == 401 -> true
            else -> isExpiredBody(response)
        }

        if (expired) {
            sessionStore.notifySessionExpired()
        }

        return response
    }

    /**
     * Lit le corps sans le consommer (peek) pour détecter un code d'expiration.
     * Ne log jamais le contenu (peut contenir des messages serveur).
     */
    private fun isExpiredBody(response: Response): Boolean {
        val peek = response.peekBody(64 * 1024)
        val text = try {
            peek.string()
        } catch (_: Exception) {
            return false
        }
        if (text.isBlank()) return false

        return try {
            val json = JSONObject(text)
            val code = sequenceOf("code", "error", "erreur")
                .mapNotNull { key -> json.optString(key, null)?.takeIf { it.isNotBlank() } }
                .firstOrNull()
                ?.uppercase()
            code in EXPIRED_CODES
        } catch (_: Exception) {
            false
        }
    }

    companion object {
        private val EXPIRED_CODES = setOf(
            "SESSION_EXPIRED",
            "TOKEN_EXPIRED",
            "UNAUTHORIZED",
            "AUTH_REQUIRED",
            "NOT_AUTHENTICATED",
        )
    }
}
