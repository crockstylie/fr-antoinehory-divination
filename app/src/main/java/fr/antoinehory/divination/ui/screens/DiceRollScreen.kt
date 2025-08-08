package fr.antoinehory.divination.ui.screens

import android.widget.Toast // Conservé pour le Toast de l'info dans les Previews
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
// Les imports spécifiques pour l'ancienne bottom bar ne sont plus nécessaires ici
// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.PieChart
// import androidx.compose.material3.BottomAppBar
// import androidx.compose.material3.Icon
// import androidx.compose.material3.IconButton
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
import fr.antoinehory.divination.ui.common.GameHistoryDisplay
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.common.BottomAppNavigationBar // AJOUT: Import de la nouvelle barre de navigation
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
// import fr.antoinehory.divination.ui.theme.OrakniumGold // Plus utilisé pour la bottom bar ici
import fr.antoinehory.divination.viewmodels.DiceRollViewModel
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.viewmodels.DiceRollViewModelFactory

@Composable
fun DiceRollScreen(
    onNavigateBack: () -> Unit,
    interactionViewModel: InteractionDetectViewModel = viewModel(),
    // Paramètres pour la BottomAppNavigationBar
    onNavigateToStats: (GameType) -> Unit, // Conservé, pour les stats spécifiques au jeu
    onNavigateToDiceSetManagement: () -> Unit, // NOUVEAU: pour les "Settings" de ce jeu
    onNavigateToInfo: () -> Unit // NOUVEAU: pour l'écran d'info général
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
    val recentLogs by diceRollViewModel.recentLogs.collectAsState()

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
            // MODIFICATION: Utilisation du nouveau composant de barre de navigation
            BottomAppNavigationBar(
                onSettingsClick = onNavigateToDiceSetManagement, // "Settings" mène à la gestion des sets de dés
                onStatsClick = { onNavigateToStats(GameType.DICE_ROLL) }, // "Stats" mène aux stats de ce jeu
                onInfoClick = onNavigateToInfo // "Info" mène à l'écran d'info général
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
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
                val painterId: Int?
                val contentDesc = when (diceValue) {
                    1 -> {
                        painterId = R.drawable.ic_dice_1
                        stringResource(R.string.dice_icon_description_1)
                    }
                    2 -> {
                        painterId = R.drawable.ic_dice_2
                        stringResource(R.string.dice_icon_description_2)
                    }
                    3 -> {
                        painterId = R.drawable.ic_dice_3
                        stringResource(R.string.dice_icon_description_3)
                    }
                    4 -> {
                        painterId = R.drawable.ic_dice_4
                        stringResource(R.string.dice_icon_description_4)
                    }
                    5 -> {
                        painterId = R.drawable.ic_dice_5
                        stringResource(R.string.dice_icon_description_5)
                    }
                    6 -> {
                        painterId = R.drawable.ic_dice_6
                        stringResource(R.string.dice_icon_description_6)
                    }
                    else -> {
                        painterId = null
                        stringResource(R.string.dice_icon_description_empty)
                    }
                }

                if (painterId != null && !isRolling) {
                    Image(
                        painter = painterResource(id = painterId),
                        contentDescription = contentDesc,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(imageAlpha)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = currentMessage,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )

            GameHistoryDisplay(
                recentLogs = recentLogs,
                gameType = GameType.DICE_ROLL
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DiceRollScreenPreview() {
    val context = LocalContext.current // Pour le Toast
    DivinationAppTheme {
        DiceRollScreen(
            onNavigateBack = {},
            onNavigateToStats = {},
            onNavigateToDiceSetManagement = {}, // AJOUT
            onNavigateToInfo = { Toast.makeText(context, "Info Clicked", Toast.LENGTH_SHORT).show()} // AJOUT
        )
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "DiceRollScreen Landscape")
@Composable
fun DiceRollScreenLandscapePreview() {
    val context = LocalContext.current // Pour le Toast
    DivinationAppTheme {
        DiceRollScreen(
            onNavigateBack = {},
            onNavigateToStats = {},
            onNavigateToDiceSetManagement = {}, // AJOUT
            onNavigateToInfo = { Toast.makeText(context, "Info Clicked", Toast.LENGTH_SHORT).show()} // AJOUT
        )
    }
}

