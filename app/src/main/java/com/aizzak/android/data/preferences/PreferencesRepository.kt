package com.aizzak.android.data.preferences

import com.aizzak.android.core.model.AppPreferences
import com.aizzak.android.core.model.ThemePreference
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val preferences: Flow<AppPreferences>

    suspend fun setTheme(theme: ThemePreference)

    /** Records first-run initialization; the locale itself belongs to Android/AppCompat. */
    suspend fun markLocaleInitialized()
}
