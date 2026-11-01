package android.aizzak.com.feature.auth

import android.aizzak.com.R
import android.aizzak.com.core.designsystem.FormPage
import android.aizzak.com.core.designsystem.IconBlock
import android.aizzak.com.core.designsystem.InformationNotice
import android.aizzak.com.core.designsystem.PageTitle
import android.aizzak.com.core.designsystem.SupportingText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp

/** Genuine identity still needs verified email and a server account context before Home. */
@Suppress("UNUSED_PARAMETER")
@Composable
fun AuthSessionScreen(state: AuthUiState, actions: AuthActions, onPreferences: () -> Unit) {
    when (state.stage) {
        AuthStage.RESTORING, AuthStage.LOADING_CONTEXT -> LoadingState(state)
        AuthStage.NEEDS_VERIFICATION -> VerificationState(state, actions)
        AuthStage.CONTEXT_UNAVAILABLE, AuthStage.ACCESS_DENIED, AuthStage.READY -> ContextFailureState(state, actions)
        AuthStage.SIGNED_OUT -> Unit
    }
}

@Composable
private fun LoadingState(state: AuthUiState) {
    FormPage(horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            CircularProgressIndicator(
                Modifier.testTag("auth-context-loading"),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
                strokeWidth = 4.dp,
            )
            PageTitle(stringResource(R.string.getting_ready), Modifier.testTag("auth-loading-title"))
            Text(
                stringResource(if (state.stage == AuthStage.RESTORING) R.string.restoring_description else R.string.context_loading_description),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VerificationState(state: AuthUiState, actions: AuthActions) {
    FormPage {
        IconBlock("✉")
        PageTitle(stringResource(R.string.check_inbox), Modifier.testTag("auth-verification-title"))
        SupportingText(stringResource(R.string.auth_verify_description))
        state.identity?.email?.let {
            Text(it, style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr))
        }
        InformationNotice(stringResource(R.string.verification_notice))
        AuthFeedback(state)
        Button(
            actions.refreshIdentity,
            Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("auth-refresh"),
            enabled = !state.busy,
        ) { Text(stringResource(R.string.auth_check_verification)) }
        OutlinedButton(
            actions.resendVerification,
            Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("auth-resend"),
            enabled = !state.busy,
        ) { Text(stringResource(R.string.auth_resend_verification)) }
        TextButton(
            actions.signOut,
            Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("auth-sign-out"),
            enabled = state.action != AuthAction.SIGN_OUT,
        ) { Text(stringResource(R.string.sign_out)) }
    }
}

@Composable
private fun ContextFailureState(state: AuthUiState, actions: AuthActions) {
    val denied = state.stage == AuthStage.ACCESS_DENIED
    FormPage(horizontalAlignment = Alignment.CenterHorizontally) {
        IconBlock("!")
        PageTitle(
            stringResource(if (denied) R.string.auth_access_denied_title else R.string.context_failure_title),
            Modifier.testTag("auth-context-title"),
        )
        Text(
            stringResource(when {
                denied -> R.string.auth_access_denied
                state.contextError == AccountContextError.NETWORK -> R.string.auth_network_error
                state.contextError == AccountContextError.RATE_LIMITED -> R.string.auth_too_many_requests
                state.contextError == AccountContextError.INVALID_RESPONSE -> R.string.auth_context_invalid
                else -> R.string.context_failure_description
            }),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AuthFeedback(state)
        state.correlationId?.let { Text(stringResource(R.string.support_reference, it), style = MaterialTheme.typography.bodySmall) }
        Button(
            actions.refreshIdentity,
            Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("auth-context-retry"),
            enabled = !state.busy,
        ) { Text(stringResource(R.string.retry)) }
        TextButton(
            actions.signOut,
            Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("auth-sign-out"),
            enabled = state.action != AuthAction.SIGN_OUT,
        ) { Text(stringResource(R.string.sign_out)) }
    }
}
