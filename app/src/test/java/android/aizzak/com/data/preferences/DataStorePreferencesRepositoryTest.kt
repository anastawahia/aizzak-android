package android.aizzak.com.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import android.aizzak.com.core.model.AppPreferences
import android.aizzak.com.core.model.ThemePreference
import java.io.IOException
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStorePreferencesRepositoryTest {
    @get:Rule val temporaryFolder = TemporaryFolder()

    @Test
    fun emptyStoreUsesSystemThemeAndUninitializedLocale() = runTest {
        val file = temporaryFolder.newFolder().resolve("settings.preferences_pb")
        val store = hostPreferencesStore(backgroundScope, file)

        assertEquals(AppPreferences(), DataStorePreferencesRepository(store).preferences.first())
    }

    @Test
    fun themeAndInitializationSurviveRepositoryAndStoreRecreation() = runTest {
        val file = temporaryFolder.newFolder().resolve("settings.preferences_pb")
        val firstScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        try {
            val firstStore = hostPreferencesStore(firstScope, file)
            val first = DataStorePreferencesRepository(firstStore)
            first.setTheme(ThemePreference.DARK)
            first.markLocaleInitialized()
        } finally {
            firstScope.coroutineContext[Job]!!.cancelAndJoin()
        }

        val reopened = hostPreferencesStore(backgroundScope, file)
        assertEquals(
            AppPreferences(ThemePreference.DARK, localeInitialized = true),
            DataStorePreferencesRepository(reopened).preferences.first(),
        )
    }

    @Test
    fun unknownFutureThemeUsesSystemWithoutErasingStoredValue() = runTest {
        val file = temporaryFolder.newFolder().resolve("settings.preferences_pb")
        val store = hostPreferencesStore(backgroundScope, file)
        val key = stringPreferencesKey("theme")
        store.edit { it[key] = "future-theme" }

        assertEquals(ThemePreference.SYSTEM, DataStorePreferencesRepository(store).preferences.first().theme)
        assertEquals("future-theme", store.data.first()[key])
    }

    @Test
    fun ioFailuresPropagateInsteadOfPretendingPreferencesWereLoadedOrSaved() = runTest {
        val failingStore = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = flow { throw IOException("Unavailable") }
            override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
                throw IOException("Unavailable")
            }
        }
        val repository = DataStorePreferencesRepository(failingStore)
        try {
            repository.preferences.first()
            fail("Read must fail")
        } catch (expected: IOException) {
            assertTrue(expected.message!!.contains("Unavailable"))
        }
        try {
            repository.setTheme(ThemePreference.LIGHT)
            fail("Write must fail")
        } catch (expected: IOException) {
            assertTrue(expected.message!!.contains("Unavailable"))
        }
    }

    // JVM tests run on the host filesystem, where Android's SDK_INT stub is zero.
    // Use DataStore's public cross-platform storage rather than Android's legacy
    // File.renameTo branch, which cannot replace an existing file on Windows.
    // This exercises the repository/serializer with real durable files. The
    // production Android preferencesDataStore delegate still needs device QA.
    private fun hostPreferencesStore(scope: CoroutineScope, file: File): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            storage = OkioStorage(FileSystem.SYSTEM, PreferencesSerializer) {
                file.absolutePath.toPath()
            },
            scope = scope,
        )
}
