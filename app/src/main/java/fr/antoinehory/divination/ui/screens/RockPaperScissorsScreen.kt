package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
// import androidx.compose.ui.unit.sp // Plus nécessaire ici directement si GameHistoryDisplay le gère
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.InteractionMode
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.ui.common.AppScaffold
// AJOUT: Import du nouveau composant d'historique
import fr.antoinehory.divination.ui.common.GameHistoryDisplay
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumGold
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
import fr.antoinehory.divination.viewmodels.RPSOutcome
import fr.antoinehory.divination.viewmodels.RockPaperScissorsViewModel
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.viewmodels.RockPaperScissorsViewModelFactory

@Composable
fun RockPaperScissorsScreen(
    onNavigateBack: () -> Unit,
    interactionViewModel: InteractionDetectViewModel = viewModel(),
    onNavigateToStats: (GameType) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication
    val launchLogRepository = application.launchLogRepository

    val rpsViewModel: RockPaperScissorsViewModel = viewModel(
        factory = RockPaperScissorsViewModelFactory(application, launchLogRepository)
    )

    val currentMessage by rpsViewModel.currentMessage.collectAsState()
    val rpsOutcome by rpsViewModel.rpsOutcome.collectAsState() // Type: RPSOutcome?
    val isProcessing by rpsViewModel.isProcessing.collectAsState()
    val recentLogs by rpsViewModel.recentLogs.collectAsState()

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
                    IconButton(onClick = { onNavigateToStats(GameType.ROCK_PAPER_SCISSORS) }) {
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
                .verticalScroll(rememberScrollState())
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
                    // Créer une variable locale non-nullable pour clarifier pour le compilateur
                    val currentOutcome: RPSOutcome = rpsOutcome!! // Garanti non-null par la condition shouldShowImage

                    val painterId = when (currentOutcome) { // when sur une variable non-nullable
                        RPSOutcome.ROCK -> R.drawable.ic_rps_rock
                        RPSOutcome.PAPER -> R.drawable.ic_rps_paper
                        RPSOutcome.SCISSORS -> R.drawable.ic_rps_scissors
                        // Plus besoin de branche null ou else car currentOutcome est non-nullable et RPSOutcome est un enum
                    }
                    val contentDescId = when (currentOutcome) { // when sur une variable non-nullable
                        RPSOutcome.ROCK -> R.string.rps_icon_description_rock
                        RPSOutcome.PAPER -> R.string.rps_icon_description_paper
                        RPSOutcome.SCISSORS -> R.string.rps_icon_description_scissors
                        // Plus besoin de branche null ou else
                    }
                    Image(
                        painter = painterResource(id = painterId),
                        contentDescription = stringResource(id = contentDescId),
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
                gameType = GameType.ROCK_PAPER_SCISSORS
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RockPaperScissorsScreenPreview() {
    DivinationAppTheme {
        RockPaperScissorsScreen(
            onNavigateBack = {},
            onNavigateToStats = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "RockPaperScissorsScreen Landscape")
@Composable
fun RockPaperScissorsScreenLandscapePreview() {
    DivinationAppTheme {
        RockPaperScissorsScreen(
            onNavigateBack = {},
            onNavigateToStats = {}
        )
    }
}
