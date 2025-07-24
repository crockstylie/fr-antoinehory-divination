package fr.antoinehory.divination.ui.theme

import android.app.Activity
import android.graphics.Color as AndroidColor // Alias pour éviter conflit avec Compose Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    error = androidx.compose.ui.graphics.Color(0xFFCF6679), // Utilise Compose Color ici
    onError = androidx.compose.ui.graphics.Color(0xFF000000) // Utilise Compose Color ici
)

@Composable
fun DivinationAppTheme( // Tu peux renommer en OrakniumAppTheme
    content: @Composable () -> Unit
) {
    val colorScheme = OrakniumDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Définit la couleur des barres système pour être transparentes
            window.statusBarColor = AndroidColor.TRANSPARENT // Utilise l'alias AndroidColor
            window.navigationBarColor = AndroidColor.TRANSPARENT // Utilise l'alias AndroidColor

            // S'assurer que les icônes des barres système sont claires (car fond d'application sombre)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}