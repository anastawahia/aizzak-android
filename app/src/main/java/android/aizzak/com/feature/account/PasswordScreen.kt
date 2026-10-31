package android.aizzak.com.feature.account

import android.aizzak.com.R
import android.aizzak.com.core.designsystem.FormPage
import android.aizzak.com.core.designsystem.PageTitle
import android.aizzak.com.feature.auth.AuthFeedback
import android.aizzak.com.feature.auth.AuthUiState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/** Secrets live only in this mounted form; no saved state or persistence. */
@Composable
fun PasswordScreen(
    state: AuthUiState,
    onSubmit: (String, String, String) -> Unit,
    onClearFeedback: () -> Unit,
    onBack: () -> Unit,
) {
    var current by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                current = ""; password = ""; confirmation = ""
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            current = ""; password = ""; confirmation = ""
        }
    }
    fun submit() { if (!state.busy) onSubmit(current, password, confirmation) }
    FormPage {
        TextButton(onBack, Modifier.testTag("password-back"), enabled = !state.busy) { Text(stringResource(R.string.back)) }
        PageTitle(stringResource(R.string.change_password))
        Text(stringResource(R.string.password_change_description))
        AuthFeedback(state)
        PasswordField(current, { current = it; onClearFeedback() }, stringResource(R.string.current_password), "password-current", !state.busy)
        PasswordField(password, { password = it; onClearFeedback() }, stringResource(R.string.new_password), "password-new", !state.busy)
        PasswordField(confirmation, { confirmation = it; onClearFeedback() }, stringResource(R.string.confirm_password), "password-confirmation", !state.busy,
            done = true, onDone = ::submit)
        Button(::submit, Modifier.fillMaxWidth().heightIn(min = 48.dp).imePadding().testTag("password-save"), enabled = !state.busy) {
            Text(stringResource(if (state.busy) R.string.auth_working else R.string.save_password))
        }
    }
}

@Composable
private fun PasswordField(value: String, onChange: (String) -> Unit, label: String, tag: String, enabled: Boolean, done: Boolean = false, onDone: () -> Unit = {}) {
    var visible by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    OutlinedTextField(value, onChange, Modifier.fillMaxWidth().testTag(tag), enabled = enabled, singleLine = true,
        label = { Text(label) }, textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false,
            imeAction = if (done) ImeAction.Done else ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }, onDone = { onDone() }),
        trailingIcon = { TextButton(onClick = { visible = !visible }) { Text(stringResource(if (visible) R.string.hide_password else R.string.show_password)) } })
}
