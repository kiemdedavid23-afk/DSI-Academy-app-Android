package app.dsiacademy.academy.core.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.prefsStore by preferencesDataStore("dsi_preferences")

@Singleton
class PreferencesStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val darkKey = booleanPreferencesKey("dark_theme")

    val isDarkFlow: Flow<Boolean> = context.prefsStore.data.map { it[darkKey] ?: false }

    suspend fun setDark(dark: Boolean) {
        context.prefsStore.edit { it[darkKey] = dark }
    }
}
