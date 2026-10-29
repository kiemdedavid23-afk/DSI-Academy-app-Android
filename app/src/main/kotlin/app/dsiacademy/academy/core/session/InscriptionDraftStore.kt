package app.dsiacademy.academy.core.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Équivalent de `localStorage.dsi_inscription_draft` du site.
 * Ne contient JAMAIS de session membre — uniquement le suivi d'inscription / paiement.
 * paymentTrackingToken : opaque, jamais journalisé.
 */
data class InscriptionDraft(
    val userId: String? = null,
    val paymentTrackingToken: String? = null,
    val paymentUrl: String? = null,
    val etape: String = "paiement", // paiement | confirmation
)

private val Context.inscriptionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "dsi_inscription_draft",
)

@Singleton
class InscriptionDraftStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val key = stringPreferencesKey("draft")

    val draftFlow: Flow<InscriptionDraft?> = context.inscriptionDataStore.data.map { prefs ->
        prefs[key]?.let { parse(it) }
    }

    suspend fun getDraft(): InscriptionDraft? {
        val raw = context.inscriptionDataStore.data.first()[key] ?: return null
        return parse(raw)
    }

    suspend fun saveDraft(draft: InscriptionDraft) {
        val json = JSONObject().apply {
            put("userId", draft.userId)
            put("paymentTrackingToken", draft.paymentTrackingToken)
            put("paymentUrl", draft.paymentUrl)
            put("etape", draft.etape)
        }
        context.inscriptionDataStore.edit { it[key] = json.toString() }
    }

    suspend fun clearDraft() {
        context.inscriptionDataStore.edit { it.remove(key) }
    }

    private fun parse(raw: String): InscriptionDraft? {
        return try {
            val j = JSONObject(raw)
            InscriptionDraft(
                userId = j.optString("userId").takeIf { it.isNotBlank() && it != "null" },
                paymentTrackingToken = j.optString("paymentTrackingToken")
                    .takeIf { it.isNotBlank() && it != "null" },
                paymentUrl = j.optString("paymentUrl").takeIf { it.isNotBlank() && it != "null" },
                etape = j.optString("etape", "paiement"),
            )
        } catch (_: Exception) {
            null
        }
    }
}
