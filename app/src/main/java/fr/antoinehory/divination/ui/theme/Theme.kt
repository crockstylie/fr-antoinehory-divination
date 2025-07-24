package fr.antoinehory.divination.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme // On pourrait supprimer cet import
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Définition du ColorScheme pour le mode sombre uniquement
private val OrakniumDarkColorScheme = darkColorScheme(
    primary = OrakniumGold, // Couleur principale pour les éléments interactifs clés
    onPrimary = OrakniumBackground, // Couleur du texte/icônes SUR la couleur primaire (ex: texte sur un bouton plein OrakniumGold)
    secondary = OrakniumGold, // Couleur secondaire (peut être la même si tu as un style simple)
    onSecondary = OrakniumBackground,
    tertiary = OrakniumGold, // Couleur tertiaire
    onTertiary = OrakniumBackground,
    background = OrakniumBackground, // Couleur de fond principale des écrans
    onBackground = OrakniumGold, // Couleur du texte et des icônes sur la couleur de fond
    surface = OrakniumSurface, // Couleur des surfaces de composants (Cards, Menus, etc.)
    onSurface = OrakniumGold, // Couleur du texte et des icônes sur les surfaces
    outline = OrakniumGold, // Couleur pour les bordures (comme pour tes boutons)
    error = Color(0xFFCF6679), // Garde une couleur d'erreur standard ou définis la tienne
    onError = Color(0xFF000000)
    // Tu peux ajuster d'autres couleurs comme surfaceVariant, primaryContainer, etc., si besoin.
)

@Composable
fun DivinationAppTheme( // Tu peux renommer cette fonction en OrakniumAppTheme si tu veux
    // darkTheme: Boolean = isSystemInDarkTheme(), // On force le mode sombre
    // Dynamic color is available on Android 12+
    // dynamicColor: Boolean = true, // On désactive la couleur dynamique pour imposer notre thème
    content: @Composable () -> Unit
) {
    val colorScheme = OrakniumDarkColorScheme // Toujours utiliser notre scheme sombre

    // Gérer la couleur de la barre d'état système (celle du haut avec l'heure)
    // et la barre de navigation (celle du bas avec les boutons système)
    // Ceci est important pour que les barres système s'intègrent bien avec ton fond sombre.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // Barre d'état avec la couleur de fond
            window.navigationBarColor = colorScheme.background.toArgb() // Barre de navigation avec la couleur de fond

            // Définit les icônes de la barre d'état en clair (car fond sombre)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            // Définit les icônes de la barre de navigation en clair (car fond sombre)
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assure-toi que Typography est bien défini dans typography.kt
        shapes = Shapes,         // Assure-toi que Shapes est bien défini dans shapes.kt
        content = content
    )
}
