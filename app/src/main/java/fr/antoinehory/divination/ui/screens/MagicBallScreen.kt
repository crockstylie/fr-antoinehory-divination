package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.InteractionMode // <-- AJOUTÉ
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
import fr.antoinehory.divination.viewmodels.MagicBallViewModel

@Composable
fun MagicBallScreen(
    onNavigateBack: () -> Unit,
    magicBallViewModel: MagicBallViewModel = viewModel(),
    interactionViewModel: InteractionDetectViewModel = viewModel()
) {
    val responseText by magicBallViewModel.currentResponse.collectAsState()
    val isPredicting by magicBallViewModel.isPredicting.collectAsState()

    // Collecter les préférences d'interaction pour afficher un message si aucune n'est activée
    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()
    // Les références à isTapAvailable, isMicrophoneAvailable, isRecordAudioPermissionGranted sont supprimées

    // Observer les déclencheurs d'interaction du InteractionDetectViewModel
    LaunchedEffect(interactionViewModel, magicBallViewModel, isPredicting) { // Ajout de isPredicting aux clés
        interactionViewModel.interactionTriggered.collect { _event ->
            if (!isPredicting) {
                magicBallViewModel.getNewPrediction()
            }
        }
    }

    val textAlpha by animateFloatAsState(
        targetValue = if (isPredicting) 0.6f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "textAlphaMagicBall"
    )

    val textColor by animateColorAsState(
        targetValue = if (isPredicting) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
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
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = responseText,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier
                    .alpha(textAlpha)
                    .weight(1f)
                    .wrapContentHeight(Alignment.CenterVertically)
            )

            Image(
                painter = painterResource(id = R.drawable.magic_ball_default),
                contentDescription = stringResource(R.string.magic_ball_content_description),
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 32.dp)
                    .clickable {
                        if (!isPredicting) {
                            // Si le mode TAP est actif, le clic direct sur l'image
                            // devrait aussi déclencher la prédiction via le système d'interaction.
                            if (interactionPrefs.activeInteractionMode == InteractionMode.TAP) {
                                interactionViewModel.userTappedScreen() // Informe le système de détection de tap
                                // Le magicBallViewModel.getNewPrediction() sera appelé par le LaunchedEffect.
                            } else {
                                // Si TAP n'est pas le mode actif, le clic ne fait rien d'automatique
                                // via le système d'interaction. Comportement de fallback optionnel.
                                // Si vous voulez que le clic fonctionne toujours :
                                // magicBallViewModel.getNewPrediction()
                            }
                        }
                    }
            )

            // Afficher un message si aucune méthode d'interaction n'est active ou disponible
            // dans le cas où le mode SHAKE est actif mais non disponible.
            val initialGenericMessage = stringResource(id = R.string.magic_ball_initial_prompt_generic)
            val noShakeInteractionPossible = interactionPrefs.activeInteractionMode == InteractionMode.SHAKE && !isShakeAvailable

            if (noShakeInteractionPossible && responseText == initialGenericMessage) {
                Text(
                    text = stringResource(id = R.string.magic_ball_no_interaction_method_active),
                    // Vous devrez AJOUTER cette nouvelle chaîne de ressource, par exemple :
                    // <string name="magic_ball_shake_unavailable_prompt">Mode "Secouer" actif mais non disponible. Vérifiez les paramètres ou tapez sur l'écran.</string>
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "MagicBallScreen - Idle")
@Composable
fun MagicBallScreenPreviewIdle() {
    DivinationAppTheme {
        MagicBallScreen(onNavigateBack = {})
    }
}
