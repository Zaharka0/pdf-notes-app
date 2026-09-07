package com.pdfnotes.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Dark = darkColorScheme(
    primary = Color(0xFF2F7CF6), onPrimary = Color.White,
    primaryContainer = Color(0xFF183B73), onPrimaryContainer = Color(0xFFDCE9FF),
    secondary = Color(0xFF9AA7BC), secondaryContainer = Color(0xFF242C3A), onSecondaryContainer = Color(0xFFE7EBF2),
    background = Color(0xFF080B11), onBackground = Color(0xFFF4F6FA),
    surface = Color(0xFF11151D), onSurface = Color(0xFFF4F6FA),
    surfaceVariant = Color(0xFF1A202B), onSurfaceVariant = Color(0xFF9EA7B7),
    outline = Color(0xFF303846), outlineVariant = Color(0xFF252C38), error = Color(0xFFFF6B73)
)

private val Type = Typography().run { copy(
    displaySmall = displaySmall.copy(fontSize = 30.sp, fontWeight = FontWeight.Bold),
    headlineLarge = headlineLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
    headlineMedium = headlineMedium.copy(fontSize = 23.sp, fontWeight = FontWeight.Bold),
    titleLarge = titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = bodyLarge.copy(fontSize = 14.sp), bodyMedium = bodyMedium.copy(fontSize = 12.sp),
    labelLarge = labelLarge.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
) }

@Composable
fun PdfNotesTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Dark, typography = Type,
        shapes = androidx.compose.material3.Shapes(
            small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)), content = content)
}
