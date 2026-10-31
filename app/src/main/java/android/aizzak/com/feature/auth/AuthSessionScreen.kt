package android.aizzak.com.feature.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import android.aizzak.com.R
import android.aizzak.com.core.designsystem.Brand
import android.aizzak.com.core.designsystem.FormPage
import android.aizzak.com.core.designsystem.InformationNotice
import android.aizzak.com.core.designsystem.PageTitle

/** Genuine identity still needs verified email and a server account context before Home. */
@Composable
fun AuthSessionScreen(state: AuthUiState, actions: AuthActions, onPreferences: () -> Unit) {
    FormPage {
        Brand()
        when (state.stage) {
            AuthStage.RESTORING -> {
                CircularProgressIndicator()
                Text(stringResource(R.string.auth_restoring_session))
            }
            AuthStage.LOADING_CONTEXT -> {
                CircularProgressIndicator(Modifier.testTag("auth-context-loading"))
                Text(stringResource(R.string.auth_context_loading))
            }
            AuthStage.NEEDS_VERIFICATION -> {
                PageTitle(stringResource(R.string.auth_verify_title))
                state.identity?.email?.let { Text(it, style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr)) }
                InformationNotice(stringResource(R.string.auth_verify_description))
                AuthFeedback(state)
                Button(actions.refreshIdentity, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("auth-refresh"), enabled = !state.busy) {
                    Text(stringResource(R.string.auth_check_verification))
                }
                TextButton(actions.resendVerification, Modifier.fillMaxWidth(), enabled = !state.busy) {
                    Text(stringResource(R.string.auth_resend_verification))
                }
            }
            AuthStage.CONTEXT_UNAVAILABLE, AuthStage.ACCESS_DENIED, AuthStage.READY -> {
                val denied = state.stage == AuthStage.ACCESS_DENIED
                PageTitle(stringResource(if (denied) R.string.auth_access_denied_title else R.string.auth_context_title))
                InformationNotice(stringResource(when {
                    denied -> R.string.auth_access_denied
                    state.contextError == AccountContextError.NETWORK -> R.string.auth_network_error
                    state.contextError == AccountContextError.RATE_LIMITED -> R.string.auth_too_many_requests
                    state.contextError == AccountContextError.INVALID_RESPONSE -> R.string.auth_context_invalid
                    else -> R.string.auth_context_unavailable
                }), isError = true)
                AuthFeedback(state)
                state.correlationId?.let {
                    Text(stringResource(R.string.support_reference, it), style = MaterialTheme.typography.bodySmall)
                }
                Button(actions.refreshIdentity, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("auth-context-retry"), enabled = !state.busy) {
                    Text(stringResource(R.string.retry))
                }
            }
            AuthStage.SIGNED_OUT -> Unit
        }
        if (state.stage != AuthStage.RESTORING && state.stage != AuthStage.SIGNED_OUT) {
            TextButton(actions.signOut, Modifier.fillMaxWidth().testTag("auth-sign-out"),
                enabled = state.action != AuthAction.SIGN_OUT) { Text(stringResource(R.string.sign_out)) }
        }
        TextButton(onPreferences, Modifier.fillMaxWidth()) { Text(stringResource(R.string.preferences)) }
    }
}
