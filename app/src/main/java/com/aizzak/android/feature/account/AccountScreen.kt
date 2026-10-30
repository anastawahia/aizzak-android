package com.aizzak.android.feature.account

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.aizzak.android.R
import com.aizzak.android.core.designsystem.AizzakTheme
import com.aizzak.android.core.designsystem.FormPage
import com.aizzak.android.core.designsystem.PageTitle

/** Receives genuine identity fields only; absent optional fields are omitted. */
@Composable
fun AccountScreen(email: String?, displayName: String?, onChangePassword: () -> Unit, onSignOut: () -> Unit) {
    FormPage {
        PageTitle(stringResource(R.string.account))
        displayName?.takeIf { it.isNotBlank() }?.let { Text(it) }
        email?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodyLarge.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr))
        }
        Button(onChangePassword, Modifier.fillMaxWidth()) { Text(stringResource(R.string.change_password)) }
        OutlinedButton(onSignOut, Modifier.fillMaxWidth()) { Text(stringResource(R.string.sign_out)) }
    }
}

@Preview(name = "Sample account — preview only", showBackground = true)
@Composable
private fun AccountPreview() {
    AizzakTheme { AccountScreen("preview@example.com", null, {}, {}) }
}
