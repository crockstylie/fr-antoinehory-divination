package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // AJOUT
import androidx.compose.foundation.verticalScroll // AJOUT
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.InteractionMode
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumGold
import fr.antoinehory.divination.viewmodels.DiceRollViewModel
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.viewmodels.DiceRollViewModelFactory

@Composable
fun DiceRollScreen(
    onNavigateBack: () -> Unit,
    interactionViewModel: InteractionDetectViewModel = viewModel(),
    onNavigateToStats: (GameType) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication
    val launchLogRepository = application.launchLogRepository

    val diceRollViewModel: DiceRollViewModel = viewModel(
        factory = DiceRollViewModelFactory(application, launchLogRepository)
    )

    val currentMessage by diceRollViewModel.currentMessage.collectAsState()
    val diceValue by diceRollViewModel.diceValue.collectAsState()
    val isRolling by diceRollViewModel.isRolling.collectAsState()

    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()

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
        onNavigateBack = onNavigateBack,
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = OrakniumGold
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigateToStats(GameType.DICE_ROLL) }) {
                        Icon(
                            imageVector = Icons.Filled.PieChart,
                            contentDescription = stringResource(id = R.string.game_stats_icon_description),
                            tint = OrakniumGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // AJOUT DU DÉFILEMENT
                .padding(16.dp)
                .clickable {
                    if (!isRolling) {
                        if (interactionPrefs.activeInteractionMode == InteractionMode.TAP) {
                            interactionViewModel.userTappedScreen()
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Gardé pour centrer si le contenu est petit
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

            // Ajout d'un Spacer en bas pour un meilleur espacement en mode défilement
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiceRollScreenPreview() {
    DivinationAppTheme {
        DiceRollScreen(
            onNavigateBack = {},
            onNavigateToStats = {}
        )
    }
}

// Ajout d'une preview paysage pour tester le défilement
@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "DiceRollScreen Landscape")
@Composable
fun DiceRollScreenLandscapePreview() {
    DivinationAppTheme {
        DiceRollScreen(
            onNavigateBack = {},
            onNavigateToStats = {}
        )
    }
}
