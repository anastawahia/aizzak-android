package android.aizzak.com.feature.account

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import android.aizzak.com.R
import android.aizzak.com.core.designsystem.AizzakTheme
import android.aizzak.com.core.designsystem.FormPage
import android.aizzak.com.core.designsystem.PageTitle
import android.aizzak.com.feature.auth.AuthFeedback
import android.aizzak.com.feature.auth.AuthUiState
import androidx.compose.ui.platform.testTag

/** Receives genuine identity fields only; absent optional fields are omitted. */
@Composable
fun AccountScreen(email: String?, displayName: String?, onChangePassword: () -> Unit, onSignOut: () -> Unit, state: AuthUiState? = null) {
    FormPage {
        PageTitle(stringResource(R.string.account))
        state?.let { AuthFeedback(it) }
        displayName?.takeIf { it.isNotBlank() }?.let { Text(it) }
        email?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodyLarge.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr))
        }
        Button(onChangePassword, Modifier.fillMaxWidth().testTag("account-change-password"), enabled = state?.busy != true) { Text(stringResource(R.string.change_password)) }
        OutlinedButton(onSignOut, Modifier.fillMaxWidth().testTag("account-sign-out"), enabled = state?.busy != true) { Text(stringResource(R.string.sign_out)) }
    }
}

@Preview(name = "Sample account — preview only", showBackground = true)
@Composable
private fun AccountPreview() {
    AizzakTheme { AccountScreen("preview@example.com", null, {}, {}) }
}
