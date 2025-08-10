package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import fr.antoinehory.divination.data.model.InteractionMode
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.common.BottomAppNavigationBar
import fr.antoinehory.divination.ui.common.GameHistoryDisplay
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
import fr.antoinehory.divination.viewmodels.RPSOutcome
import fr.antoinehory.divination.viewmodels.RockPaperScissorsViewModel
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.viewmodels.RockPaperScissorsViewModelFactory

/**
 * Composable screen for the Rock Paper Scissors game.
 * It allows users to play a game of RPS, triggered by tap or shake interactions.
 * The screen displays the outcome with an image and text, and shows recent game history.
 *
 * @param onNavigateBack Callback to navigate to the previous screen.
 * @param interactionViewModel ViewModel for detecting user interactions (tap/shake). Defaults to a new instance.
 * @param onNavigateToStats Callback to navigate to the statistics screen for the RPS game.
 * @param onNavigateToInfo Callback to navigate to the application information screen.
 */
@Composable
fun RockPaperScissorsScreen(
    onNavigateBack: () -> Unit,
    interactionViewModel: InteractionDetectViewModel = viewModel(),
    onNavigateToStats: (GameType) -> Unit,
    onNavigateToInfo: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication
    val launchLogRepository = application.launchLogRepository // Used by the ViewModel factory.

    // ViewModel for managing RPS game logic and state.
    val rpsViewModel: RockPaperScissorsViewModel = viewModel(
        factory = RockPaperScissorsViewModelFactory(application, launchLogRepository)
    )

    // Collecting state from RockPaperScissorsViewModel.
    val currentMessage by rpsViewModel.currentMessage.collectAsState() // Text message (e.g., "Rock!", "You Win!").
    val rpsOutcome by rpsViewModel.rpsOutcome.collectAsState()         // The outcome of the game (Rock, Paper, or Scissors).
    val isProcessing by rpsViewModel.isProcessing.collectAsState()     // True if the game is currently processing a play.
    val recentLogs by rpsViewModel.recentLogs.collectAsState()         // List of recent game logs.

    // Collecting state from InteractionDetectViewModel.
    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState() // User's preferred interaction mode.
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()     // True if shake detection is available.

    // Triggers a new game when an interaction is detected and not currently processing.
    LaunchedEffect(interactionViewModel, rpsViewModel, isProcessing) {
        interactionViewModel.interactionTriggered.collect { _event ->
            if (!isProcessing) {
                rpsViewModel.playGame()
            }
        }
    }

    // Animation for the alpha of the outcome image. Fades in when processing is done and an outcome exists.
    val imageAlpha by animateFloatAsState(
        targetValue = if (isProcessing || rpsOutcome == null) 0f else 1f, // Fade out if processing or no outcome.
        animationSpec = tween(durationMillis = 300, delayMillis = if (isProcessing) 0 else 100), // Delay fade-in slightly.
        label = "rpsImageAlpha" // Label for animation tooling.
    )
    // Animation for the alpha of the message text. Dims slightly if processing with no outcome image yet.
    val textAlpha by animateFloatAsState(
        targetValue = if (isProcessing && rpsOutcome == null) 0.6f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "rpsTextAlpha" // Label for animation tooling.
    )

    AppScaffold(
        title = stringResource(id = R.string.rps_screen_title),
        canNavigateBack = true,
        onNavigateBack = onNavigateBack,
        bottomBar = {
            BottomAppNavigationBar(
                onSettingsClick = {}, // Settings button is not used/shown in this screen.
                onStatsClick = { onNavigateToStats(GameType.ROCK_PAPER_SCISSORS) },
                onInfoClick = onNavigateToInfo,
                showSettingsButton = false // Explicitly hide settings button.
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from the Scaffold.
                .verticalScroll(rememberScrollState()) // Allow content to scroll.
                .padding(16.dp) // Inner padding for the Column content.
                .clickable { // Handles tap interaction for playing the game.
                    if (!isProcessing) {
                        if (interactionPrefs.activeInteractionMode == InteractionMode.TAP) {
                            interactionViewModel.userTappedScreen()
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Check if shake interaction is preferred but unavailable, and the initial prompt is shown.
            val initialGenericMessage = stringResource(id = R.string.rps_initial_prompt_generic)
            val noShakeInteractionPossible = interactionPrefs.activeInteractionMode == InteractionMode.SHAKE && !isShakeAvailable

            // Display a warning message if shake is selected but unavailable, and the initial prompt is visible.
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

            // Box to display the outcome image (Rock, Paper, or Scissors).
            Box(
                modifier = Modifier.size(120.dp), // Fixed size for the image container.
                contentAlignment = Alignment.Center
            ) {
                val shouldShowImage = rpsOutcome != null && !isProcessing // Show image if outcome exists and not processing.

                if (shouldShowImage) {
                    val currentOutcome: RPSOutcome = rpsOutcome!! // Safe non-null assertion due to shouldShowImage check.

                    // Determine the drawable resource ID based on the game outcome.
                    val painterId = when (currentOutcome) {
                        RPSOutcome.ROCK -> R.drawable.ic_rps_rock
                        RPSOutcome.PAPER -> R.drawable.ic_rps_paper
                        RPSOutcome.SCISSORS -> R.drawable.ic_rps_scissors
                    }
                    // Determine the content description string resource ID for accessibility.
                    val contentDescId = when (currentOutcome) {
                        RPSOutcome.ROCK -> R.string.rps_icon_description_rock
                        RPSOutcome.PAPER -> R.string.rps_icon_description_paper
                        RPSOutcome.SCISSORS -> R.string.rps_icon_description_scissors
                    }
                    Image(
                        painter = painterResource(id = painterId),
                        contentDescription = stringResource(id = contentDescId),
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(imageAlpha) // Apply fade animation.
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display the current message (e.g., outcome, prompt).
            Text(
                text = currentMessage,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha) // Apply fade animation.
            )

            // Display recent game history.
            GameHistoryDisplay(
                recentLogs = recentLogs,
                gameType = GameType.ROCK_PAPER_SCISSORS
            )

            Spacer(modifier = Modifier.height(16.dp)) // Spacer at the bottom.
        }
    }
}

/**
 * Preview composable for the [RockPaperScissorsScreen].
 * This preview shows the screen in its default state.
 */
@Preview(showBackground = true)
@Composable
fun RockPaperScissorsScreenPreview() {
    DivinationAppTheme {
        RockPaperScissorsScreen(
            onNavigateBack = {},
            onNavigateToStats = {},
            onNavigateToInfo = {}
        )
    }
}

/**
 * Preview composable for the [RockPaperScissorsScreen] in landscape orientation.
 * Demonstrates how the screen might look with different width/height ratios.
 */
@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "RockPaperScissorsScreen Landscape")
@Composable
fun RockPaperScissorsScreenLandscapePreview() {
    DivinationAppTheme {
        RockPaperScissorsScreen(
            onNavigateBack = {},
            onNavigateToStats = {},
            onNavigateToInfo = {}
        )
    }
}

