package android.aizzak.com.feature.settings

import android.aizzak.com.R
import android.aizzak.com.core.designsystem.ConnectedPanel
import android.aizzak.com.core.designsystem.FormPage
import android.aizzak.com.core.designsystem.InformationNotice
import android.aizzak.com.core.designsystem.PageTitle
import android.aizzak.com.core.designsystem.PanelDivider
import android.aizzak.com.core.designsystem.SectionLabel
import android.aizzak.com.core.designsystem.SupportingText
import android.aizzak.com.core.model.ThemePreference
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    language: String,
    version: String,
    onTheme: (ThemePreference) -> Unit,
    onLanguage: (String) -> Unit,
    onRetry: () -> Unit,
    onBack: (() -> Unit)?,
) {
    FormPage(maxWidth = 620.dp) {
        onBack?.let {
            TextButton(it, Modifier.heightIn(min = 48.dp).testTag("settings-back")) {
                Text(stringResource(R.string.back_with_arrow))
            }
        }
        PageTitle(stringResource(R.string.settings))
        SupportingText(stringResource(R.string.settings_description))
        if (state.loading) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator()
                Text(stringResource(R.string.loading))
            }
        }
        if (state.error != null) {
            InformationNotice(
                stringResource(if (state.error == SettingsError.READ) R.string.preferences_read_error else R.string.preferences_write_error),
                isError = true,
            )
            TextButton(onRetry, Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.retry)) }
        }
        state.preferences?.let { preferences ->
            SectionLabel(stringResource(R.string.language))
            ConnectedPanel {
                Column(Modifier.selectableGroup()) {
                    PreferenceChoice(stringResource(R.string.english), language == "en", !state.saving, "settings-language-en") { onLanguage("en") }
                    PanelDivider()
                    PreferenceChoice(stringResource(R.string.arabic), language == "ar", !state.saving, "settings-language-ar") { onLanguage("ar") }
                }
            }
            SectionLabel(stringResource(R.string.appearance))
            ConnectedPanel {
                Column(Modifier.selectableGroup()) {
                    ThemePreference.entries.forEachIndexed { index, theme ->
                        val label = stringResource(when (theme) {
                            ThemePreference.SYSTEM -> R.string.theme_system
                            ThemePreference.LIGHT -> R.string.theme_light
                            ThemePreference.DARK -> R.string.theme_dark
                        })
                        PreferenceChoice(
                            label,
                            preferences.theme == theme,
                            !state.saving,
                            "settings-theme-${theme.name.lowercase()}",
                        ) { onTheme(theme) }
                        if (index != ThemePreference.entries.lastIndex) PanelDivider()
                    }
                }
            }
        }
        if (state.saving) Text(stringResource(R.string.saving), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            stringResource(R.string.version, version),
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PreferenceChoice(label: String, selected: Boolean, enabled: Boolean, tag: String, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .testTag(tag)
                .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            RadioButton(selected, onClick = null, enabled = enabled)
        }
    }
}
