package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.GenerationLanguage
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.snDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sn_creativity_preferences"
)

data class UserPreferencesState(
    val themeMode: AppThemeMode = AppThemeMode.NEON_DARK,
    val preferredLanguage: GenerationLanguage = GenerationLanguage.ENGLISH,
    val enable3DMotionEffects: Boolean = true,
    val autoFallbackToLocalOnUnconfigured: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val ENABLE_3D_MOTION = booleanPreferencesKey("enable_3d_motion")
        val AUTO_LOCAL_FALLBACK = booleanPreferencesKey("auto_local_fallback")
    }

    val preferencesFlow: Flow<UserPreferencesState> = context.snDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            UserPreferencesState(
                themeMode = AppThemeMode.fromKey(prefs[Keys.THEME_MODE]),
                preferredLanguage = GenerationLanguage.fromCode(prefs[Keys.LANGUAGE_CODE]),
                enable3DMotionEffects = prefs[Keys.ENABLE_3D_MOTION] ?: true,
                autoFallbackToLocalOnUnconfigured = prefs[Keys.AUTO_LOCAL_FALLBACK] ?: false
            )
        }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.snDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.storageKey
        }
    }

    suspend fun setPreferredLanguage(language: GenerationLanguage) {
        context.snDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE_CODE] = language.code
        }
    }

    suspend fun setEnable3DMotion(enabled: Boolean) {
        context.snDataStore.edit { prefs ->
            prefs[Keys.ENABLE_3D_MOTION] = enabled
        }
    }

    suspend fun setAutoLocalFallback(enabled: Boolean) {
        context.snDataStore.edit { prefs ->
            prefs[Keys.AUTO_LOCAL_FALLBACK] = enabled
        }
    }
}
