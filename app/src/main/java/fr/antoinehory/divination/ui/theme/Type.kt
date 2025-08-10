package fr.antoinehory.divination.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Defines the custom typography settings for the Divination application.
 * This [Typography] object configures various text styles used throughout the app,
 * such as `bodyLarge` for main text content and `labelMedium` for smaller labels.
 * These styles are applied when [MaterialTheme] is used with this typography.
 */
val Typography = Typography(
    /**
     * Style for large body text.
     * Used for primary text content that needs to be prominent.
     * - Font Family: Default system font.
     * - Font Weight: Normal.
     * - Font Size: 36sp.
     * - Line Height: 42sp.
     * - Letter Spacing: 0.5sp.
     */
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.5.sp
    ),
    /**
     * Style for medium-sized labels.
     * Often used for button text, captions, or other less prominent textual elements.
     * - Font Family: Default system font.
     * - Font Weight: Normal.
     * - Font Size: 16sp.
     */
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    )
)

