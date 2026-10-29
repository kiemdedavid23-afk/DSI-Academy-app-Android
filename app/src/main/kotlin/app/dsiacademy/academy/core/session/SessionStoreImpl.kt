package app.dsiacademy.academy.core.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implémentation chiffrée de SessionStore.
 * - Mode souvenir = true  → EncryptedSharedPreferences (Keystore)
 * - Mode souvenir = false → mémoire seule (équivalent sessionStorage)
 *
 * Aligné sur sessionStore.js du site : mêmes champs, même sémantique.
 */
@Singleton
class SessionStoreImpl @Inject constructor(
    @ApplicationContext context: Context,
) : SessionStore {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    /** Session non persistante (équivalent sessionStorage). */
    @Volatile
    private var memorySession: MemberSession? = null

    private val _sessionFlow = MutableStateFlow<MemberSession?>(null)
    override val sessionFlow: StateFlow<MemberSession?> = _sessionFlow.asStateFlow()

    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    override val events: SharedFlow<SessionEvent> = _events.asSharedFlow()

    init {
        _sessionFlow.value = readPersistent()
    }

    override fun getSession(): MemberSession? {
        return memorySession ?: readPersistent()
    }

    override fun hasSession(): Boolean = getSession() != null

    override fun setSession(session: MemberSession, souvenir: Boolean) {
        if (souvenir) {
            memorySession = null
            writePersistent(session)
        } else {
            clearPersistent()
            memorySession = session
        }
        _sessionFlow.value = session
    }

    override fun clearSession() {
        memorySession = null
        clearPersistent()
        _sessionFlow.value = null
    }

    override fun notifySessionExpired() {
        clearSession()
        _events.tryEmit(SessionEvent.Expired)
    }

    private fun writePersistent(session: MemberSession) {
        val state = JSONObject().apply {
            put("accountStatus", session.accountStatus)
            put("subscriptionStatus", session.subscriptionStatus)
            put("subscriptionActive", session.subscriptionActive)
            put("dateFin", session.dateFin)
            put("accessProof", session.accessProof)
        }
        encryptedPrefs.edit()
            .putString(KEY_TOKEN, session.token)
            .putString(KEY_STATE, state.toString())
            .apply()
    }

    private fun readPersistent(): MemberSession? {
        val token = encryptedPrefs.getString(KEY_TOKEN, null) ?: return null
        val raw = encryptedPrefs.getString(KEY_STATE, null)
        var accountStatus: String? = null
        var subscriptionStatus: String? = null
        var subscriptionActive = false
        var dateFin: String? = null
        var accessProof: String? = null
        if (raw != null) {
            try {
                val json = JSONObject(raw)
                accountStatus = json.nullableString("accountStatus")
                subscriptionStatus = json.nullableString("subscriptionStatus")
                subscriptionActive = json.optBoolean("subscriptionActive", false)
                dateFin = json.nullableString("dateFin")
                accessProof = json.nullableString("accessProof")
            } catch (_: Exception) {
                // état corrompu : on garde le token, on ignore l'état (comme le site)
            }
        }
        return MemberSession(
            token = token,
            accountStatus = accountStatus,
            subscriptionStatus = subscriptionStatus,
            subscriptionActive = subscriptionActive,
            dateFin = dateFin,
            accessProof = accessProof,
        )
    }

    private fun clearPersistent() {
        encryptedPrefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "dsi_session_encrypted"
        private const val KEY_TOKEN = "dsi_session_token"
        private const val KEY_STATE = "dsi_session_state"
    }
}

private fun JSONObject.nullableString(key: String): String? {
    if (!has(key) || isNull(key)) return null
    val value = optString(key, "")
    return value.takeIf { it.isNotEmpty() && it != "null" }
}
