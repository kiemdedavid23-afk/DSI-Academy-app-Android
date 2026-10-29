package app.dsiacademy.academy.core.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/** Équivalent ContinueStore / FavoritesStore du site (local, sans Worker). */

data class ContinueEntry(
    val title: String,
    val house: String,
    val path: String,
    val icon: String = "📌",
    val progress: Int? = null,
)

data class FavoriteEntry(
    val id: String,
    val title: String,
    val path: String,
    val icon: String = "⭐",
)

private val Context.localContentStore by preferencesDataStore("dsi_local_content")

@Singleton
class LocalContentStores @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val continueKey = stringPreferencesKey("continue")
    private val favoritesKey = stringPreferencesKey("favorites")

    val continueFlow: Flow<ContinueEntry?> = context.localContentStore.data.map { prefs ->
        prefs[continueKey]?.let { parseContinue(it) }
    }

    val favoritesFlow: Flow<List<FavoriteEntry>> = context.localContentStore.data.map { prefs ->
        prefs[favoritesKey]?.let { parseFavorites(it) } ?: emptyList()
    }

    suspend fun getContinue(): ContinueEntry? =
        context.localContentStore.data.first()[continueKey]?.let { parseContinue(it) }

    suspend fun setContinue(entry: ContinueEntry) {
        val json = JSONObject()
            .put("title", entry.title)
            .put("house", entry.house)
            .put("path", entry.path)
            .put("icon", entry.icon)
            .put("progress", entry.progress)
        context.localContentStore.edit { it[continueKey] = json.toString() }
    }

    suspend fun getFavorites(): List<FavoriteEntry> =
        context.localContentStore.data.first()[favoritesKey]?.let { parseFavorites(it) } ?: emptyList()

    suspend fun setFavorites(items: List<FavoriteEntry>) {
        val arr = JSONArray()
        items.forEach { e ->
            arr.put(
                JSONObject()
                    .put("id", e.id)
                    .put("title", e.title)
                    .put("path", e.path)
                    .put("icon", e.icon),
            )
        }
        context.localContentStore.edit { it[favoritesKey] = arr.toString() }
    }

    private fun parseContinue(raw: String): ContinueEntry? = try {
        val j = JSONObject(raw)
        ContinueEntry(
            title = j.optString("title"),
            house = j.optString("house"),
            path = j.optString("path"),
            icon = j.optString("icon", "📌"),
            progress = if (j.has("progress") && !j.isNull("progress")) j.optInt("progress") else null,
        )
    } catch (_: Exception) { null }

    private fun parseFavorites(raw: String): List<FavoriteEntry> = try {
        val arr = JSONArray(raw)
        buildList {
            for (i in 0 until arr.length()) {
                val j = arr.getJSONObject(i)
                add(
                    FavoriteEntry(
                        id = j.optString("id"),
                        title = j.optString("title"),
                        path = j.optString("path"),
                        icon = j.optString("icon", "⭐"),
                    ),
                )
            }
        }
    } catch (_: Exception) { emptyList() }
}
