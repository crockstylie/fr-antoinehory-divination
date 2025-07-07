package fr.antoinehory.divination.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Palette de couleurs pour le mode sombre
private val DarkColorScheme = darkColorScheme(
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkBackground, // Pour la simplicité, surface = background
    onSurface = DarkOnBackground, // Pour la simplicité, onSurface = onBackground
    primary = DarkPrimary,
    onPrimary = DarkOnBackground,
    error = DarkError, // Ajout de la couleur d'erreur
    onError = DarkOnBackground // Texte sur la couleur d'erreur
)

// Palette de couleurs pour le mode clair
private val LightColorScheme = lightColorScheme(
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightBackground, // Pour la simplicité, surface = background
    onSurface = LightOnBackground, // Pour la simplicité, onSurface = onBackground
    primary = LightPrimary,
    onPrimary = Color.White, // Texte sur la couleur primaire
    error = LightError, // Ajout de la couleur d'erreur
    onError = Color.White // Texte sur la couleur d'erreur
)

@Composable
fun DivinationAppTheme( // Nom du thème utilisé dans l'application
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Défini dans Type.kt
        content = content
    )
}