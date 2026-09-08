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
    primary = Color(0xFF8B7CFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2C2752),
    onPrimaryContainer = Color(0xFFE9E5FF),
    secondary = Color(0xFF69B8FF),
    secondaryContainer = Color(0xFF17324A),
    onSecondaryContainer = Color(0xFFD9EEFF),
    background = Color(0xFF090A10),
    onBackground = Color(0xFFF6F5FA),
    surface = Color(0xFF11131B),
    onSurface = Color(0xFFF6F5FA),
    surfaceVariant = Color(0xFF191C27),
    onSurfaceVariant = Color(0xFF9B9EAC),
    outline = Color(0xFF303342),
    outlineVariant = Color(0xFF252834),
    error = Color(0xFFFF6B7A)
)

private val Type = Typography().run { copy(
    displaySmall = displaySmall.copy(fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp),
    headlineLarge = headlineLarge.copy(fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp),
    headlineMedium = headlineMedium.copy(fontSize = 23.sp, fontWeight = FontWeight.Bold),
    titleLarge = titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = bodyLarge.copy(fontSize = 14.sp),
    bodyMedium = bodyMedium.copy(fontSize = 12.sp),
    labelLarge = labelLarge.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
) }

@Composable
fun PdfNotesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Dark,
        typography = Type,
        shapes = androidx.compose.material3.Shapes(
            small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
        ),
        content = content
    )
}
