package android.aizzak.com.feature.settings

import androidx.lifecycle.ViewModelStore
import android.aizzak.com.core.model.AppPreferences
import android.aizzak.com.core.model.ThemePreference
import android.aizzak.com.data.preferences.PreferencesRepository
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val store = ViewModelStore()

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun failedInitialReadDoesNotExposeDefaultPreferencesAndCanBeRetried() = runTest {
        val repository = FakePreferencesRepository().apply { failRead = true }
        val subject = subject(repository)
        runCurrent()
        assertNull(subject.uiState.value.preferences)
        assertEquals(SettingsError.READ, subject.uiState.value.error)

        repository.failRead = false
        subject.retryRead()
        runCurrent()
        assertEquals(AppPreferences(), subject.uiState.value.preferences)
        assertNull(subject.uiState.value.error)
        assertFalse(subject.uiState.value.loading)
    }

    @Test
    fun failedSaveRetainsConfirmedThemeAndRetryPersistsRequestedTheme() = runTest {
        val repository = FakePreferencesRepository().apply { failTheme = true }
        val subject = subject(repository)
        runCurrent()
        subject.setTheme(ThemePreference.DARK)
        advanceUntilIdle()
        assertEquals(ThemePreference.SYSTEM, subject.uiState.value.preferences!!.theme)
        assertEquals(SettingsError.WRITE, subject.uiState.value.error)
        assertFalse(subject.uiState.value.saving)

        repository.failTheme = false
        subject.retrySave()
        advanceUntilIdle()
        assertEquals(ThemePreference.DARK, subject.uiState.value.preferences!!.theme)
        assertNull(subject.uiState.value.error)
    }

    @Test
    fun rapidChangesAreSerializedAndLastChoiceWins() = runTest {
        val repository = FakePreferencesRepository()
        val subject = subject(repository)
        runCurrent()
        subject.setTheme(ThemePreference.DARK)
        subject.setTheme(ThemePreference.LIGHT)
        subject.setTheme(ThemePreference.SYSTEM)
        assertTrue(subject.uiState.value.saving)
        advanceUntilIdle()

        assertEquals(listOf(ThemePreference.DARK, ThemePreference.LIGHT, ThemePreference.SYSTEM), repository.savedThemes)
        assertEquals(1, repository.maximumConcurrentWrites)
        assertEquals(ThemePreference.SYSTEM, subject.uiState.value.preferences!!.theme)
        assertFalse(subject.uiState.value.saving)
    }

    @Test
    fun successfulLocaleWriteDoesNotEraseFailedThemeWriteAndBothReadWriteErrorsSurvive() = runTest {
        val repository = FakePreferencesRepository().apply {
            failRead = true
            failTheme = true
        }
        val subject = subject(repository)
        runCurrent()
        subject.setTheme(ThemePreference.DARK)
        subject.markLocaleInitialized()
        advanceUntilIdle()
        assertTrue(subject.uiState.value.readFailed)
        assertTrue(subject.uiState.value.writeFailed)

        repository.failRead = false
        subject.retryRead()
        runCurrent()
        assertEquals(SettingsError.WRITE, subject.uiState.value.error)
        assertTrue(subject.uiState.value.preferences!!.localeInitialized)
        repository.failTheme = false
        subject.retrySave()
        advanceUntilIdle()
        assertEquals(ThemePreference.DARK, subject.uiState.value.preferences!!.theme)
        assertNull(subject.uiState.value.error)
    }

    @Test
    fun dismissedReadErrorRemainsRetryable() = runTest {
        val repository = FakePreferencesRepository().apply { failRead = true }
        val subject = subject(repository)
        runCurrent()
        subject.dismissError()
        assertNull(subject.uiState.value.error)
        assertTrue(subject.uiState.value.readFailed)
        repository.failRead = false
        subject.retryRead()
        runCurrent()
        assertEquals(AppPreferences(), subject.uiState.value.preferences)
        assertFalse(subject.uiState.value.readFailed)
    }

    @Test
    fun clearingViewModelCancelsPendingWriteWithoutReportingSuccess() = runTest {
        val repository = FakePreferencesRepository()
        val subject = subject(repository)
        runCurrent()
        subject.setTheme(ThemePreference.DARK)
        runCurrent()
        store.clear()
        advanceUntilIdle()

        assertEquals(AppPreferences(), repository.stored.value)
        assertTrue(repository.savedThemes.isEmpty())
    }

    private fun subject(repository: PreferencesRepository): SettingsViewModel =
        SettingsViewModel(repository).also { store.put("settings", it) }

    private class FakePreferencesRepository : PreferencesRepository {
        val stored = MutableStateFlow(AppPreferences())
        var failRead = false
        var failTheme = false
        var maximumConcurrentWrites = 0
        private var concurrentWrites = 0
        val savedThemes = mutableListOf<ThemePreference>()
        override val preferences: Flow<AppPreferences> = flow {
            if (failRead) throw IOException("Read unavailable")
            emitAll(stored)
        }

        override suspend fun setTheme(theme: ThemePreference) {
            concurrentWrites++
            maximumConcurrentWrites = maxOf(maximumConcurrentWrites, concurrentWrites)
            try {
                delay(10)
                if (failTheme) throw IOException("Save unavailable")
                stored.value = stored.value.copy(theme = theme)
                savedThemes += theme
            } finally {
                concurrentWrites--
            }
        }

        override suspend fun markLocaleInitialized() {
            stored.value = stored.value.copy(localeInitialized = true)
        }
    }
}
