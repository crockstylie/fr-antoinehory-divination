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
import fr.antoinehory.divination.viewmodels.DiceRollViewModel
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
// AJOUTS :
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.viewmodels.DiceRollViewModelFactory

@Composable
fun DiceRollScreen(
    onNavigateBack: () -> Unit,
    // diceRollViewModel: DiceRollViewModel = viewModel(), // Retiré pour initialisation avec factory
    interactionViewModel: InteractionDetectViewModel = viewModel()
) {
    // AJOUT: Récupération du LaunchLogRepository et Application
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication
    val launchLogRepository = application.launchLogRepository

    // AJOUT: Initialisation du DiceRollViewModel avec la factory
    val diceRollViewModel: DiceRollViewModel = viewModel(
        factory = DiceRollViewModelFactory(application, launchLogRepository)
    )

    val currentMessage by diceRollViewModel.currentMessage.collectAsState()
    val diceValue by diceRollViewModel.diceValue.collectAsState()
    val isRolling by diceRollViewModel.isRolling.collectAsState()

    // États d'InteractionDetectViewModel pour l'affichage conditionnel de messages
    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()

    // Observer les déclencheurs d'interaction
    LaunchedEffect(interactionViewModel, diceRollViewModel, isRolling) {
        interactionViewModel.interactionTriggered.collect { _event ->
            if (!isRolling) {
                diceRollViewModel.performRoll()
            }
        }
    }

    val imageAlpha by animateFloatAsState(
        targetValue = if (isRolling || diceValue == null) 0f else 1f,
        animationSpec = tween(durationMillis = 300, delayMillis = if (isRolling) 0 else 100),
        label = "diceImageAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (isRolling && diceValue == null) 0.6f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "diceTextAlpha"
    )

    AppScaffold(
        title = stringResource(id = R.string.dice_roll_screen_title),
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .clickable {
                    if (!isRolling) {
                        if (interactionPrefs.activeInteractionMode == InteractionMode.TAP) {
                            interactionViewModel.userTappedScreen()
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val initialGenericMessage = stringResource(id = R.string.dice_initial_prompt_generic)
            val noShakeInteractionPossible = interactionPrefs.activeInteractionMode == InteractionMode.SHAKE && !isShakeAvailable

            if (noShakeInteractionPossible && currentMessage == initialGenericMessage) {
                Text(
                    text = stringResource(id = R.string.dice_no_interaction_method_active),
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
                if (diceValue != null && !isRolling) {
                    val painterId = when (diceValue) {
                        1 -> R.drawable.ic_dice_1
                        2 -> R.drawable.ic_dice_2
                        3 -> R.drawable.ic_dice_3
                        4 -> R.drawable.ic_dice_4
                        5 -> R.drawable.ic_dice_5
                        6 -> R.drawable.ic_dice_6
                        else -> null
                    }
                    val contentDescId = when (diceValue) {
                        1 -> R.string.dice_icon_description_1
                        2 -> R.string.dice_icon_description_2
                        3 -> R.string.dice_icon_description_3
                        4 -> R.string.dice_icon_description_4
                        5 -> R.string.dice_icon_description_5
                        6 -> R.string.dice_icon_description_6
                        else -> R.string.dice_icon_description_empty
                    }

                    painterId?.let {
                        Image(
                            painter = painterResource(id = it),
                            contentDescription = stringResource(id = contentDescId),
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(imageAlpha)
                        )
                    }
                } else if (isRolling) {
                    // Optionnel : Afficher un CircularProgressIndicator ou une animation de "lancement"
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
fun DiceRollScreenPreview() {
    DivinationAppTheme {
        // DiceRollScreen(onNavigateBack = {}) // Commenté pour l'instant
        Text("Preview for DiceRollScreen needs adjustment for ViewModel with repository.")
    }
}
