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
import androidx.lifecycle.viewmodel.compose.viewModel // Pour la preview
import fr.antoinehory.divination.MagicBallViewModel
import fr.antoinehory.divination.ui.common.AppScreen // Importe ton nouveau composable
import fr.antoinehory.divination.ui.theme.DivinationAppTheme // ou OrakniumAppTheme

@Composable
fun MagicBallScreen(
    viewModel: MagicBallViewModel,
    onNavigateBack: () -> Unit // Ajoute le callback de navigation retour
) {
    val responseText by viewModel.currentResponse
    val isShuffling by viewModel.isShuffling
    val lifecycleOwner = LocalLifecycleOwner.current

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

    AppScreen(
        title = "Boule Magique",
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Important
                .windowInsetsPadding(WindowInsets.safeDrawing) // Garde-le si tu veux un contrôle fin
                .padding(horizontal = 32.dp, vertical = 16.dp), // Ajuste le padding si besoin
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (viewModel.isAccelerometerAvailable.value == false && responseText.contains("Secouez")) {
                Text(
                    text = "Accéléromètre non disponible.",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = responseText,
                    style = MaterialTheme.typography.headlineSmall, // Peut-être un style plus grand
                    textAlign = TextAlign.Center,
                    color = textColor,
                    modifier = Modifier.alpha(textAlpha)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "MagicBallScreen - Idle")
@Composable
fun MagicBallScreenPreviewIdle() {
    DivinationAppTheme {
        // Pour la preview, tu peux créer un ViewModel factice ou ne pas en passer
        // et simuler l'état.
        val fakeViewModel: MagicBallViewModel = viewModel() // Juste pour la compilation de la preview
        MagicBallScreen(viewModel = fakeViewModel, onNavigateBack = {})
    }
}

