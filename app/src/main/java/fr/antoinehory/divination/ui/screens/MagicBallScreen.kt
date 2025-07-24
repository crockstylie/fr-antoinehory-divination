package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import fr.antoinehory.divination.MagicBallViewModel // Assure-toi que l'import est correct
import fr.antoinehory.divination.ui.theme.DivinationAppTheme

@Composable
fun MagicBallScreen(
    viewModel: MagicBallViewModel // On passe directement le ViewModel
) {
    val responseText by viewModel.currentResponse
    val isShuffling by viewModel.isShuffling
    val lifecycleOwner = LocalLifecycleOwner.current

    // Gérer l'enregistrement et le désenregistrement du capteur via le ViewModel
    // en fonction du cycle de vie de cet écran.
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.registerSensorListener()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.unregisterSensorListener()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Assure-toi aussi de désenregistrer si l'écran est détruit et pas seulement mis en pause
            viewModel.unregisterSensorListener()
        }
    }

    val textAlpha by animateFloatAsState(
        targetValue = if (isShuffling) 0.6f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "textAlpha"
    )

    val textColor by animateColorAsState(
        targetValue = if (isShuffling) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(durationMillis = 300),
        label = "textColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing) // S'assurer que le contenu est dans la zone sûre
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (viewModel.isAccelerometerAvailable.value == false && responseText.contains("Secouez")) {
            Text(
                text = "Accéléromètre non disponible sur cet appareil.",
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                text = responseText,
                style = MaterialTheme.typography.bodyLarge, // Ou headlineMedium pour plus d'impact
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier.alpha(textAlpha)
            )
        }
    }
}

@Preview(showBackground = true, name = "MagicBallScreen - Idle")
@Composable
fun MagicBallScreenPreviewIdle() {
    DivinationAppTheme {
        // Pour la preview, on ne peut pas facilement instancier un ViewModel réel avec des dépendances.
        // On pourrait créer un faux ViewModel ou passer des valeurs en dur.
        // Ici, un exemple simple sans ViewModel pour la preview visuelle.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "C'est certain.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, name = "MagicBallScreen - Shuffling")
@Composable
fun MagicBallScreenPreviewShuffling() {
    DivinationAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Réponse brumeuse...",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(0.6f)
            )
        }
    }
}
