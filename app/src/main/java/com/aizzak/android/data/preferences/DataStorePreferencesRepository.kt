package com.aizzak.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aizzak.android.core.model.AppPreferences
import com.aizzak.android.core.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.aizzakPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "aizzak_preferences",
)

class DataStorePreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : PreferencesRepository {
    override val preferences: Flow<AppPreferences> = dataStore.data.map { stored ->
        AppPreferences(
            theme = ThemePreference.fromStorageKey(stored[THEME]),
            localeInitialized = stored[LOCALE_INITIALIZED] ?: false,
        )
    }

    override suspend fun setTheme(theme: ThemePreference) {
        dataStore.edit { it[THEME] = theme.storageKey }
    }

    override suspend fun markLocaleInitialized() {
        dataStore.edit { it[LOCALE_INITIALIZED] = true }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme")
        val LOCALE_INITIALIZED = booleanPreferencesKey("locale_initialized")
    }
}
