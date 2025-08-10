package fr.antoinehory.divination.ui.theme

import android.app.Activity
// import android.graphics.Color as AndroidColor // Alias plus nécessaire si on ne définit plus les couleurs directement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color // Garder pour les couleurs du ColorScheme
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
    error = Color(0xFFCF6679), // Utilise Compose Color ici
    onError = Color(0xFF000000) // Utilise Compose Color ici
)

@Composable
fun DivinationAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = OrakniumDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Indique que l'application gérera les insets pour un affichage edge-to-edge
            // Cela rendra les barres système transparentes par défaut.
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // S'assurer que les icônes des barres système sont claires (car fond d'application sombre)
            // Cette partie reste essentielle.
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
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
