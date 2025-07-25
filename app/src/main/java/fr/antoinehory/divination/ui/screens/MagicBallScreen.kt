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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.MagicBallViewModel

@Composable
fun MagicBallScreen(
    onNavigateBack: () -> Unit,
    viewModel: MagicBallViewModel = viewModel()
) {
    val responseText by viewModel.currentResponse.collectAsState()
    val isProcessingInteraction by viewModel.isProcessingShake.collectAsState()
    val isAccelerometerAvailable by viewModel.isAccelerometerAvailable.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.registerSensorListener() // Méthode de ShakeDetectViewModel
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.unregisterSensorListener() // Méthode de ShakeDetectViewModel
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val textAlpha by animateFloatAsState(
        targetValue = if (isProcessingInteraction) 0.6f else 1.0f, // Utilise isProcessingInteraction
        animationSpec = tween(durationMillis = 300),
        label = "textAlphaMagicBall"
    )

    val textColor by animateColorAsState(
        targetValue = if (isProcessingInteraction) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f) // Utilise isProcessingInteraction
        else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(durationMillis = 300),
        label = "textColorMagicBall"
    )

    AppScaffold(
        title = stringResource(id = R.string.magic_ball_screen_title),
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
            // La logique pour afficher le message d'erreur de l'accéléromètre est affinée.
            // Le ViewModel définit déjà un message spécifique si l'accéléromètre n'est pas là au démarrage.
            // On peut afficher un message d'erreur plus générique de l'UI si l'accéléromètre
            // n'est pas disponible et que le message actuel est celui invitant à secouer.
            val initialNoAccelerometerText = stringResource(id = R.string.magic_ball_initial_prompt_no_accelerometer)
            if (!isAccelerometerAvailable && responseText == initialNoAccelerometerText) {
                Text(
                    // Utilise la chaîne de ressource plus générique pour l'UI si besoin,
                    // ou tu peux aussi te fier uniquement au message du ViewModel.
                    // Ici, on utilise la string spécifique de l'UI.
                    text = stringResource(id = R.string.magic_ball_accelerometer_not_available_ui_message), // << MODIFIÉ
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Text(
                text = responseText, // Provient du ViewModel, qui utilise déjà des ressources.
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
        // Pour la preview, le ViewModel utilisera les ressources par défaut (français ici).
        MagicBallScreen(onNavigateBack = {})
    }
}