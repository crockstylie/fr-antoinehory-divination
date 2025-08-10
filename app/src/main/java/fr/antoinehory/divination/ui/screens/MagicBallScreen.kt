package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.InteractionMode
import fr.antoinehory.divination.ui.common.GameHistoryDisplay
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.common.BottomAppNavigationBar
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
import fr.antoinehory.divination.viewmodels.MagicBallViewModel
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.viewmodels.MagicBallViewModelFactory


/**
 * Composable screen for the Magic 8-Ball game.
 * It displays a prediction response that can be triggered by user interaction (tap or shake).
 * The screen also shows recent prediction history and handles UI changes based on interaction mode and availability.
 *
 * @param onNavigateBack Callback to navigate to the previous screen.
 * @param interactionViewModel ViewModel for detecting user interactions (tap/shake). Defaults to a new instance.
 * @param onNavigateToStats Callback to navigate to the statistics screen for the Magic 8-Ball game.
 * @param onNavigateToInfo Callback to navigate to the application information screen.
 */
@Composable
fun MagicBallScreen(
    onNavigateBack: () -> Unit,
    interactionViewModel: InteractionDetectViewModel = viewModel(),
    onNavigateToStats: (GameType) -> Unit,
    onNavigateToInfo: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication

    // ViewModel for managing Magic 8-Ball logic and state.
    val magicBallViewModel: MagicBallViewModel = viewModel(
        factory = MagicBallViewModelFactory(application, application.launchLogRepository)
    )

    // Collecting state from MagicBallViewModel.
    val responseText by magicBallViewModel.currentResponse.collectAsState()
    val isPredicting by magicBallViewModel.isPredicting.collectAsState() // True if a new prediction is being fetched.
    val recentLogs by magicBallViewModel.recentLogs.collectAsState() // List of recent predictions.

    // Collecting state from InteractionDetectViewModel.
    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState() // True if shake detection is available.

    // Triggers a new prediction when an interaction is detected and not currently predicting.
    LaunchedEffect(interactionViewModel, magicBallViewModel, isPredicting) {
        interactionViewModel.interactionTriggered.collect { _event ->
            if (!isPredicting) {
                magicBallViewModel.getNewPrediction()
            }
        }
    }

    // Animation for the alpha of the response text, making it semi-transparent while predicting.
    val textAlpha by animateFloatAsState(
        targetValue = if (isPredicting) 0.6f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "textAlphaMagicBall" // Label for animation tooling.
    )

    // Animation for the color of the response text, dimming it while predicting.
    val textColor by animateColorAsState(
        targetValue = if (isPredicting) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(durationMillis = 300),
        label = "textColorMagicBall" // Label for animation tooling.
    )

    AppScaffold(
        title = stringResource(id = R.string.magic_ball_screen_title),
        canNavigateBack = true,
        onNavigateBack = onNavigateBack,
        actions = {
            // Placeholder for potential future actions in the TopAppBar.
        },
        bottomBar = {
            BottomAppNavigationBar(
                onSettingsClick = {}, // Settings button is not shown/used in this screen.
                onStatsClick = { onNavigateToStats(GameType.MAGIC_EIGHT_BALL) },
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
                .clickable { // Handles tap interaction for getting a new prediction.
                    if (!isPredicting) {
                        if (interactionPrefs.activeInteractionMode == InteractionMode.TAP) {
                            interactionViewModel.userTappedScreen()
                        }
                    }
                }
                .padding(horizontal = 32.dp, vertical = 16.dp), // Inner padding for content.
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display the Magic 8-Ball response text.
            Text(
                text = responseText,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = textColor, // Animated text color.
                modifier = Modifier
                    .alpha(textAlpha) // Animated text alpha.
            )

            // Display recent game history.
            GameHistoryDisplay(
                recentLogs = recentLogs,
                gameType = GameType.MAGIC_EIGHT_BALL
            )

            // Check if shake interaction is preferred but not available, and if the initial prompt is shown.
            val initialGenericMessage = stringResource(id = R.string.magic_ball_initial_prompt_generic)
            val noShakeInteractionPossible = interactionPrefs.activeInteractionMode == InteractionMode.SHAKE && !isShakeAvailable

            // Display a warning message if shake is selected but unavailable, and the initial prompt is visible.
            if (noShakeInteractionPossible && responseText == initialGenericMessage) {
                Spacer(modifier = Modifier.weight(1f)) // Push message to the bottom if space allows.
                Text(
                    text = stringResource(id = R.string.magic_ball_no_interaction_method_active),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp)) // Default spacer otherwise.
            }
        }
    }
}

/**
 * Preview composable for the [MagicBallScreen] in its idle state.
 * This preview shows the screen with default values, without active interaction.
 */
@Preview(showBackground = true, name = "MagicBallScreen - Idle")
@Composable
fun MagicBallScreenPreviewIdle() {
    DivinationAppTheme {
        MagicBallScreen(
            onNavigateBack = {},
            onNavigateToStats = {},
            onNavigateToInfo = {} // Added for preview consistency.
        )
    }
}

/**
 * Preview composable for the [MagicBallScreen] in landscape orientation.
 * This demonstrates how the screen might look with different width/height ratios.
 */
@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "MagicBallScreen Landscape")
@Composable
fun MagicBallScreenLandscapePreview() {
    DivinationAppTheme {
        MagicBallScreen(
            onNavigateBack = {},
            onNavigateToStats = {},
            onNavigateToInfo = {} // Added for preview consistency.
        )
    }
}

