package android.aizzak.com.data.preferences

import android.aizzak.com.core.model.AppPreferences
import android.aizzak.com.core.model.ThemePreference
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val preferences: Flow<AppPreferences>

    suspend fun setTheme(theme: ThemePreference)

    /** Records first-run initialization; the locale itself belongs to Android/AppCompat. */
    suspend fun markLocaleInitialized()
}
