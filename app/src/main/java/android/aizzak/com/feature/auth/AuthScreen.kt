package android.aizzak.com.feature.auth

import android.aizzak.com.R
import android.aizzak.com.core.designsystem.AizzakTextField
import android.aizzak.com.core.designsystem.DirectionalBackIcon
import android.aizzak.com.core.designsystem.ExternalLabel
import android.aizzak.com.core.designsystem.FormPage
import android.aizzak.com.core.designsystem.IconBlock
import android.aizzak.com.core.designsystem.InformationNotice
import android.aizzak.com.core.designsystem.PageTitle
import android.aizzak.com.core.designsystem.SupportingText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp

enum class AuthPage { SIGN_IN, SIGN_UP, RESET }

data class AuthActions(
    val signIn: (String, String) -> Unit,
    val signUp: (String, String, String) -> Unit,
    val resetPassword: (String) -> Unit,
    val resendVerification: () -> Unit,
    val refreshIdentity: () -> Unit,
    val signOut: () -> Unit,
    val clearFeedback: () -> Unit,
    val changePassword: (String, String, String) -> Unit = { _, _, _ -> },
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
    var email by remember(page) { mutableStateOf("") }
    var password by remember(page) { mutableStateOf("") }
    var confirmation by remember(page) { mutableStateOf("") }
    val focus = LocalFocusManager.current
    val submitLabel = stringResource(when (page) {
        AuthPage.SIGN_IN -> R.string.sign_in
        AuthPage.SIGN_UP -> R.string.create_account
        AuthPage.RESET -> R.string.send_reset
    })
    fun submit() {
        when (page) {
            AuthPage.SIGN_IN -> actions.signIn(email, password)
            AuthPage.SIGN_UP -> actions.signUp(email, password, confirmation)
            AuthPage.RESET -> actions.resetPassword(email)
        }
    }
    FormPage {
        if (page != AuthPage.SIGN_IN) {
            TextButton(onSignIn, Modifier.heightIn(min = 48.dp), enabled = !state.busy) {
                DirectionalBackIcon()
                Text(stringResource(R.string.back))
            }
        }
        if (page == AuthPage.RESET) IconBlock("✉")
        PageTitle(
            stringResource(when (page) {
                AuthPage.SIGN_IN -> R.string.welcome_back
                AuthPage.SIGN_UP -> R.string.create_account_title
                AuthPage.RESET -> R.string.reset_password
            }),
            Modifier.testTag("auth-title"),
        )
        SupportingText(stringResource(when (page) {
            AuthPage.SIGN_IN -> R.string.sign_in_description
            AuthPage.SIGN_UP -> R.string.sign_up_description
            AuthPage.RESET -> R.string.reset_description
        }))
        AuthFeedback(state)
        Column(Modifier.fillMaxWidth().imePadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IdentityField(
                email,
                { email = it; actions.clearFeedback() },
                stringResource(R.string.email_address),
                stringResource(R.string.email_placeholder),
                tag = "auth-email",
                enabled = !state.busy,
                problem = if (state.error == AuthError.INVALID_EMAIL) stringResource(R.string.auth_invalid_email) else null,
                imeAction = if (page == AuthPage.RESET) ImeAction.Done else ImeAction.Next,
                onDone = ::submit,
            )
            if (page != AuthPage.RESET) {
                IdentityField(
                    password,
                    { password = it; actions.clearFeedback() },
                    stringResource(R.string.password),
                    stringResource(R.string.password_placeholder),
                    tag = "auth-password",
                    enabled = !state.busy,
                    secret = true,
                    problem = if (state.error == AuthError.EMPTY_PASSWORD) stringResource(R.string.auth_empty_password) else null,
                    imeAction = if (page == AuthPage.SIGN_IN) ImeAction.Done else ImeAction.Next,
                    onDone = ::submit,
                )
            }
            if (page == AuthPage.SIGN_UP) {
                val mismatch = (confirmation.isNotEmpty() && confirmation != password) || state.error == AuthError.PASSWORD_MISMATCH
                IdentityField(
                    confirmation,
                    { confirmation = it; actions.clearFeedback() },
                    stringResource(R.string.confirm_password),
                    stringResource(R.string.password_placeholder),
                    tag = "auth-confirmation",
                    enabled = !state.busy,
                    secret = true,
                    problem = if (mismatch) stringResource(R.string.password_mismatch) else null,
                    imeAction = ImeAction.Done,
                    onDone = ::submit,
                )
            }
            Button(
                onClick = ::submit,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("auth-submit"),
            ) { Text(if (state.busy) stringResource(R.string.auth_working) else submitLabel) }
            when (page) {
                AuthPage.SIGN_IN -> {
                    TextButton(onReset, Modifier.fillMaxWidth().heightIn(min = 48.dp), enabled = !state.busy) {
                        Text(stringResource(R.string.forgot_password))
                    }
                    FooterAction(stringResource(R.string.new_here), stringResource(R.string.create_account), onSignUp, !state.busy)
                    TextButton(
                        onPreferences,
                        Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("auth-preferences"),
                    ) {
                        Text(stringResource(R.string.preferences))
                    }
                }
                AuthPage.SIGN_UP -> FooterAction(
                    stringResource(R.string.already_have_account),
                    stringResource(R.string.sign_in),
                    onSignIn,
                    !state.busy,
                )
                AuthPage.RESET -> TextButton(onSignIn, Modifier.fillMaxWidth().heightIn(min = 48.dp), enabled = !state.busy) {
                    Text(stringResource(R.string.back_sign_in))
                }
            }
        }
    }
}

@Composable
private fun FooterAction(prefix: String, action: String, onClick: () -> Unit, enabled: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Text(prefix, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick, enabled = enabled) { Text(action) }
    }
}

@Composable
fun AuthFeedback(state: AuthUiState) {
    Column(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.error?.let { InformationNotice(stringResource(authErrorText(it)), isError = true) }
        state.message?.let {
            InformationNotice(stringResource(when (it) {
                AuthMessage.RESET_REQUESTED -> R.string.auth_reset_requested
                AuthMessage.VERIFICATION_SENT -> R.string.auth_verification_sent
                AuthMessage.EMAIL_NOT_VERIFIED -> R.string.auth_email_not_verified
                AuthMessage.PASSWORD_CHANGED -> R.string.password_changed
            }))
        }
        if (state.busy && state.stage == AuthStage.SIGNED_OUT) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.heightIn(max = 28.dp))
                Text(stringResource(R.string.auth_working))
            }
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
    placeholder: String,
    tag: String,
    enabled: Boolean,
    secret: Boolean = false,
    problem: String? = null,
    imeAction: ImeAction,
    onDone: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExternalLabel(label)
        AizzakTextField(
            value = value,
            onValueChange = onChange,
            placeholder = placeholder,
            modifier = Modifier.testTag(tag),
            enabled = enabled,
            textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (secret) KeyboardType.Password else KeyboardType.Email,
                autoCorrectEnabled = false,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focus.moveFocus(FocusDirection.Down) },
                onDone = { onDone() },
            ),
            visualTransformation = if (secret && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            isError = problem != null,
            supportingText = if (problem != null) ({ Text(problem) }) else null,
            trailingIcon = if (secret) ({
                TextButton(onClick = { visible = !visible }, Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(if (visible) R.string.hide_password else R.string.show_password))
                }
            }) else null,
        )
    }
}
