package android.aizzak.com.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import android.aizzak.com.R
import android.aizzak.com.core.designsystem.Brand
import android.aizzak.com.core.designsystem.FormPage
import android.aizzak.com.core.designsystem.InformationNotice
import android.aizzak.com.core.designsystem.PageTitle

enum class AuthPage { SIGN_IN, SIGN_UP, RESET }

/** Event boundary shared by real routes and isolated UI tests. No SDK types reach composition. */
data class AuthActions(
    val signIn: (String, String) -> Unit,
    val signUp: (String, String, String) -> Unit,
    val resetPassword: (String) -> Unit,
    val resendVerification: () -> Unit,
    val refreshIdentity: () -> Unit,
    val signOut: () -> Unit,
    val clearFeedback: () -> Unit,
)

@Composable
fun AuthScreen(
    page: AuthPage,
    state: AuthUiState,
    actions: AuthActions,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    onReset: () -> Unit,
    onPreferences: () -> Unit,
) {
    // Passwords deliberately never enter saved instance state or the ViewModel's state.
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
        AuthFeedback(state)
        Column(Modifier.imePadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IdentityField(email, { email = it; actions.clearFeedback() }, stringResource(R.string.email),
                tag = "auth-email", enabled = !state.busy,
                problem = if (state.error == AuthError.INVALID_EMAIL) stringResource(R.string.auth_invalid_email) else null)
            if (page != AuthPage.RESET) {
                IdentityField(password, { password = it; actions.clearFeedback() }, stringResource(R.string.password),
                    tag = "auth-password", enabled = !state.busy, secret = true,
                    problem = if (state.error == AuthError.EMPTY_PASSWORD) stringResource(R.string.auth_empty_password) else null)
            }
            if (page == AuthPage.SIGN_UP) {
                val mismatch = (confirmation.isNotEmpty() && confirmation != password) || state.error == AuthError.PASSWORD_MISMATCH
                IdentityField(confirmation, { confirmation = it; actions.clearFeedback() }, stringResource(R.string.confirm_password),
                    tag = "auth-confirmation", enabled = !state.busy, secret = true,
                    problem = if (mismatch) stringResource(R.string.password_mismatch) else null)
            }
            Button(
                onClick = {
                    when (page) {
                        AuthPage.SIGN_IN -> actions.signIn(email, password)
                        AuthPage.SIGN_UP -> actions.signUp(email, password, confirmation)
                        AuthPage.RESET -> actions.resetPassword(email)
                    }
                },
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("auth-submit"),
            ) {
                Text(if (state.busy) stringResource(R.string.auth_working)
                    else if (page == AuthPage.RESET) stringResource(R.string.send_reset) else title)
            }
            when (page) {
                AuthPage.SIGN_IN -> {
                    TextButton(onReset, Modifier.fillMaxWidth(), enabled = !state.busy) { Text(stringResource(R.string.forgot_password)) }
                    TextButton(onSignUp, Modifier.fillMaxWidth(), enabled = !state.busy) { Text(stringResource(R.string.create_account)) }
                }
                else -> TextButton(onSignIn, Modifier.fillMaxWidth(), enabled = !state.busy) { Text(stringResource(R.string.back_sign_in)) }
            }
            TextButton(onPreferences, Modifier.fillMaxWidth()) { Text(stringResource(R.string.preferences)) }
        }
    }
}

@Composable
fun AuthFeedback(state: AuthUiState) {
    Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.error?.let { InformationNotice(stringResource(authErrorText(it)), isError = true) }
        state.message?.let {
            InformationNotice(stringResource(when (it) {
                AuthMessage.RESET_REQUESTED -> R.string.auth_reset_requested
                AuthMessage.VERIFICATION_SENT -> R.string.auth_verification_sent
                AuthMessage.EMAIL_NOT_VERIFIED -> R.string.auth_email_not_verified
            }))
        }
        if (state.busy) {
            CircularProgressIndicator()
            Text(stringResource(R.string.auth_working))
        }
    }
}

private fun authErrorText(error: AuthError): Int = when (error) {
    AuthError.INVALID_EMAIL -> R.string.auth_invalid_email
    AuthError.EMPTY_PASSWORD -> R.string.auth_empty_password
    AuthError.PASSWORD_MISMATCH -> R.string.password_mismatch
    AuthError.INVALID_CREDENTIALS -> R.string.auth_invalid_credentials
    AuthError.EMAIL_IN_USE -> R.string.auth_email_in_use
    AuthError.WEAK_PASSWORD -> R.string.auth_weak_password
    AuthError.NETWORK -> R.string.auth_network_error
    AuthError.TOO_MANY_REQUESTS -> R.string.auth_too_many_requests
    AuthError.USER_DISABLED -> R.string.auth_user_disabled
    AuthError.SESSION_EXPIRED -> R.string.auth_session_expired
    AuthError.SERVICE_UNAVAILABLE -> R.string.auth_service_unavailable
    AuthError.UNKNOWN -> R.string.auth_unknown_error
}

@Composable
private fun IdentityField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    tag: String,
    enabled: Boolean,
    secret: Boolean = false,
    problem: String? = null,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value, onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().testTag(tag), singleLine = true, enabled = enabled,
        label = { Text(label) },
        textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
        keyboardOptions = KeyboardOptions(keyboardType = if (secret) KeyboardType.Password else KeyboardType.Email, autoCorrectEnabled = false),
        visualTransformation = if (secret && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        isError = problem != null,
        supportingText = problem?.let { { Text(it) } },
        trailingIcon = if (secret) ({
            TextButton(onClick = { visible = !visible }) {
                Text(stringResource(if (visible) R.string.hide_password else R.string.show_password))
            }
        }) else null,
    )
}
