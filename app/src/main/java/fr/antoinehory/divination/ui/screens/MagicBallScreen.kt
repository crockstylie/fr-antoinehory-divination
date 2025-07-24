package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel // IMPORTANT: Nouvel import
import fr.antoinehory.divination.ui.common.AppScaffold // ou AppScreen si tu l'as renommé
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.MagicBallViewModel // Garder l'import du type ViewModel

@Composable
fun MagicBallScreen(
    onNavigateBack: () -> Unit,
    // Le ViewModel est obtenu ici, avec une valeur par défaut pour les previews/tests si nécessaire
    viewModel: MagicBallViewModel = viewModel()
) {
    val responseText by viewModel.currentResponse.collectAsState()
    val isProcessingShake by viewModel.isProcessingShake.collectAsState() // Assure-toi que c'est le bon nom (hérité)
    val isAccelerometerAvailable by viewModel.isAccelerometerAvailable.collectAsState()

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
        }
    }

    val textAlpha by animateFloatAsState(
        targetValue = if (isProcessingShake) 0.6f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "textAlphaMagicBall"
    )

    val textColor by animateColorAsState(
        targetValue = if (isProcessingShake) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(durationMillis = 300),
        label = "textColorMagicBall"
    )

    AppScaffold( // ou AppScreen
        title = "Boule Magique",
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isAccelerometerAvailable && responseText.contains("Secouez", ignoreCase = true)) {
                Text(
                    text = "Accéléromètre non disponible.",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Text(
                text = responseText,
                style = MaterialTheme.typography.headlineSmall,
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
        // viewModel() sera utilisé ici. Pour les previews plus complexes,
        // on peut injecter un ViewModel factice en modifiant la signature du composable.
        MagicBallScreen(onNavigateBack = {})
    }
}

