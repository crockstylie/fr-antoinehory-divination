package fr.antoinehory.divination.ui.screens

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
import androidx.compose.ui.platform.LocalContext // AJOUT: Pour LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.InteractionMode
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
import fr.antoinehory.divination.viewmodels.RPSOutcome
import fr.antoinehory.divination.viewmodels.RockPaperScissorsViewModel
// AJOUTS:
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.viewmodels.RockPaperScissorsViewModelFactory

@Composable
fun RockPaperScissorsScreen(
    onNavigateBack: () -> Unit,
    // rpsViewModel: RockPaperScissorsViewModel = viewModel(), // Retiré pour initialisation avec factory
    interactionViewModel: InteractionDetectViewModel = viewModel()
) {
    // AJOUT: Récupération du LaunchLogRepository et Application
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication
    val launchLogRepository = application.launchLogRepository

    // AJOUT: Initialisation du RockPaperScissorsViewModel avec la factory
    val rpsViewModel: RockPaperScissorsViewModel = viewModel(
        factory = RockPaperScissorsViewModelFactory(application, launchLogRepository)
    )

    val currentMessage by rpsViewModel.currentMessage.collectAsState()
    val rpsOutcome by rpsViewModel.rpsOutcome.collectAsState()
    val isProcessing by rpsViewModel.isProcessing.collectAsState()

    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()

    LaunchedEffect(interactionViewModel, rpsViewModel, isProcessing) {
        interactionViewModel.interactionTriggered.collect { _event ->
            if (!isProcessing) {
                rpsViewModel.playGame()
            }
        }
    }

    val imageAlpha by animateFloatAsState(
        targetValue = if (isProcessing || rpsOutcome == null) 0f else 1f,
        animationSpec = tween(durationMillis = 300, delayMillis = if (isProcessing) 0 else 100),
        label = "rpsImageAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (isProcessing && rpsOutcome == null) 0.6f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "rpsTextAlpha"
    )

    AppScaffold(
        title = stringResource(id = R.string.rps_screen_title),
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .clickable {
                    if (!isProcessing) {
                        if (interactionPrefs.activeInteractionMode == InteractionMode.TAP) {
                            interactionViewModel.userTappedScreen()
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val initialGenericMessage = stringResource(id = R.string.rps_initial_prompt_generic)
            val noShakeInteractionPossible = interactionPrefs.activeInteractionMode == InteractionMode.SHAKE && !isShakeAvailable

            if (noShakeInteractionPossible && currentMessage == initialGenericMessage) {
                Text(
                    text = stringResource(id = R.string.rps_no_interaction_method_active),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                val shouldShowImage = rpsOutcome != null && !isProcessing

                if (shouldShowImage) {
                    val painterId = when (rpsOutcome) {
                        RPSOutcome.ROCK -> R.drawable.ic_rps_rock
                        RPSOutcome.PAPER -> R.drawable.ic_rps_paper
                        RPSOutcome.SCISSORS -> R.drawable.ic_rps_scissors
                        null -> null // Should not happen if shouldShowImage is true
                    }
                    val contentDescId = when (rpsOutcome) {
                        RPSOutcome.ROCK -> R.string.rps_icon_description_rock
                        RPSOutcome.PAPER -> R.string.rps_icon_description_paper
                        RPSOutcome.SCISSORS -> R.string.rps_icon_description_scissors
                        null -> R.string.general_content_description_empty // Should not happen
                    }

                    if (painterId != null) {
                        Image(
                            painter = painterResource(id = painterId),
                            contentDescription = stringResource(id = contentDescId),
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(imageAlpha)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = currentMessage,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RockPaperScissorsScreenPreview() {
    DivinationAppTheme {
        // RockPaperScissorsScreen(onNavigateBack = {}) // Commenté pour l'''instant
        Text("Preview for RockPaperScissorsScreen needs adjustment for ViewModel with repository.")
    }
}
