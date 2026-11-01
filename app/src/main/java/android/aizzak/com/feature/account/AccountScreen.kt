package android.aizzak.com.feature.account

import android.aizzak.com.R
import android.aizzak.com.core.designsystem.AizzakTheme
import android.aizzak.com.core.designsystem.ConnectedPanel
import android.aizzak.com.core.designsystem.FormPage
import android.aizzak.com.core.designsystem.LockIcon
import android.aizzak.com.core.designsystem.PageTitle
import android.aizzak.com.core.designsystem.PanelDivider
import android.aizzak.com.core.designsystem.SectionLabel
import android.aizzak.com.core.designsystem.SupportingText
import android.aizzak.com.feature.auth.AuthFeedback
import android.aizzak.com.feature.auth.AuthUiState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AccountScreen(
    email: String?,
    displayName: String?,
    onChangePassword: () -> Unit,
    onSignOut: () -> Unit,
    state: AuthUiState? = null,
) {
    val safeEmail = email?.takeIf { it.isNotBlank() }
    val safeName = displayName?.takeIf { it.isNotBlank() }
    FormPage(maxWidth = 620.dp) {
        PageTitle(stringResource(R.string.account))
        SupportingText(stringResource(R.string.account_description))
        state?.let { AuthFeedback(it) }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(70.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(Modifier.size(18.dp).background(MaterialTheme.colorScheme.onPrimaryContainer, CircleShape))
                    Box(
                        Modifier.width(34.dp).height(17.dp)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer, RoundedCornerShape(50)),
                    )
                }
            }
            Text(stringResource(R.string.your_account), style = MaterialTheme.typography.titleLarge)
            safeEmail?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        ConnectedPanel {
            IdentityRow(stringResource(R.string.email_address), safeEmail.orEmpty(), ltr = true)
            safeName?.let {
                PanelDivider()
                IdentityRow(stringResource(R.string.name), it)
            }
        }

        SectionLabel(stringResource(R.string.security))
        ConnectedPanel {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    .clickable(enabled = state?.busy != true, role = Role.Button, onClick = onChangePassword)
                    .testTag("account-change-password").padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LockIcon()
                Text(stringResource(R.string.change_password), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Text(
                    "›",
                    modifier = Modifier.clearAndSetSemantics { },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            shape = MaterialTheme.shapes.small,
        ) {
            TextButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("account-sign-out")
                    .semantics { role = Role.Button },
                enabled = state?.busy != true,
            ) { Text(stringResource(R.string.sign_out), color = MaterialTheme.colorScheme.onErrorContainer) }
        }
    }
}

@Composable
private fun IdentityRow(label: String, value: String, ltr: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(0.42f), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            Modifier.weight(0.58f),
            style = if (ltr) MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr) else MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(name = "Sample account — preview only", showBackground = true)
@Composable
private fun AccountPreview() {
    AizzakTheme { AccountScreen("preview@example.com", "Aizzak User", {}, {}) }
}
