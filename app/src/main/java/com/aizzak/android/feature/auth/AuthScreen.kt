package com.aizzak.android.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.aizzak.android.R
import com.aizzak.android.core.designsystem.Brand
import com.aizzak.android.core.designsystem.FormPage
import com.aizzak.android.core.designsystem.InformationNotice
import com.aizzak.android.core.designsystem.PageTitle

enum class AuthPage { SIGN_IN, SIGN_UP, RESET }

/** Local forms only. Submission stays disabled until genuine authentication is configured. */
@Composable
fun AuthScreen(page: AuthPage, onSignIn: () -> Unit, onSignUp: () -> Unit, onReset: () -> Unit, onPreferences: () -> Unit) {
    // Deliberately not saveable: passwords must never enter saved instance state.
    var email by remember(page) { mutableStateOf("") }
    var password by remember(page) { mutableStateOf("") }
    var confirmation by remember(page) { mutableStateOf("") }
    val title = stringResource(when (page) {
        AuthPage.SIGN_IN -> R.string.sign_in
        AuthPage.SIGN_UP -> R.string.create_account
        AuthPage.RESET -> R.string.reset_password
    })
    FormPage {
        Brand()
        PageTitle(title)
        InformationNotice(stringResource(R.string.auth_unavailable))
        Column(Modifier.imePadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IdentityField(email, { email = it }, stringResource(R.string.email))
            if (page != AuthPage.RESET) {
                IdentityField(password, { password = it }, stringResource(R.string.password), secret = true)
            }
            if (page == AuthPage.SIGN_UP) {
                IdentityField(confirmation, { confirmation = it }, stringResource(R.string.confirm_password), secret = true,
                    mismatch = confirmation.isNotEmpty() && confirmation != password)
            }
            Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                Text(if (page == AuthPage.RESET) stringResource(R.string.send_reset) else title)
            }
            when (page) {
                AuthPage.SIGN_IN -> {
                    TextButton(onReset, Modifier.fillMaxWidth()) { Text(stringResource(R.string.forgot_password)) }
                    TextButton(onSignUp, Modifier.fillMaxWidth()) { Text(stringResource(R.string.create_account)) }
                }
                else -> TextButton(onSignIn, Modifier.fillMaxWidth()) { Text(stringResource(R.string.back_sign_in)) }
            }
            TextButton(onPreferences, Modifier.fillMaxWidth()) { Text(stringResource(R.string.preferences)) }
        }
    }
}

@Composable
private fun IdentityField(value: String, onChange: (String) -> Unit, label: String, secret: Boolean = false, mismatch: Boolean = false) {
    var visible by remember { mutableStateOf(false) }
    // Labels follow the page; input text uses explicit LTR within either locale.
    OutlinedTextField(
        value = value, onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(), singleLine = true,
        label = { Text(label) },
        textStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Ltr),
        keyboardOptions = KeyboardOptions(keyboardType = if (secret) KeyboardType.Password else KeyboardType.Email, autoCorrectEnabled = false),
        visualTransformation = if (secret && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        isError = mismatch,
        supportingText = if (mismatch) ({ Text(stringResource(R.string.password_mismatch)) }) else null,
        trailingIcon = if (secret) ({
            TextButton(onClick = { visible = !visible }) {
                Text(stringResource(if (visible) R.string.hide_password else R.string.show_password))
            }
        }) else null,
    )
}
