package android.aizzak.com.feature.home

import android.aizzak.com.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Compact reference layout; the wide layout keeps its own independent policy. */
@Composable
internal fun HomePhoneContent(assistants: List<AssistantCardModel>) {
    val colors = MaterialTheme.colorScheme
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val fontScale = LocalDensity.current.fontScale
    Column(Modifier.fillMaxSize().background(colors.background)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 17.dp, bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Image(painterResource(R.drawable.applogo), null, Modifier.size(36.dp))
            Column {
                Text(stringResource(R.string.app_name), style = phoneText(14.08f, 19f, 650))
                Spacer(Modifier.height(2.dp))
                Text(stringResource(R.string.app_tagline), color = colors.onSurfaceVariant, style = phoneText(10.14f, 13.7f))
            }
        }
        Column(
            Modifier.fillMaxSize().testTag("home-phone-scroll").verticalScroll(rememberScrollState())
                .padding(start = 22.dp, end = 22.dp, top = 17.dp, bottom = 28.dp),
        ) {
            Text(stringResource(R.string.home_eyebrow).uppercase(), color = colors.primary,
                style = phoneText(10.56f, 15.84f, 650).copy(letterSpacing = 1.16.sp))
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.home_title),
                Modifier.fillMaxWidth().testTag("home-phone-title").semantics { heading() },
                style = phoneText(28f, 33.6f, 650).copy(
                    letterSpacing = if (rtl) 0.sp else (-0.98).sp,
                    textDirection = if (rtl) TextDirection.Rtl else TextDirection.Ltr,
                ),
                maxLines = if (fontScale <= 1.2f) 1 else Int.MAX_VALUE,
            )
            Spacer(Modifier.height(11.dp))
            Text(stringResource(R.string.home_description), color = colors.onSurfaceVariant, style = phoneText(14.08f, 23.23f))
            Spacer(Modifier.height(24.dp))
            if (assistants.isEmpty()) Text(stringResource(R.string.no_assistants), style = phoneText(14.08f, 23.23f))
            assistants.forEachIndexed { index, assistant ->
                if (index > 0) Spacer(Modifier.height(14.dp))
                PhoneAssistantCard(assistant)
            }
            Spacer(Modifier.height(22.dp))
            Surface(Modifier.testTag("home-phone-notice"), color = colors.primaryContainer, shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(PhoneIcons.info, null, Modifier.padding(top = 2.dp).size(19.dp), tint = colors.onPrimaryContainer)
                    Text(stringResource(R.string.assistants_preparing), Modifier.weight(1f), color = colors.onPrimaryContainer,
                        style = phoneText(12.48f, 19.97f))
                }
            }
            Spacer(Modifier.height(22.dp))
        }
    }
}

@Composable
private fun PhoneAssistantCard(item: AssistantCardModel) {
    val colors = MaterialTheme.colorScheme
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val (accent, container, icon) = when (item.accent) {
        AssistantAccent.VIOLET -> Triple(colors.tertiary, colors.tertiaryContainer, PhoneIcons.smart)
        AssistantAccent.CYAN -> Triple(colors.secondary, colors.secondaryContainer, PhoneIcons.search)
        AssistantAccent.AMBER -> Triple(
            if (colors.background == Color(0xFF16233B)) Color(0xFFFFD37C) else Color(0xFF8A5100),
            if (colors.background == Color(0xFF16233B)) Color(0xFF5B4725) else Color(0xFFFFF0CE), PhoneIcons.wire,
        )
    }
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier.fillMaxWidth().testTag("assistant-${item.id}").semantics(mergeDescendants = true) {}
            .clip(shape).background(colors.surface).border(1.dp, colors.outlineVariant, shape)
            .drawWithContent {
                drawContent()
                val stripe = 3.dp.toPx()
                drawRect(accent, Offset(if (rtl) size.width - stripe else 0f, 0f), Size(stripe, size.height))
            }
            .padding(start = 23.dp, end = 21.dp, top = 21.dp, bottom = 21.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(Modifier.size(42.dp).testTag("home-icon-${item.id}").background(container, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(24.dp), tint = accent)
        }
        Column(Modifier.weight(1f)) {
            Text(stringResource(item.name), Modifier.testTag("home-name-${item.id}"),
                style = phoneText(16f, 22.4f, 650).copy(letterSpacing = (-0.24).sp, textDirection = TextDirection.Ltr))
            Spacer(Modifier.height(5.dp))
            Text(stringResource(item.description), style = phoneText(12.8f, 19.84f), color = colors.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Surface(color = colors.surfaceVariant, shape = CircleShape, border = BorderStroke(1.dp, colors.outlineVariant)) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(5.dp).background(colors.onSurfaceVariant, CircleShape))
                    Text(stringResource(R.string.coming_later), style = phoneText(9.92f, 14.88f, 600), color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

/** Reference bottom bar only while compact Home is selected; callbacks retain the shared navigation. */
@Composable
fun HomePhoneNavigation(onHome: () -> Unit, onAccount: () -> Unit, onSettings: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().background(colors.surface).navigationBarsPadding()) {
        HorizontalDivider(color = colors.outlineVariant, thickness = 1.dp)
        Row(Modifier.fillMaxWidth().selectableGroup().padding(start = 12.dp, end = 12.dp, top = 9.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val labels = listOf(R.string.home, R.string.account, R.string.settings)
            val tags = listOf("nav-home", "nav-account", "nav-settings")
            val icons = listOf(PhoneIcons.home, PhoneIcons.user, PhoneIcons.settings)
            val actions = listOf(onHome, onAccount, onSettings)
            labels.forEachIndexed { index, label ->
                val selected = index == 0
                Column(Modifier.weight(1f).heightIn(min = 58.dp).testTag(tags[index])
                    .selectable(selected, role = Role.Tab, onClick = actions[index]).padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(Modifier.size(56.dp, 29.dp).background(if (selected) colors.primary else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(icons[index], null, Modifier.size(21.dp), tint = if (selected) colors.onPrimary else colors.onSurfaceVariant)
                    }
                    Text(stringResource(label), style = phoneText(10.4f, 15.6f, if (selected) 650 else 400),
                        color = if (selected) colors.primary else colors.onSurfaceVariant)
                }
            }
        }
    }
}

private fun phoneText(size: Float, lineHeight: Float, weight: Int = 400) =
    TextStyle(fontSize = size.sp, lineHeight = lineHeight.sp, fontWeight = FontWeight(weight))

/** The actual paths from UI-PREVIEW.html, expressed as stroked Android vectors. */
private object PhoneIcons {
    val smart = outline("Smart", "m12 3 2.5 6.5L21 12l-6.5 2.5L12 21l-2.5-6.5L3 12l6.5-2.5Z")
    val search = outline("Search", "M17 10.5a6.5 6.5 0 1 0-13 0a6.5 6.5 0 1 0 13 0 M16 16l5 5M7 9h7M7 12h5")
    val wire = outline("Wire", "M3 5h6v6h6V5h6M3 19h6v-8M15 11v8h6 M4 5a1 1 0 1 0-2 0a1 1 0 1 0 2 0 M22 5a1 1 0 1 0-2 0a1 1 0 1 0 2 0 M4 19a1 1 0 1 0-2 0a1 1 0 1 0 2 0 M22 19a1 1 0 1 0-2 0a1 1 0 1 0 2 0")
    val info = outline("Info", "M21 12a9 9 0 1 0-18 0a9 9 0 1 0 18 0 M12 11v6M12 7v1")
    val home = outline("Home", "M3 10 12 3l9 7v10a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1Z")
    val user = outline("Account", "M16 8a4 4 0 1 0-8 0a4 4 0 1 0 8 0 M4 21v-2a8 8 0 0 1 16 0v2")
    val settings = outline("Settings", "m9 3-1 3-3 1v4l-2 1 2 2v4l3 1 1 2h6l1-2 3-1v-4l2-2-2-1V7l-3-1-1-3Z M15 12a3 3 0 1 0-6 0a3 3 0 1 0 6 0")
    private fun outline(name: String, path: String) = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        .addPath(PathParser().parsePathString(path).toNodes(), fill = null, stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.65f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round).build()
}
