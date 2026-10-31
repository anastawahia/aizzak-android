package android.aizzak.com.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import android.aizzak.com.R
import android.aizzak.com.core.designsystem.FormPage
import android.aizzak.com.core.designsystem.InformationNotice
import android.aizzak.com.core.designsystem.PageTitle
import android.aizzak.com.core.model.ThemePreference

@Composable
fun SettingsScreen(state: SettingsUiState, language: String, version: String, onTheme: (ThemePreference) -> Unit, onLanguage: (String) -> Unit, onRetry: () -> Unit, onBack: () -> Unit) {
    FormPage {
        TextButton(onBack) { Text(stringResource(R.string.back)) }
        PageTitle(stringResource(R.string.settings))
        if (state.loading) {
            CircularProgressIndicator()
            Text(stringResource(R.string.loading))
        }
        if (state.error != null) {
            InformationNotice(stringResource(if (state.error == SettingsError.READ) R.string.preferences_read_error else R.string.preferences_write_error), isError = true)
            TextButton(onRetry) { Text(stringResource(R.string.retry)) }
        }
        state.preferences?.let { preferences ->
            SectionTitle(stringResource(R.string.language))
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PreferenceChoice(stringResource(R.string.english), language == "en", !state.saving) { onLanguage("en") }
                PreferenceChoice(stringResource(R.string.arabic), language == "ar", !state.saving) { onLanguage("ar") }
            }
            SectionTitle(stringResource(R.string.appearance))
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemePreference.entries.forEach { theme ->
                    val label = stringResource(when (theme) {
                        ThemePreference.SYSTEM -> R.string.theme_system
                        ThemePreference.LIGHT -> R.string.theme_light
                        ThemePreference.DARK -> R.string.theme_dark
                    })
                    PreferenceChoice(label, preferences.theme == theme, !state.saving) { onTheme(theme) }
                }
            }
        }
        if (state.saving) Text(stringResource(R.string.saving), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.version, version), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun PreferenceChoice(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Surface(color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.small) {
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RadioButton(selected, onClick = null, enabled = enabled)
            Text(label, Modifier.weight(1f))
        }
    }
}
