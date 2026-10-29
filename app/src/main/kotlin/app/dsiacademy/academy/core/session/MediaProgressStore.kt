package app.dsiacademy.academy.core.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private val Context.progressStore by preferencesDataStore("dsi_media_progress")

data class MediaProgress(val positionMs: Long = 0, val page: Int = 0, val percent: Int = 0)

@Singleton
class MediaProgressStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private fun key(id: String) = stringPreferencesKey("p_$id")

    suspend fun get(contentId: String): MediaProgress {
        val raw = context.progressStore.data.first()[key(contentId)] ?: return MediaProgress()
        return try {
            val j = JSONObject(raw)
            MediaProgress(
                positionMs = j.optLong("ms", 0),
                page = j.optInt("page", 0),
                percent = j.optInt("pct", 0),
            )
        } catch (_: Exception) {
            MediaProgress()
        }
    }

    suspend fun save(contentId: String, progress: MediaProgress) {
        val json = JSONObject()
            .put("ms", progress.positionMs)
            .put("page", progress.page)
            .put("pct", progress.percent)
            .toString()
        context.progressStore.edit { it[key(contentId)] = json }
    }
}
