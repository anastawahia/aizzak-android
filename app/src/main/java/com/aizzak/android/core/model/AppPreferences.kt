package com.aizzak.android.core.model

enum class ThemePreference(val storageKey: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStorageKey(value: String?): ThemePreference =
            entries.firstOrNull { it.storageKey == value } ?: SYSTEM
    }
}

data class AppPreferences(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val localeInitialized: Boolean = false,
)
