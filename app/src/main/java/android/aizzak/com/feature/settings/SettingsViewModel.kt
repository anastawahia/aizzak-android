package android.aizzak.com.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.aizzak.com.core.model.AppPreferences
import android.aizzak.com.core.model.ThemePreference
import android.aizzak.com.data.preferences.PreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SettingsError { READ, WRITE }

data class SettingsUiState(
    val loading: Boolean = true,
    val preferences: AppPreferences? = null,
    val saving: Boolean = false,
    val readFailed: Boolean = false,
    val writeFailed: Boolean = false,
    val errorDismissed: Boolean = false,
) {
    val error: SettingsError?
        get() = when {
            errorDismissed -> null
            readFailed -> SettingsError.READ
            writeFailed -> SettingsError.WRITE
            else -> null
        }
}

class SettingsViewModel(
    private val repository: PreferencesRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = mutableUiState.asStateFlow()

    private val writes = Channel<PreferenceWrite>(Channel.UNLIMITED)
    private var pendingWrites = 0
    private var failedTheme: PreferenceWrite.Theme? = null
    private var failedLocaleInitialization = false
    private var readJob: Job? = null

    init {
        observePreferences()
        viewModelScope.launch {
            for (write in writes) {
                try {
                    when (write) {
                        is PreferenceWrite.Theme -> repository.setTheme(write.value)
                        PreferenceWrite.InitializeLocale -> repository.markLocaleInitialized()
                    }
                    when (write) {
                        is PreferenceWrite.Theme -> failedTheme = null
                        PreferenceWrite.InitializeLocale -> failedLocaleInitialization = false
                    }
                    mutableUiState.update {
                        it.copy(writeFailed = failedTheme != null || failedLocaleInitialization)
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    when (write) {
                        is PreferenceWrite.Theme -> failedTheme = write
                        PreferenceWrite.InitializeLocale -> failedLocaleInitialization = true
                    }
                    mutableUiState.update { it.copy(writeFailed = true, errorDismissed = false) }
                } finally {
                    pendingWrites--
                    mutableUiState.update { it.copy(saving = pendingWrites > 0) }
                }
            }
        }
    }

    fun setTheme(theme: ThemePreference) = enqueue(PreferenceWrite.Theme(theme))

    fun markLocaleInitialized() = enqueue(PreferenceWrite.InitializeLocale)

    fun retryRead() {
        if (mutableUiState.value.readFailed) observePreferences()
    }

    fun retrySave() {
        failedTheme?.let(::enqueue)
        if (failedLocaleInitialization) enqueue(PreferenceWrite.InitializeLocale)
    }

    /** Dismisses the notification, without claiming that the failed operation succeeded. */
    fun dismissError() {
        mutableUiState.update { it.copy(errorDismissed = true) }
    }

    private fun observePreferences() {
        readJob?.cancel()
        mutableUiState.update { it.copy(loading = true) }
        readJob = viewModelScope.launch {
            try {
                repository.preferences.collect { preferences ->
                    mutableUiState.update {
                        it.copy(loading = false, preferences = preferences, readFailed = false)
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableUiState.update {
                    it.copy(loading = false, readFailed = true, errorDismissed = false)
                }
            }
        }
    }

    private fun enqueue(write: PreferenceWrite) {
        pendingWrites++
        mutableUiState.update { it.copy(saving = true) }
        if (writes.trySend(write).isFailure) {
            pendingWrites--
            mutableUiState.update { it.copy(saving = pendingWrites > 0, writeFailed = true) }
        }
    }

    override fun onCleared() {
        writes.close()
        super.onCleared()
    }

    private sealed interface PreferenceWrite {
        data class Theme(val value: ThemePreference) : PreferenceWrite
        data object InitializeLocale : PreferenceWrite
    }
}
