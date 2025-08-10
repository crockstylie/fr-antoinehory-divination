package fr.antoinehory.divination.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Defines the dark color scheme for the Oraknium theme.
 * This scheme is used when the application is in dark mode (which is the default for this app).
 * It specifies the primary, secondary, tertiary, background, surface, and error colors,
 * as well as the "on" colors used for text and icons displayed on top of these main colors.
 */
private val OrakniumDarkColorScheme = darkColorScheme(
    primary = OrakniumGold,
    onPrimary = OrakniumBackground,
    secondary = OrakniumGold,
    onSecondary = OrakniumBackground,
    tertiary = OrakniumGold,
    onTertiary = OrakniumBackground,
    background = OrakniumBackground,
    onBackground = OrakniumGold,
    surface = OrakniumSurface,
    onSurface = OrakniumGold,
    outline = OrakniumGold,
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000)
)

/**
 * The main theme composable for the Divination application.
 * This function applies the [OrakniumDarkColorScheme], custom [Typography], and [Shapes]
 * to the Jetpack Compose UI.
 *
 * It also includes a [SideEffect] to configure system UI elements like the status bar
 * and navigation bar to achieve an edge-to-edge display and set their appearance
 * to dark to match the app's theme. This effect runs when the composable enters
 * the composition and is not executed during preview mode.
 *
 * @param content The composable content to which this theme will be applied.
 */
@Composable
fun DivinationAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = OrakniumDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        // This SideEffect runs once when DivinationAppTheme enters the composition.
        // It's used to interact with the Android Window to set up edge-to-edge display
        // and control the appearance of system bars.
        SideEffect {
            val window = (view.context as Activity).window

            // Allows the app content to draw behind the system bars (status bar, navigation bar).
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val insetsController = WindowCompat.getInsetsController(window, view)
            // Sets the status bar icons and text to be light (suitable for dark backgrounds).
            insetsController.isAppearanceLightStatusBars = false
            // Sets the navigation bar icons to be light (suitable for dark backgrounds).
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

