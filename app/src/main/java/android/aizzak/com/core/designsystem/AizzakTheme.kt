package android.aizzak.com.core.designsystem

import android.aizzak.com.core.model.ThemePreference
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF245AE8), onPrimary = Color.White,
    primaryContainer = Color(0xFFE0EAFF), onPrimaryContainer = Color(0xFF2143A0),
    secondary = Color(0xFF006B80), onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9F7FF), onSecondaryContainer = Color(0xFF006B80),
    tertiary = Color(0xFF6842CB), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF0E8FF), onTertiaryContainer = Color(0xFF6842CB),
    background = Color(0xFFF3F7FF), onBackground = Color(0xFF172A4D),
    surface = Color.White, onSurface = Color(0xFF172A4D),
    surfaceVariant = Color(0xFFE9EFFB), onSurfaceVariant = Color(0xFF52627C),
    outline = Color(0xFF7384A0), outlineVariant = Color(0xFFD5DFF0),
    error = Color(0xFFB4233F), onError = Color.White,
    errorContainer = Color(0xFFFFF0F3), onErrorContainer = Color(0xFFB4233F),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF91B7FF), onPrimary = Color(0xFF112852),
    primaryContainer = Color(0xFF304E7A), onPrimaryContainer = Color(0xFFE0EBFF),
    secondary = Color(0xFF6CE6FA), onSecondary = Color(0xFF204D62),
    secondaryContainer = Color(0xFF204D62), onSecondaryContainer = Color(0xFF6CE6FA),
    tertiary = Color(0xFFC6ADFF), onTertiary = Color(0xFF49366B),
    tertiaryContainer = Color(0xFF49366B), onTertiaryContainer = Color(0xFFC6ADFF),
    background = Color(0xFF16233B), onBackground = Color(0xFFF4F7FF),
    surface = Color(0xFF20324F), onSurface = Color(0xFFF4F7FF),
    surfaceVariant = Color(0xFF2B4264), onSurfaceVariant = Color(0xFFC0CEE7),
    outline = Color(0xFF94AACA), outlineVariant = Color(0xFF465F83),
    error = Color(0xFFFFB6C6), onError = Color(0xFF542B40),
    errorContainer = Color(0xFF542B40), onErrorContainer = Color(0xFFFFB6C6),
)

private val AizzakTypography = Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
        headlineMedium = headlineMedium.copy(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = bodyMedium.copy(fontSize = 14.sp, lineHeight = 21.sp),
        labelLarge = labelLarge.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    )
}

private val AizzakShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun AizzakTheme(preference: ThemePreference = ThemePreference.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (preference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AizzakTypography,
        shapes = AizzakShapes,
        content = content,
    )
}
